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

    fun startNewGame(player1: String, player2: String) {
        viewModelScope.launch {
            val game = GameState(
                player1 = player1.trim().ifBlank { "Joueur 1" },
                player2 = player2.trim().ifBlank { "Joueur 2" },
                currentPlayerIndex = 0,
                blocksPlaced = 0,
                targetLevel = 1,
                targetSlot = 1,
                placedPositions = emptySet(),
                isInProgress = true,
            )
            _uiState.update { it.copy(freePlayChallenge = null) }
            preferences.saveGame(game)
        }
    }

    fun placeBlock() {
        val state = _uiState.value
        val game = state.game
        if (!game.isInProgress || game.isFinished) return

        val level = game.targetLevel.coerceIn(1, 10)
        val slot = game.targetSlot.coerceIn(1, 3)
        if (game.isPlaced(level, slot)) return

        val challenge = pick(level, slot, state.settings, excludeId = null) ?: return
        val actor = game.currentPlayerIndex
        val placed = game.placedPositions + game.positionKey(level, slot)
        val finished = level == 10 && slot == 3

        val next = if (finished) {
            level to slot
        } else {
            nextTarget(level, slot, placed)
        }

        val practice = challenge.sexualPractice?.takeUnless { it == SexualPractice.CHOICE }
        val position = SexPositionCatalog.random(practice)

        val updated = game.copy(
            currentPlayerIndex = 1 - actor,
            blocksPlaced = placed.size,
            targetLevel = next.first,
            targetSlot = next.second,
            placedPositions = placed,
            currentChallengeId = challenge.id,
            challengePlayerIndex = actor,
            selectedSexualPractice = practice,
            currentSexPosition = position,
            isInProgress = !finished,
            isFinished = finished,
        )
        viewModelScope.launch { preferences.saveGame(updated) }
    }

    fun selectTargetSlot(slot: Int) {
        val game = _uiState.value.game
        val safeSlot = slot.coerceIn(1, 3)
        if (!game.isInProgress || game.isFinished || game.isPlaced(game.targetLevel, safeSlot)) return
        viewModelScope.launch { preferences.saveGame(game.copy(targetSlot = safeSlot)) }
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

    fun selectSexualPractice(practice: SexualPractice) {
        val state = _uiState.value
        val challenge = state.currentChallenge ?: return
        if (!challenge.sexual || practice == SexualPractice.CHOICE) return
        val position = SexPositionCatalog.random(practice, state.game.currentSexPosition)
        viewModelScope.launch {
            preferences.saveGame(
                state.game.copy(
                    selectedSexualPractice = practice,
                    currentSexPosition = position,
                )
            )
        }
    }

    fun rerollSexPosition() {
        val state = _uiState.value
        val challenge = state.currentChallenge ?: return
        if (!challenge.sexual) return
        val practice = state.game.selectedSexualPractice
            ?: challenge.sexualPractice?.takeUnless { it == SexualPractice.CHOICE }
            ?: return
        val position = SexPositionCatalog.random(practice, state.game.currentSexPosition)
        viewModelScope.launch {
            preferences.saveGame(
                state.game.copy(
                    selectedSexualPractice = practice,
                    currentSexPosition = position,
                )
            )
        }
    }

    fun useJoker() {
        val state = _uiState.value
        val current = state.currentChallenge ?: return
        val replacement = pick(current.level, current.slot, state.settings, current.id) ?: return
        val practice = replacement.sexualPractice?.takeUnless { it == SexualPractice.CHOICE }
        val position = SexPositionCatalog.random(practice)
        viewModelScope.launch {
            preferences.saveGame(
                state.game.copy(
                    currentChallengeId = replacement.id,
                    selectedSexualPractice = practice,
                    currentSexPosition = position,
                )
            )
        }
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
        startNewGame(game.player1, game.player2)
    }

    fun pickFreePlay() {
        val state = _uiState.value
        val maxLevel = state.game.reachedLevel.coerceAtLeast(1)
        val pool = allChallenges.filter { c ->
            c.level <= maxLevel && c.enabled &&
                c.intensity.rank <= state.settings.intensity.rank &&
                (state.settings.allowClothing || !c.clothing) &&
                (state.settings.allowFantasy || !c.fantasy) &&
                (state.settings.allowSexualPractices || !c.sexual)
        }
        if (pool.isNotEmpty()) {
            val previous = state.freePlayChallenge?.id
            val alternatives = pool.filterNot { it.id == previous }.ifEmpty { pool }
            _uiState.update { it.copy(freePlayChallenge = alternatives.random()) }
        }
    }

    fun setIntensity(intensity: Intensity) = updateSettings(_uiState.value.settings.copy(intensity = intensity))
    fun setAllowClothing(value: Boolean) = updateSettings(_uiState.value.settings.copy(allowClothing = value))
    fun setAllowFantasy(value: Boolean) = updateSettings(_uiState.value.settings.copy(allowFantasy = value))
    fun setAllowSexualPractices(value: Boolean) = updateSettings(_uiState.value.settings.copy(allowSexualPractices = value))

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

    private fun pick(level: Int, slot: Int, settings: AppSettings, excludeId: String?): Challenge? {
        val eligible = challengeRepository.eligible(allChallenges, level, slot, settings)
        val alternatives = eligible.filterNot { it.id == excludeId }.ifEmpty { eligible }
        return alternatives.randomOrNull()
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
