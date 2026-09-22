package com.crabscode.towerfortwo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.GameState
import com.crabscode.towerfortwo.model.Intensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tower_for_two")

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val PLAYER_1 = stringPreferencesKey("player_1")
        val PLAYER_2 = stringPreferencesKey("player_2")
        val CURRENT_PLAYER = intPreferencesKey("current_player")
        val BLOCKS_PLACED = intPreferencesKey("blocks_placed")
        val TARGET_LEVEL = intPreferencesKey("target_level")
        val TARGET_SLOT = intPreferencesKey("target_slot")
        val PLACED_POSITIONS = stringSetPreferencesKey("placed_positions")
        val CURRENT_CHALLENGE = stringPreferencesKey("current_challenge")
        val CHALLENGE_PLAYER = intPreferencesKey("challenge_player")
        val FALLEN_BY = intPreferencesKey("fallen_by")
        val IN_PROGRESS = booleanPreferencesKey("in_progress")
        val FINISHED = booleanPreferencesKey("finished")
        val INTENSITY = stringPreferencesKey("intensity")
        val ALLOW_CLOTHING = booleanPreferencesKey("allow_clothing")
        val ALLOW_FANTASY = booleanPreferencesKey("allow_fantasy")
    }

    val gameFlow: Flow<GameState> = context.dataStore.data.map { p ->
        val oldCount = p[Keys.BLOCKS_PLACED] ?: 0
        val migratedPositions = p[Keys.PLACED_POSITIONS] ?: (0 until oldCount.coerceAtMost(30))
            .map { index -> "${index / 3 + 1}:${index % 3 + 1}" }
            .toSet()
        val fallbackLevel = (oldCount / 3 + 1).coerceIn(1, 10)
        val fallbackSlot = (oldCount % 3 + 1).coerceIn(1, 3)

        GameState(
            player1 = p[Keys.PLAYER_1] ?: "Joueur 1",
            player2 = p[Keys.PLAYER_2] ?: "Joueur 2",
            currentPlayerIndex = p[Keys.CURRENT_PLAYER] ?: 0,
            blocksPlaced = migratedPositions.size,
            targetLevel = p[Keys.TARGET_LEVEL] ?: fallbackLevel,
            targetSlot = p[Keys.TARGET_SLOT] ?: fallbackSlot,
            placedPositions = migratedPositions,
            currentChallengeId = p[Keys.CURRENT_CHALLENGE],
            challengePlayerIndex = p[Keys.CHALLENGE_PLAYER],
            fallenByIndex = p[Keys.FALLEN_BY],
            isInProgress = p[Keys.IN_PROGRESS] ?: false,
            isFinished = p[Keys.FINISHED] ?: false,
        )
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            intensity = Intensity.fromName(p[Keys.INTENSITY]),
            allowClothing = p[Keys.ALLOW_CLOTHING] ?: false,
            allowFantasy = p[Keys.ALLOW_FANTASY] ?: true,
        )
    }

    suspend fun saveGame(game: GameState) {
        context.dataStore.edit { p ->
            p[Keys.PLAYER_1] = game.player1
            p[Keys.PLAYER_2] = game.player2
            p[Keys.CURRENT_PLAYER] = game.currentPlayerIndex
            p[Keys.BLOCKS_PLACED] = game.blocksPlaced
            p[Keys.TARGET_LEVEL] = game.targetLevel
            p[Keys.TARGET_SLOT] = game.targetSlot
            p[Keys.PLACED_POSITIONS] = game.placedPositions
            game.currentChallengeId?.let { p[Keys.CURRENT_CHALLENGE] = it } ?: p.remove(Keys.CURRENT_CHALLENGE)
            game.challengePlayerIndex?.let { p[Keys.CHALLENGE_PLAYER] = it } ?: p.remove(Keys.CHALLENGE_PLAYER)
            game.fallenByIndex?.let { p[Keys.FALLEN_BY] = it } ?: p.remove(Keys.FALLEN_BY)
            p[Keys.IN_PROGRESS] = game.isInProgress
            p[Keys.FINISHED] = game.isFinished
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { p ->
            p[Keys.INTENSITY] = settings.intensity.name
            p[Keys.ALLOW_CLOTHING] = settings.allowClothing
            p[Keys.ALLOW_FANTASY] = settings.allowFantasy
        }
    }
}
