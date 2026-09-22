package com.crabscode.towerfortwo.data

import android.content.Context
import android.content.SharedPreferences
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.Challenge
import com.crabscode.towerfortwo.model.ChallengeType
import com.crabscode.towerfortwo.model.Intensity
import com.crabscode.towerfortwo.model.SexPositionCatalog
import com.crabscode.towerfortwo.model.SexualPractice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class ChallengeRepository(private val context: Context) {
    private val customFile = File(context.filesDir, "custom_challenges.json")
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tower_for_two", Context.MODE_PRIVATE)

    private companion object {
        const val CUSTOM_CHALLENGES_JSON = "custom_challenges_json"
    }

    suspend fun loadAll(): List<Challenge> = withContext(Dispatchers.IO) {
        val builtIns = context.assets.open("challenges.json").bufferedReader().use { parse(it.readText(), false) }
        builtIns + loadCustom()
    }

    suspend fun loadCustom(): List<Challenge> = withContext(Dispatchers.IO) {
        val stored = prefs.getString(CUSTOM_CHALLENGES_JSON, null)
        if (stored != null) {
            return@withContext parse(stored, true)
        }

        // One-time migration from older app versions that used an internal JSON file.
        if (customFile.exists()) {
            val legacy = customFile.readText()
            prefs.edit().putString(CUSTOM_CHALLENGES_JSON, legacy).apply()
            customFile.delete()
            return@withContext parse(legacy, true)
        }

        emptyList()
    }

    suspend fun addCustom(
        level: Int,
        slot: Int,
        text: String,
        intensity: Intensity,
        clothing: Boolean,
        fantasy: Boolean,
        sexual: Boolean,
    ): Challenge = withContext(Dispatchers.IO) {
        val challenge = Challenge(
            id = "custom-${UUID.randomUUID()}",
            level = level.coerceIn(1, 10),
            slot = slot.coerceIn(1, 3),
            type = if (slot == 1) ChallengeType.ACTION else ChallengeType.TRUTH,
            text = text.trim(),
            intensity = intensity,
            clothing = clothing,
            fantasy = fantasy,
            sexual = sexual,
            sexRelated = sexual,
            sexualPractice = if (sexual) SexualPractice.CHOICE else null,
            custom = true,
        )
        val updated = loadCustom() + challenge
        saveCustom(updated)
        challenge
    }

    suspend fun updateCustom(
        id: String,
        level: Int,
        slot: Int,
        text: String,
        intensity: Intensity,
        clothing: Boolean,
        fantasy: Boolean,
        sexual: Boolean,
    ) = withContext(Dispatchers.IO) {
        val updated = loadCustom().map { current ->
            if (current.id != id) current else current.copy(
                level = level.coerceIn(1, 10),
                slot = slot.coerceIn(1, 3),
                type = if (slot == 1) ChallengeType.ACTION else ChallengeType.TRUTH,
                text = text.trim(),
                intensity = intensity,
                clothing = clothing,
                fantasy = fantasy,
                sexual = sexual,
                sexRelated = sexual,
                sexualPractice = if (sexual) (current.sexualPractice ?: SexualPractice.CHOICE) else null,
            )
        }
        saveCustom(updated)
    }

    suspend fun deleteCustom(id: String) = withContext(Dispatchers.IO) {
        saveCustom(loadCustom().filterNot { it.id == id })
    }

    fun eligible(
        all: List<Challenge>,
        level: Int,
        slot: Int? = null,
        settings: AppSettings,
    ): List<Challenge> {
        return all.filter { c ->
            c.level == level &&
                (slot == null || c.slot == slot) &&
                isEligibleChallenge(c, settings)
        }
    }

    fun isEligibleChallenge(c: Challenge, settings: AppSettings): Boolean {
        if (!c.enabled) return false
        if (!settings.allowClothing && c.clothing) return false
        if (!settings.allowFantasy && c.fantasy) return false

        val sexualFinal = settings.isSexualFinalLevel(c.level)

        if (sexualFinal) {
            if (!c.sexRelated || c.intensity != settings.intensity) return false
        } else {
            if (c.sexRelated || c.intensity.rank > settings.intensity.rank) return false
        }

        if (!c.sexual) return true

        val available = SexPositionCatalog.eligiblePractices(c.level, settings)
        if (available.isEmpty()) return false
        val required = c.sexualPractice?.takeUnless { it == SexualPractice.CHOICE }
        return required == null || required in available
    }

    private fun parse(json: String, custom: Boolean): List<Challenge> {
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(
                    Challenge(
                        id = o.getString("id"),
                        level = o.getInt("level"),
                        slot = o.getInt("slot"),
                        type = ChallengeType.valueOf(o.getString("type")),
                        text = o.getString("text"),
                        intensity = Intensity.fromName(o.optString("intensity", Intensity.TORRIDE.name)),
                        clothing = o.optBoolean("clothing", false),
                        fantasy = o.optBoolean("fantasy", false),
                        sexual = o.optBoolean("sexual", false),
                        sexRelated = o.optBoolean("sexRelated", o.optBoolean("sexual", false)),
                        sexualPractice = SexualPractice.fromName(o.optString("sexualPractice", null)),
                        enabled = o.optBoolean("enabled", true),
                        custom = custom || o.optBoolean("custom", false),
                    )
                )
            }
        }
    }

    private fun saveCustom(challenges: List<Challenge>) {
        val array = JSONArray()
        challenges.forEach { c ->
            array.put(JSONObject().apply {
                put("id", c.id)
                put("level", c.level)
                put("slot", c.slot)
                put("type", c.type.name)
                put("text", c.text)
                put("intensity", c.intensity.name)
                put("clothing", c.clothing)
                put("fantasy", c.fantasy)
                put("sexual", c.sexual)
                put("sexRelated", c.sexRelated)
                c.sexualPractice?.let { put("sexualPractice", it.name) }
                put("enabled", c.enabled)
                put("custom", true)
            })
        }
        prefs.edit().putString(CUSTOM_CHALLENGES_JSON, array.toString(2)).apply()
        if (customFile.exists()) customFile.delete()
    }
}
