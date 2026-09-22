package com.crabscode.towerfortwo.data

import android.content.Context
import android.content.SharedPreferences
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.GameState
import com.crabscode.towerfortwo.model.Intensity
import com.crabscode.towerfortwo.model.SexualPractice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tower_for_two", Context.MODE_PRIVATE)

    private object Keys {
        const val PLAYER_1 = "player_1"
        const val PLAYER_2 = "player_2"
        const val CURRENT_PLAYER = "current_player"
        const val BLOCKS_PLACED = "blocks_placed"
        const val TARGET_LEVEL = "target_level"
        const val TARGET_SLOT = "target_slot"
        const val PLACED_POSITIONS = "placed_positions"
        const val CURRENT_CHALLENGE = "current_challenge"
        const val CHALLENGE_PLAYER = "challenge_player"
        const val SELECTED_SEXUAL_PRACTICE = "selected_sexual_practice"
        const val CURRENT_SEX_POSITION = "current_sex_position"
        const val FALLEN_BY = "fallen_by"
        const val IN_PROGRESS = "in_progress"
        const val FINISHED = "finished"
        const val INTENSITY = "intensity"
        const val ALLOW_CLOTHING = "allow_clothing"
        const val ALLOW_FANTASY = "allow_fantasy"
        const val ALLOW_SEXUAL_PRACTICES = "allow_sexual_practices"
    }

    private val _gameFlow = MutableStateFlow(readGame())
    val gameFlow: StateFlow<GameState> = _gameFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(readSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun readGame(): GameState {
        val oldCount = prefs.getInt(Keys.BLOCKS_PLACED, 0)
        val storedPositions = prefs.getStringSet(Keys.PLACED_POSITIONS, null)?.toSet()
        val migratedPositions = storedPositions ?: (0 until oldCount.coerceAtMost(30))
            .map { index -> "${index / 3 + 1}:${index % 3 + 1}" }
            .toSet()
        val fallbackLevel = (oldCount / 3 + 1).coerceIn(1, 10)
        val fallbackSlot = (oldCount % 3 + 1).coerceIn(1, 3)

        return GameState(
            player1 = prefs.getString(Keys.PLAYER_1, "Joueur 1") ?: "Joueur 1",
            player2 = prefs.getString(Keys.PLAYER_2, "Joueur 2") ?: "Joueur 2",
            currentPlayerIndex = prefs.getInt(Keys.CURRENT_PLAYER, 0),
            blocksPlaced = migratedPositions.size,
            targetLevel = prefs.getInt(Keys.TARGET_LEVEL, fallbackLevel),
            targetSlot = prefs.getInt(Keys.TARGET_SLOT, fallbackSlot),
            placedPositions = migratedPositions,
            currentChallengeId = prefs.getString(Keys.CURRENT_CHALLENGE, null),
            challengePlayerIndex = prefs.takeIf { it.contains(Keys.CHALLENGE_PLAYER) }?.getInt(Keys.CHALLENGE_PLAYER, 0),
            selectedSexualPractice = SexualPractice.fromName(prefs.getString(Keys.SELECTED_SEXUAL_PRACTICE, null)),
            currentSexPosition = prefs.getString(Keys.CURRENT_SEX_POSITION, null),
            fallenByIndex = prefs.takeIf { it.contains(Keys.FALLEN_BY) }?.getInt(Keys.FALLEN_BY, 0),
            isInProgress = prefs.getBoolean(Keys.IN_PROGRESS, false),
            isFinished = prefs.getBoolean(Keys.FINISHED, false),
        )
    }

    private fun readSettings(): AppSettings =
        AppSettings(
            intensity = Intensity.fromName(prefs.getString(Keys.INTENSITY, null)),
            allowClothing = prefs.getBoolean(Keys.ALLOW_CLOTHING, false),
            allowFantasy = prefs.getBoolean(Keys.ALLOW_FANTASY, true),
            allowSexualPractices = prefs.getBoolean(Keys.ALLOW_SEXUAL_PRACTICES, false),
        )

    suspend fun saveGame(game: GameState) {
        prefs.edit().apply {
            putString(Keys.PLAYER_1, game.player1)
            putString(Keys.PLAYER_2, game.player2)
            putInt(Keys.CURRENT_PLAYER, game.currentPlayerIndex)
            putInt(Keys.BLOCKS_PLACED, game.blocksPlaced)
            putInt(Keys.TARGET_LEVEL, game.targetLevel)
            putInt(Keys.TARGET_SLOT, game.targetSlot)
            putStringSet(Keys.PLACED_POSITIONS, game.placedPositions)
            game.currentChallengeId?.let { putString(Keys.CURRENT_CHALLENGE, it) } ?: remove(Keys.CURRENT_CHALLENGE)
            game.challengePlayerIndex?.let { putInt(Keys.CHALLENGE_PLAYER, it) } ?: remove(Keys.CHALLENGE_PLAYER)
            game.selectedSexualPractice?.let { putString(Keys.SELECTED_SEXUAL_PRACTICE, it.name) } ?: remove(Keys.SELECTED_SEXUAL_PRACTICE)
            game.currentSexPosition?.let { putString(Keys.CURRENT_SEX_POSITION, it) } ?: remove(Keys.CURRENT_SEX_POSITION)
            game.fallenByIndex?.let { putInt(Keys.FALLEN_BY, it) } ?: remove(Keys.FALLEN_BY)
            putBoolean(Keys.IN_PROGRESS, game.isInProgress)
            putBoolean(Keys.FINISHED, game.isFinished)
            apply()
        }
        _gameFlow.value = game
    }

    suspend fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(Keys.INTENSITY, settings.intensity.name)
            .putBoolean(Keys.ALLOW_CLOTHING, settings.allowClothing)
            .putBoolean(Keys.ALLOW_FANTASY, settings.allowFantasy)
            .putBoolean(Keys.ALLOW_SEXUAL_PRACTICES, settings.allowSexualPractices)
            .apply()
        _settingsFlow.value = settings
    }
}
