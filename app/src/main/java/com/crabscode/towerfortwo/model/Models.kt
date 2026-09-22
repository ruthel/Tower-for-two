package com.crabscode.towerfortwo.model

enum class ChallengeType { ACTION, TRUTH }

enum class Intensity(val rank: Int, val label: String) {
    SENSUEL(1, "Sensuel"),
    TORRIDE(2, "Torride"),
    VERY_HOT(3, "Très torride");

    companion object {
        fun fromName(value: String?) = entries.firstOrNull { it.name == value } ?: TORRIDE
    }
}

data class Challenge(
    val id: String,
    val level: Int,
    val slot: Int,
    val type: ChallengeType,
    val text: String,
    val intensity: Intensity,
    val clothing: Boolean = false,
    val fantasy: Boolean = false,
    val enabled: Boolean = true,
    val custom: Boolean = false,
)

data class GameState(
    val player1: String = "Joueur 1",
    val player2: String = "Joueur 2",
    val currentPlayerIndex: Int = 0,
    val blocksPlaced: Int = 0,
    val currentChallengeId: String? = null,
    val challengePlayerIndex: Int? = null,
    val fallenByIndex: Int? = null,
    val isInProgress: Boolean = false,
    val isFinished: Boolean = false,
) {
    val nextLevel: Int get() = (blocksPlaced / 3 + 1).coerceAtMost(10)
    val nextSlot: Int get() = (blocksPlaced % 3) + 1
    val currentLevel: Int get() = if (blocksPlaced == 0) 1 else ((blocksPlaced - 1) / 3 + 1).coerceAtMost(10)
    val currentSlot: Int get() = if (blocksPlaced == 0) 1 else ((blocksPlaced - 1) % 3 + 1)
    val reachedLevel: Int get() = if (blocksPlaced == 0) 0 else ((blocksPlaced - 1) / 3 + 1).coerceAtMost(10)
    fun playerName(index: Int): String = if (index == 0) player1 else player2
}

data class AppSettings(
    val intensity: Intensity = Intensity.TORRIDE,
    val allowClothing: Boolean = false,
    val allowFantasy: Boolean = true,
)

data class GameUiState(
    val game: GameState = GameState(),
    val settings: AppSettings = AppSettings(),
    val challenges: List<Challenge> = emptyList(),
    val currentChallenge: Challenge? = null,
    val freePlayChallenge: Challenge? = null,
    val customChallenges: List<Challenge> = emptyList(),
    val loaded: Boolean = false,
)