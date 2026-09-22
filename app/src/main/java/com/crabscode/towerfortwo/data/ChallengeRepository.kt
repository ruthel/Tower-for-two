package com.crabscode.towerfortwo.data

import android.content.Context
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.Challenge
import com.crabscode.towerfortwo.model.ChallengeType
import com.crabscode.towerfortwo.model.Intensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class ChallengeRepository(private val context: Context) {
    private val customFile = File(context.filesDir, "custom_challenges.json")

    suspend fun loadAll(): List<Challenge> = withContext(Dispatchers.IO) {
        val builtIns = context.assets.open("challenges.json").bufferedReader().use { parse(it.readText(), false) }
        builtIns + loadCustom()
    }

    suspend fun loadCustom(): List<Challenge> = withContext(Dispatchers.IO) {
        if (!customFile.exists()) emptyList() else parse(customFile.readText(), true)
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
            c.enabled &&
                c.level == level &&
                (slot == null || c.slot == slot) &&
                c.intensity.rank <= settings.intensity.rank &&
                (settings.allowClothing || !c.clothing) &&
                (settings.allowFantasy || !c.fantasy) &&
                (settings.allowSexualPractices || !c.sexual)
        }
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
                put("enabled", c.enabled)
                put("custom", true)
            })
        }
        customFile.writeText(array.toString(2))
    }
}
