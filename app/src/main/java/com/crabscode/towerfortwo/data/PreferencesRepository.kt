package com.crabscode.towerfortwo.data

import android.content.Context
import android.content.SharedPreferences
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.GameState
import com.crabscode.towerfortwo.model.Intensity
import com.crabscode.towerfortwo.model.LifetimeStats
import com.crabscode.towerfortwo.model.PlayerGender
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
        const val PLAYER_1_GENDER = "player_1_gender"
        const val PLAYER_2_GENDER = "player_2_gender"
        const val PLAYER_SETUP_VALIDATED = "player_setup_validated"
        const val CURRENT_PLAYER = "current_player"
        const val BLOCKS_PLACED = "blocks_placed"
        const val TARGET_LEVEL = "target_level"
        const val TARGET_SLOT = "target_slot"
        const val PLACED_POSITIONS = "placed_positions"
        const val CURRENT_CHALLENGE = "current_challenge"
        const val CHALLENGE_PLAYER = "challenge_player"
        const val SELECTED_SEXUAL_PRACTICE = "selected_sexual_practice"
        const val CURRENT_SEX_POSITION = "current_sex_position"
        const val CURRENT_SEX_DURATION = "current_sex_duration"
        const val SEXUAL_GIVER = "sexual_giver"
        const val SEXUAL_RECEIVER = "sexual_receiver"
        const val RECENT_SEXUAL_PRACTICES = "recent_sexual_practices"
        const val RECENT_SEX_POSITIONS = "recent_sex_positions"
        const val RECENT_SEX_POSITION_FAMILIES = "recent_sex_position_families"
        const val RECENT_CHALLENGE_IDS = "recent_challenge_ids"
        const val COUNTDOWN_CHALLENGE_ID = "countdown_challenge_id"
        const val COUNTDOWN_INITIAL_SEC = "countdown_initial_sec"
        const val COUNTDOWN_REMAINING_SEC = "countdown_remaining_sec"
        const val COUNTDOWN_RUNNING = "countdown_running"
        const val FALLEN_BY = "fallen_by"
        const val IN_PROGRESS = "in_progress"
        const val FINISHED = "finished"
        const val INTENSITY = "intensity"
        const val ALLOW_CLOTHING = "allow_clothing"
        const val ALLOW_FANTASY = "allow_fantasy"
        const val ALLOW_SEXUAL_PRACTICES = "allow_sexual_practices"
        const val ALLOWED_SEXUAL_PRACTICES = "allowed_sexual_practices"
        const val PREFERRED_SEXUAL_PRACTICES = "preferred_sexual_practices"
        const val REJECTED_CHALLENGE_IDS = "rejected_challenge_ids"
        const val ALLOW_STANDING_SEX_POSITIONS = "allow_standing_sex_positions"
        const val SOUND_ENABLED = "sound_enabled"
        const val HAPTICS_ENABLED = "haptics_enabled"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val STATS_GAMES_STARTED = "stats_games_started"
        const val STATS_GAMES_COMPLETED = "stats_games_completed"
        const val STATS_TOWERS_FALLEN = "stats_towers_fallen"
        const val STATS_BLOCKS_PLACED = "stats_blocks_placed"
        const val STATS_JOKERS_USED = "stats_jokers_used"
        const val STATS_REJECTED = "stats_rejected"
        const val STATS_FREE_PLAY_DRAWS = "stats_free_play_draws"
        const val ONBOARDING_PAGE = "onboarding_page"
    }

    private val _gameFlow = MutableStateFlow(readGame())
    val gameFlow: StateFlow<GameState> = _gameFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(readSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private val _statsFlow = MutableStateFlow(readStats())
    val statsFlow: StateFlow<LifetimeStats> = _statsFlow.asStateFlow()

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
            player1Gender = PlayerGender.fromName(prefs.getString(Keys.PLAYER_1_GENDER, null)),
            player2Gender = PlayerGender.fromName(prefs.getString(Keys.PLAYER_2_GENDER, null)),
            playerSetupValidated = prefs.getBoolean(Keys.PLAYER_SETUP_VALIDATED, false),
            currentPlayerIndex = prefs.getInt(Keys.CURRENT_PLAYER, 0),
            blocksPlaced = migratedPositions.size,
            targetLevel = prefs.getInt(Keys.TARGET_LEVEL, fallbackLevel),
            targetSlot = prefs.getInt(Keys.TARGET_SLOT, fallbackSlot),
            placedPositions = migratedPositions,
            currentChallengeId = prefs.getString(Keys.CURRENT_CHALLENGE, null),
            challengePlayerIndex = prefs.takeIf { it.contains(Keys.CHALLENGE_PLAYER) }?.getInt(Keys.CHALLENGE_PLAYER, 0),
            selectedSexualPractice = SexualPractice.fromName(prefs.getString(Keys.SELECTED_SEXUAL_PRACTICE, null)),
            currentSexPosition = prefs.getString(Keys.CURRENT_SEX_POSITION, null),
            currentSexDurationSec = prefs.getInt(Keys.CURRENT_SEX_DURATION, 0),
            sexualGiverIndex = prefs.takeIf { it.contains(Keys.SEXUAL_GIVER) }?.getInt(Keys.SEXUAL_GIVER, 0),
            sexualReceiverIndex = prefs.takeIf { it.contains(Keys.SEXUAL_RECEIVER) }?.getInt(Keys.SEXUAL_RECEIVER, 1),
            recentSexualPractices = decodeList(prefs.getString(Keys.RECENT_SEXUAL_PRACTICES, null)),
            recentSexPositionIds = decodeList(prefs.getString(Keys.RECENT_SEX_POSITIONS, null)),
            recentSexPositionFamilies = decodeList(prefs.getString(Keys.RECENT_SEX_POSITION_FAMILIES, null)),
            recentChallengeIds = decodeList(prefs.getString(Keys.RECENT_CHALLENGE_IDS, null)),
            countdownChallengeId = prefs.getString(Keys.COUNTDOWN_CHALLENGE_ID, null),
            countdownInitialSec = prefs.getInt(Keys.COUNTDOWN_INITIAL_SEC, 0),
            countdownRemainingSec = prefs.getInt(Keys.COUNTDOWN_REMAINING_SEC, 0),
            countdownRunning = prefs.getBoolean(Keys.COUNTDOWN_RUNNING, false),
            fallenByIndex = prefs.takeIf { it.contains(Keys.FALLEN_BY) }?.getInt(Keys.FALLEN_BY, 0),
            isInProgress = prefs.getBoolean(Keys.IN_PROGRESS, false),
            isFinished = prefs.getBoolean(Keys.FINISHED, false),
        )
    }

    private fun readSettings(): AppSettings {
        val stored = prefs.getStringSet(Keys.ALLOWED_SEXUAL_PRACTICES, null)
        val allowed = stored
            ?.mapNotNull(SexualPractice::fromName)
            ?.filterNot { it == SexualPractice.CHOICE }
            ?.toSet()
            ?: SexualPractice.playable.toSet()

        val preferred = prefs.getStringSet(Keys.PREFERRED_SEXUAL_PRACTICES, null)
            ?.mapNotNull(SexualPractice::fromName)
            ?.filterNot { it == SexualPractice.CHOICE }
            ?.toSet()
            .orEmpty()

        return AppSettings(
            intensity = Intensity.fromName(prefs.getString(Keys.INTENSITY, null)),
            allowClothing = prefs.getBoolean(Keys.ALLOW_CLOTHING, false),
            allowFantasy = prefs.getBoolean(Keys.ALLOW_FANTASY, true),
            allowSexualPractices = prefs.getBoolean(Keys.ALLOW_SEXUAL_PRACTICES, false),
            allowedSexualPractices = allowed,
            preferredSexualPractices = preferred intersect allowed,
            rejectedChallengeIds = prefs.getStringSet(Keys.REJECTED_CHALLENGE_IDS, emptySet())?.toSet().orEmpty(),
            allowStandingSexPositions = prefs.getBoolean(Keys.ALLOW_STANDING_SEX_POSITIONS, true),
            soundEnabled = prefs.getBoolean(Keys.SOUND_ENABLED, true),
            hapticsEnabled = prefs.getBoolean(Keys.HAPTICS_ENABLED, true),
            onboardingCompleted = prefs.getBoolean(Keys.ONBOARDING_COMPLETED, false),
            onboardingPage = prefs.getInt(Keys.ONBOARDING_PAGE, 0).coerceIn(0, 3),
        )
    }

    private fun readStats(): LifetimeStats = LifetimeStats(
        gamesStarted = prefs.getInt(Keys.STATS_GAMES_STARTED, 0),
        gamesCompleted = prefs.getInt(Keys.STATS_GAMES_COMPLETED, 0),
        towersFallen = prefs.getInt(Keys.STATS_TOWERS_FALLEN, 0),
        blocksPlaced = prefs.getInt(Keys.STATS_BLOCKS_PLACED, 0),
        jokersUsed = prefs.getInt(Keys.STATS_JOKERS_USED, 0),
        rejectedChallenges = prefs.getInt(Keys.STATS_REJECTED, 0),
        freePlayDraws = prefs.getInt(Keys.STATS_FREE_PLAY_DRAWS, 0),
    )

    suspend fun saveGame(game: GameState) {
        prefs.edit().apply {
            putString(Keys.PLAYER_1, game.player1)
            putString(Keys.PLAYER_2, game.player2)
            putString(Keys.PLAYER_1_GENDER, game.player1Gender.name)
            putString(Keys.PLAYER_2_GENDER, game.player2Gender.name)
            putBoolean(Keys.PLAYER_SETUP_VALIDATED, game.playerSetupValidated)
            putInt(Keys.CURRENT_PLAYER, game.currentPlayerIndex)
            putInt(Keys.BLOCKS_PLACED, game.blocksPlaced)
            putInt(Keys.TARGET_LEVEL, game.targetLevel)
            putInt(Keys.TARGET_SLOT, game.targetSlot)
            putStringSet(Keys.PLACED_POSITIONS, game.placedPositions)
            game.currentChallengeId?.let { putString(Keys.CURRENT_CHALLENGE, it) } ?: remove(Keys.CURRENT_CHALLENGE)
            game.challengePlayerIndex?.let { putInt(Keys.CHALLENGE_PLAYER, it) } ?: remove(Keys.CHALLENGE_PLAYER)
            game.selectedSexualPractice?.let { putString(Keys.SELECTED_SEXUAL_PRACTICE, it.name) } ?: remove(Keys.SELECTED_SEXUAL_PRACTICE)
            game.currentSexPosition?.let { putString(Keys.CURRENT_SEX_POSITION, it) } ?: remove(Keys.CURRENT_SEX_POSITION)
            if (game.currentSexDurationSec > 0) putInt(Keys.CURRENT_SEX_DURATION, game.currentSexDurationSec) else remove(Keys.CURRENT_SEX_DURATION)
            game.sexualGiverIndex?.let { putInt(Keys.SEXUAL_GIVER, it) } ?: remove(Keys.SEXUAL_GIVER)
            game.sexualReceiverIndex?.let { putInt(Keys.SEXUAL_RECEIVER, it) } ?: remove(Keys.SEXUAL_RECEIVER)
            putString(Keys.RECENT_SEXUAL_PRACTICES, encodeList(game.recentSexualPractices))
            putString(Keys.RECENT_SEX_POSITIONS, encodeList(game.recentSexPositionIds))
            putString(Keys.RECENT_SEX_POSITION_FAMILIES, encodeList(game.recentSexPositionFamilies))
            putString(Keys.RECENT_CHALLENGE_IDS, encodeList(game.recentChallengeIds))
            game.countdownChallengeId?.let { putString(Keys.COUNTDOWN_CHALLENGE_ID, it) } ?: remove(Keys.COUNTDOWN_CHALLENGE_ID)
            putInt(Keys.COUNTDOWN_INITIAL_SEC, game.countdownInitialSec)
            putInt(Keys.COUNTDOWN_REMAINING_SEC, game.countdownRemainingSec)
            putBoolean(Keys.COUNTDOWN_RUNNING, game.countdownRunning)
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
            .putStringSet(Keys.ALLOWED_SEXUAL_PRACTICES, settings.allowedSexualPractices.map { it.name }.toSet())
            .putStringSet(Keys.PREFERRED_SEXUAL_PRACTICES, settings.preferredSexualPractices.map { it.name }.toSet())
            .putStringSet(Keys.REJECTED_CHALLENGE_IDS, settings.rejectedChallengeIds)
            .putBoolean(Keys.ALLOW_STANDING_SEX_POSITIONS, settings.allowStandingSexPositions)
            .putBoolean(Keys.SOUND_ENABLED, settings.soundEnabled)
            .putBoolean(Keys.HAPTICS_ENABLED, settings.hapticsEnabled)
            .putBoolean(Keys.ONBOARDING_COMPLETED, settings.onboardingCompleted)
            .putInt(Keys.ONBOARDING_PAGE, settings.onboardingPage.coerceIn(0, 3))
            .apply()
        _settingsFlow.value = settings
    }

    suspend fun saveStats(stats: LifetimeStats) {
        prefs.edit()
            .putInt(Keys.STATS_GAMES_STARTED, stats.gamesStarted)
            .putInt(Keys.STATS_GAMES_COMPLETED, stats.gamesCompleted)
            .putInt(Keys.STATS_TOWERS_FALLEN, stats.towersFallen)
            .putInt(Keys.STATS_BLOCKS_PLACED, stats.blocksPlaced)
            .putInt(Keys.STATS_JOKERS_USED, stats.jokersUsed)
            .putInt(Keys.STATS_REJECTED, stats.rejectedChallenges)
            .putInt(Keys.STATS_FREE_PLAY_DRAWS, stats.freePlayDraws)
            .apply()
        _statsFlow.value = stats
    }

    private fun encodeList(values: List<String>): String = values.joinToString("|")
    private fun decodeList(value: String?): List<String> =
        value?.split("|")?.filter { it.isNotBlank() }.orEmpty()
}
