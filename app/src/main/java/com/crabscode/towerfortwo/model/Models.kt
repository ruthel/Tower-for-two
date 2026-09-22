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
    val targetLevel: Int = 1,
    val targetSlot: Int = 1,
    val placedPositions: Set<String> = emptySet(),
    val currentChallengeId: String? = null,
    val challengePlayerIndex: Int? = null,
    val fallenByIndex: Int? = null,
    val isInProgress: Boolean = false,
    val isFinished: Boolean = false,
) {
    val nextLevel: Int get() = targetLevel.coerceIn(1, 10)
    val nextSlot: Int get() = targetSlot.coerceIn(1, 3)
    val currentLevel: Int get() = nextLevel
    val currentSlot: Int get() = nextSlot
    val reachedLevel: Int
        get() = placedPositions
            .mapNotNull { it.substringBefore(':').toIntOrNull() }
            .maxOrNull()
            ?: if (blocksPlaced == 0) 0 else ((blocksPlaced - 1) / 3 + 1).coerceAtMost(10)

    val completedLevelCount: Int
        get() = if (isFinished) 10 else (nextLevel - 1).coerceIn(0, 9)

    fun playerName(index: Int): String = if (index == 0) player1 else player2
    fun positionKey(level: Int, slot: Int): String = "$level:$slot"
    fun isPlaced(level: Int, slot: Int): Boolean = positionKey(level, slot) in placedPositions
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
