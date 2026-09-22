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
                isInProgress = true,
            )
            _uiState.update { it.copy(freePlayChallenge = null) }
            preferences.saveGame(game)
        }
    }

    fun placeBlock() {
        val state = _uiState.value
        val game = state.game
        if (!game.isInProgress || game.blocksPlaced >= 30) return

        val index = game.blocksPlaced
        val level = index / 3 + 1
        val slot = index % 3 + 1
        val challenge = pick(level, slot, state.settings, excludeId = null) ?: return
        val actor = game.currentPlayerIndex
        val newCount = game.blocksPlaced + 1

        val updated = game.copy(
            currentPlayerIndex = 1 - actor,
            blocksPlaced = newCount,
            currentChallengeId = challenge.id,
            challengePlayerIndex = actor,
            isFinished = newCount >= 30,
        )
        viewModelScope.launch { preferences.saveGame(updated) }
    }

    fun useJoker() {
        val state = _uiState.value
        val current = state.currentChallenge ?: return
        val replacement = pick(current.level, current.slot, state.settings, current.id) ?: return
        viewModelScope.launch {
            preferences.saveGame(state.game.copy(currentChallengeId = replacement.id))
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
                (state.settings.allowFantasy || !c.fantasy)
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
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            challengeRepository.addCustom(level, slot, text, intensity, clothing, fantasy)
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
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            challengeRepository.updateCustom(id, level, slot, text, intensity, clothing, fantasy)
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