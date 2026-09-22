package com.crabscode.towerfortwo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crabscode.towerfortwo.data.ChallengeRepository
import com.crabscode.towerfortwo.data.PreferencesRepository
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.Challenge
import com.crabscode.towerfortwo.model.GameState
import com.crabscode.towerfortwo.model.GameUiState
import com.crabscode.towerfortwo.model.Intensity
import com.crabscode.towerfortwo.model.PlayerGender
import com.crabscode.towerfortwo.model.ResolvedSexualAction
import com.crabscode.towerfortwo.model.SexPositionCatalog
import com.crabscode.towerfortwo.model.SexualPractice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TowerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesRepository(application)
    private val challengeRepository = ChallengeRepository(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var allChallenges: List<Challenge> = emptyList()

    init {
        viewModelScope.launch {
            allChallenges = challengeRepository.loadAll()
            combine(preferences.gameFlow, preferences.settingsFlow) { game, settings -> game to settings }
                .collect { (game, settings) ->
                    _uiState.update {
                        it.copy(
                            game = game,
                            settings = settings,
                            challenges = allChallenges,
                            customChallenges = allChallenges.filter { c -> c.custom },
                            currentChallenge = game.currentChallengeId?.let { id -> allChallenges.find { c -> c.id == id } },
                            loaded = true,
                        )
                    }
                }
        }
    }

    fun startNewGame(
        player1: String,
        player1Gender: PlayerGender,
        player2: String,
        player2Gender: PlayerGender,
    ) {
        viewModelScope.launch {
            val previous = _uiState.value.game
            val game = GameState(
                player1 = player1.trim().ifBlank { "Joueur 1" },
                player2 = player2.trim().ifBlank { "Joueur 2" },
                player1Gender = player1Gender,
                player2Gender = player2Gender,
                playerSetupValidated = true,
                currentPlayerIndex = 0,
                blocksPlaced = 0,
                targetLevel = 1,
                targetSlot = 1,
                placedPositions = emptySet(),
                recentSexualPractices = previous.recentSexualPractices,
                recentSexPositionIds = previous.recentSexPositionIds,
                recentChallengeIds = previous.recentChallengeIds,
                isInProgress = true,
            )
            _uiState.update { it.copy(freePlayChallenge = null) }
            preferences.saveGame(game)
        }
    }

    fun savePlayerSetup(
        player1: String,
        player1Gender: PlayerGender,
        player2: String,
        player2Gender: PlayerGender,
        validated: Boolean,
    ) {
        val current = _uiState.value.game
        viewModelScope.launch {
            preferences.saveGame(
                current.copy(
                    player1 = player1.trim().ifBlank { "Joueur 1" },
                    player2 = player2.trim().ifBlank { "Joueur 2" },
                    player1Gender = player1Gender,
                    player2Gender = player2Gender,
                    playerSetupValidated = validated,
                )
            )
        }
    }

    fun setOnboardingPage(page: Int) {
        updateSettings(
            _uiState.value.settings.copy(
                onboardingPage = page.coerceIn(0, 3),
            )
        )
    }

    fun completeOnboarding() {
        updateSettings(
            _uiState.value.settings.copy(
                onboardingCompleted = true,
                onboardingPage = 3,
            )
        )
    }

    fun placeBlock() {
        val state = _uiState.value
        val game = state.game
        if (!game.isInProgress || game.isFinished) return

        val level = game.targetLevel.coerceIn(1, 10)
        val slot = game.targetSlot.coerceIn(1, 3)
        if (game.isPlaced(level, slot)) return

        val challenge = pick(
            level = level,
            slot = slot,
            settings = state.settings,
            excludeId = null,
            recentIds = game.recentChallengeIds,
        ) ?: return
        val actor = game.currentPlayerIndex
        val placed = game.placedPositions + game.positionKey(level, slot)
        val finished = level == 10 && slot == 3

        val next = if (finished) {
            level to slot
        } else {
            nextTarget(level, slot, placed)
        }

        val resolved = SexPositionCatalog.resolve(challenge, game, state.settings, actor)

        var updated = game.copy(
            currentPlayerIndex = 1 - actor,
            blocksPlaced = placed.size,
            targetLevel = next.first,
            targetSlot = next.second,
            placedPositions = placed,
            currentChallengeId = challenge.id,
            challengePlayerIndex = actor,
            selectedSexualPractice = null,
            currentSexPosition = null,
            currentSexDurationSec = 0,
            sexualGiverIndex = null,
            sexualReceiverIndex = null,
            recentChallengeIds = (game.recentChallengeIds + challenge.id).takeLast(50),
            countdownChallengeId = null,
            countdownInitialSec = 0,
            countdownRemainingSec = 0,
            countdownRunning = false,
            isInProgress = !finished,
            isFinished = finished,
        )
        updated = applyResolvedAction(updated, resolved)
        viewModelScope.launch { preferences.saveGame(updated) }
    }

    fun nextFloor() {
        val game = _uiState.value.game
        if (!game.isInProgress || game.isFinished || !game.canAdvanceFloor) return
        val newLevel = game.targetLevel + 1
        val firstAvailable = (1..3).firstOrNull { !game.isPlaced(newLevel, it) } ?: 1
        viewModelScope.launch {
            preferences.saveGame(game.copy(targetLevel = newLevel, targetSlot = firstAvailable))
        }
    }

    private fun nextTarget(level: Int, slot: Int, placed: Set<String>): Pair<Int, Int> {
        val nextSlot = ((slot + 1)..3).firstOrNull { "$level:$it" !in placed }
        if (nextSlot != null) return level to nextSlot
        if (level < 10) return (level + 1) to 1
        val remaining = (1..3).firstOrNull { "10:$it" !in placed }
        return 10 to (remaining ?: 3)
    }

    fun persistCountdown(
        challengeId: String,
        initialSeconds: Int,
        remainingSeconds: Int,
        running: Boolean,
    ) {
        val game = _uiState.value.game
        if (game.currentChallengeId != challengeId) return

        val safeInitial = initialSeconds.coerceAtLeast(1)
        val safeRemaining = remainingSeconds.coerceIn(0, safeInitial)
        val updated = game.copy(
            countdownChallengeId = challengeId,
            countdownInitialSec = safeInitial,
            countdownRemainingSec = safeRemaining,
            countdownRunning = running && safeRemaining > 0,
        )

        if (
            updated.countdownChallengeId == game.countdownChallengeId &&
            updated.countdownInitialSec == game.countdownInitialSec &&
            updated.countdownRemainingSec == game.countdownRemainingSec &&
            updated.countdownRunning == game.countdownRunning
        ) return

        viewModelScope.launch { preferences.saveGame(updated) }
    }

    fun rerollSexPosition() {
        val state = _uiState.value
        val challenge = state.currentChallenge ?: return
        val current = state.game.resolvedSexualAction() ?: return
        if (!challenge.sexual) return

        val rerolled = SexPositionCatalog.reroll(
            current = current,
            level = challenge.level,
            game = state.game,
            settings = state.settings,
        )
        val updated = state.game.copy(
            selectedSexualPractice = rerolled.practice,
            currentSexPosition = rerolled.positionId,
            currentSexDurationSec = rerolled.durationSec,
            sexualGiverIndex = rerolled.giverPlayerIndex,
            sexualReceiverIndex = rerolled.receiverPlayerIndex,
            recentSexPositionIds = (state.game.recentSexPositionIds + rerolled.positionId).takeLast(8),
        )
        viewModelScope.launch { preferences.saveGame(updated) }
    }

    fun useJoker() {
        val state = _uiState.value
        val current = state.currentChallenge ?: return
        val replacement = pick(
            level = current.level,
            slot = current.slot,
            settings = state.settings,
            excludeId = current.id,
            recentIds = state.game.recentChallengeIds,
        ) ?: return
        val actor = state.game.challengePlayerIndex ?: (1 - state.game.currentPlayerIndex)
        val resolved = SexPositionCatalog.resolve(replacement, state.game, state.settings, actor)

        var updated = state.game.copy(
            currentChallengeId = replacement.id,
            selectedSexualPractice = null,
            currentSexPosition = null,
            currentSexDurationSec = 0,
            sexualGiverIndex = null,
            sexualReceiverIndex = null,
            recentChallengeIds = (state.game.recentChallengeIds + replacement.id).takeLast(50),
            countdownChallengeId = null,
            countdownInitialSec = 0,
            countdownRemainingSec = 0,
            countdownRunning = false,
        )
        updated = applyResolvedAction(updated, resolved)
        viewModelScope.launch { preferences.saveGame(updated) }
    }

    fun markTowerFallen(playerIndex: Int) {
        val game = _uiState.value.game
        viewModelScope.launch {
            preferences.saveGame(
                game.copy(
                    fallenByIndex = playerIndex.coerceIn(0, 1),
                    isInProgress = false,
                )
            )
        }
    }

    fun replay() {
        val game = _uiState.value.game
        startNewGame(game.player1, game.player1Gender, game.player2, game.player2Gender)
    }

    fun pickFreePlay() {
        val state = _uiState.value
        val maxLevel = state.game.reachedLevel.coerceAtLeast(1)
        val pool = allChallenges.filter { c ->
            c.level <= maxLevel && challengeRepository.isEligibleChallenge(c, state.settings)
        }
        chooseFreePlayChallenge(pool)
    }

    fun pickFinalSurprise() {
        val state = _uiState.value
        val pool = allChallenges.filter { c ->
            c.level == 10 && challengeRepository.isEligibleChallenge(c, state.settings)
        }
        chooseFreePlayChallenge(pool)
    }

    private fun chooseFreePlayChallenge(pool: List<Challenge>) {
        if (pool.isEmpty()) return
        val state = _uiState.value
        val previous = state.freePlayChallenge?.id
        val recent = state.game.recentChallengeIds.takeLast(30).toSet()
        val preferred = pool.filterNot { it.id == previous || it.id in recent }
        val alternatives = preferred.ifEmpty {
            pool.filterNot { it.id == previous }.ifEmpty { pool }
        }
        val chosen = alternatives.random()
        _uiState.update { it.copy(freePlayChallenge = chosen) }
        viewModelScope.launch {
            preferences.saveGame(
                state.game.copy(
                    recentChallengeIds = (state.game.recentChallengeIds + chosen.id).takeLast(50),
                )
            )
        }
    }

    fun setIntensity(intensity: Intensity) = updateSettings(_uiState.value.settings.copy(intensity = intensity))
    fun setAllowClothing(value: Boolean) = updateSettings(_uiState.value.settings.copy(allowClothing = value))
    fun setAllowFantasy(value: Boolean) = updateSettings(_uiState.value.settings.copy(allowFantasy = value))
    fun setAllowSexualPractices(value: Boolean) = updateSettings(_uiState.value.settings.copy(allowSexualPractices = value))
    fun setAllowStandingSexPositions(value: Boolean) =
        updateSettings(_uiState.value.settings.copy(allowStandingSexPositions = value))

    fun setSexualPracticeAllowed(practice: SexualPractice, value: Boolean) {
        if (practice == SexualPractice.CHOICE) return
        val current = _uiState.value.settings.allowedSexualPractices
        if (!value && practice in current && current.size <= 1) return
        val updated = if (value) current + practice else current - practice
        updateSettings(_uiState.value.settings.copy(allowedSexualPractices = updated))
    }

    private fun updateSettings(settings: AppSettings) {
        viewModelScope.launch { preferences.saveSettings(settings) }
    }

    fun addCustomChallenge(
        level: Int,
        slot: Int,
        text: String,
        intensity: Intensity,
        clothing: Boolean,
        fantasy: Boolean,
        sexual: Boolean,
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            challengeRepository.addCustom(level, slot, text, intensity, clothing, fantasy, sexual)
            reloadChallenges()
        }
    }

    fun updateCustomChallenge(
        id: String,
        level: Int,
        slot: Int,
        text: String,
        intensity: Intensity,
        clothing: Boolean,
        fantasy: Boolean,
        sexual: Boolean,
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            challengeRepository.updateCustom(id, level, slot, text, intensity, clothing, fantasy, sexual)
            reloadChallenges()
        }
    }

    fun deleteCustomChallenge(id: String) {
        viewModelScope.launch {
            challengeRepository.deleteCustom(id)
            reloadChallenges()
        }
    }

    private suspend fun reloadChallenges() {
        allChallenges = challengeRepository.loadAll()
        val currentGame = _uiState.value.game
        _uiState.update {
            it.copy(
                challenges = allChallenges,
                customChallenges = allChallenges.filter { c -> c.custom },
                currentChallenge = currentGame.currentChallengeId?.let { id -> allChallenges.find { c -> c.id == id } },
            )
        }
    }

    private fun pick(
        level: Int,
        slot: Int,
        settings: AppSettings,
        excludeId: String?,
        recentIds: List<String>,
    ): Challenge? {
        val eligible = challengeRepository.eligible(allChallenges, level, slot, settings)
            .filterNot { it.id == excludeId }

        if (eligible.isNotEmpty()) {
            val recent = recentIds.takeLast(30).toSet()
            return eligible.filterNot { it.id in recent }
                .ifEmpty { eligible }
                .random()
        }

        // Safety net: never let a valid placement become a dead button.
        val sameCell = allChallenges.filter {
            it.enabled &&
                it.level == level &&
                it.slot == slot &&
                it.id != excludeId
        }
        return sameCell.randomOrNull()
    }

    private fun applyResolvedAction(game: GameState, resolved: ResolvedSexualAction?): GameState {
        if (resolved == null) return game
        return game.copy(
            selectedSexualPractice = resolved.practice,
            currentSexPosition = resolved.positionId,
            currentSexDurationSec = resolved.durationSec,
            sexualGiverIndex = resolved.giverPlayerIndex,
            sexualReceiverIndex = resolved.receiverPlayerIndex,
            recentSexualPractices = (game.recentSexualPractices + resolved.practice.name).takeLast(6),
            recentSexPositionIds = (game.recentSexPositionIds + resolved.positionId).takeLast(8),
        )
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TowerViewModel(application) as T
                }
            }
    }
}
