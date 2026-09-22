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

enum class PlayerGender(val label: String) {
    HOMME("Homme"),
    FEMME("Femme"),
    AUTRE("Autre"),
    NON_PRECISE("Non précisé");

    companion object {
        fun fromName(value: String?): PlayerGender =
            entries.firstOrNull { it.name == value } ?: NON_PRECISE
    }
}

enum class SexualPractice(val label: String, val minLevel: Int) {
    CARESSES_INTIMES("Caresses intimes", 7),
    MASTURBATION("Masturbation", 7),
    MASTURBATION_MUTUELLE("Masturbation mutuelle", 8),
    SEXE_ORAL("Sexe oral", 8),
    PENETRATION("Pénétration", 10),
    CHOICE("Surprise", 7);

    companion object {
        val playable: List<SexualPractice>
            get() = entries.filterNot { it == CHOICE }

        fun fromName(value: String?): SexualPractice? =
            entries.firstOrNull { it.name == value }
    }
}

enum class SexStickerPose {
    FACE_TO_FACE,
    SIDE_BY_SIDE,
    SPOON,
    RECEIVER_LYING,
    RECEIVER_SEATED,
    KNEELING,
    PARTNER_ON_TOP,
    SEATED_EMBRACE,
    STANDING_FACE_TO_FACE,
}

enum class SexPositionFamily(val label: String) {
    FACE_TO_FACE("Face à face"),
    SIDE("Sur le côté"),
    SPOON("Cuillère"),
    LYING("Allongé"),
    SEATED("Assis"),
    KNEELING("À genoux"),
    TOP("Au-dessus"),
    STANDING("Debout");

    companion object {
        fun fromPose(pose: SexStickerPose): SexPositionFamily = when (pose) {
            SexStickerPose.FACE_TO_FACE -> FACE_TO_FACE
            SexStickerPose.SIDE_BY_SIDE -> SIDE
            SexStickerPose.SPOON -> SPOON
            SexStickerPose.RECEIVER_LYING -> LYING
            SexStickerPose.RECEIVER_SEATED, SexStickerPose.SEATED_EMBRACE -> SEATED
            SexStickerPose.KNEELING -> KNEELING
            SexStickerPose.PARTNER_ON_TOP -> TOP
            SexStickerPose.STANDING_FACE_TO_FACE -> STANDING
        }
    }
}

data class SexPositionSpec(
    val id: String,
    val practice: SexualPractice,
    val label: String,
    val setup: String,
    val sticker: SexStickerPose,
    val standing: Boolean = false,
    val minLevel: Int = practice.minLevel,
    val family: SexPositionFamily = SexPositionFamily.fromPose(sticker),
)

data class ResolvedSexualAction(
    val practice: SexualPractice,
    val positionId: String,
    val durationSec: Int,
    val giverPlayerIndex: Int? = null,
    val receiverPlayerIndex: Int? = null,
) {
    val mutual: Boolean get() = practice == SexualPractice.MASTURBATION_MUTUELLE
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
    val sexual: Boolean = false,
    val sexRelated: Boolean = false,
    val sexualPractice: SexualPractice? = null,
    val enabled: Boolean = true,
    val custom: Boolean = false,
)

data class GameState(
    val player1: String = "Joueur 1",
    val player2: String = "Joueur 2",
    val player1Gender: PlayerGender = PlayerGender.NON_PRECISE,
    val player2Gender: PlayerGender = PlayerGender.NON_PRECISE,
    val playerSetupValidated: Boolean = false,
    val currentPlayerIndex: Int = 0,
    val blocksPlaced: Int = 0,
    val targetLevel: Int = 1,
    val targetSlot: Int = 1,
    val placedPositions: Set<String> = emptySet(),
    val currentChallengeId: String? = null,
    val challengePlayerIndex: Int? = null,
    val selectedSexualPractice: SexualPractice? = null,
    val currentSexPosition: String? = null,
    val currentSexDurationSec: Int = 0,
    val sexualGiverIndex: Int? = null,
    val sexualReceiverIndex: Int? = null,
    val recentSexualPractices: List<String> = emptyList(),
    val recentSexPositionIds: List<String> = emptyList(),
    val recentSexPositionFamilies: List<String> = emptyList(),
    val recentChallengeIds: List<String> = emptyList(),
    val countdownChallengeId: String? = null,
    val countdownInitialSec: Int = 0,
    val countdownRemainingSec: Int = 0,
    val countdownRunning: Boolean = false,
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

    val placedOnCurrentLevel: Int
        get() = placedCountOnLevel(targetLevel)

    val canAdvanceFloor: Boolean
        get() = targetLevel < 10 && placedOnCurrentLevel >= 1

    val fullLevelCount: Int
        get() = (1..10).count { placedCountOnLevel(it) == 3 }

    val shortenedLevelCount: Int
        get() = (1..9).count { placedCountOnLevel(it) in 1..2 }

    fun playerName(index: Int): String = if (index == 0) player1 else player2
    fun playerGender(index: Int): PlayerGender = if (index == 0) player1Gender else player2Gender
    fun positionKey(level: Int, slot: Int): String = "$level:$slot"
    fun isPlaced(level: Int, slot: Int): Boolean = positionKey(level, slot) in placedPositions
    fun placedCountOnLevel(level: Int): Int =
        placedPositions.count { it.startsWith("$level:") }
    fun hasPlacedOnLevel(level: Int): Boolean = placedCountOnLevel(level) > 0

    fun resolvedSexualAction(): ResolvedSexualAction? {
        val practice = selectedSexualPractice ?: return null
        val position = currentSexPosition ?: return null
        if (currentSexDurationSec <= 0) return null
        return ResolvedSexualAction(
            practice = practice,
            positionId = position,
            durationSec = currentSexDurationSec,
            giverPlayerIndex = sexualGiverIndex,
            receiverPlayerIndex = sexualReceiverIndex,
        )
    }
}

data class AppSettings(
    val intensity: Intensity = Intensity.TORRIDE,
    val allowClothing: Boolean = false,
    val allowFantasy: Boolean = true,
    val allowSexualPractices: Boolean = false,
    val allowedSexualPractices: Set<SexualPractice> = SexualPractice.playable.toSet(),
    val preferredSexualPractices: Set<SexualPractice> = emptySet(),
    val rejectedChallengeIds: Set<String> = emptySet(),
    val allowStandingSexPositions: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val onboardingPage: Int = 0,
) {
    val sexualFinalStartLevel: Int
        get() = when (intensity) {
            Intensity.SENSUEL -> 10
            Intensity.TORRIDE -> 9
            Intensity.VERY_HOT -> 8
        }

    fun isSexualFinalLevel(level: Int): Boolean =
        level >= sexualFinalStartLevel
}

data class LifetimeStats(
    val gamesStarted: Int = 0,
    val gamesCompleted: Int = 0,
    val towersFallen: Int = 0,
    val blocksPlaced: Int = 0,
    val jokersUsed: Int = 0,
    val rejectedChallenges: Int = 0,
    val freePlayDraws: Int = 0,
)

data class GameUiState(
    val game: GameState = GameState(),
    val settings: AppSettings = AppSettings(),
    val stats: LifetimeStats = LifetimeStats(),
    val challenges: List<Challenge> = emptyList(),
    val currentChallenge: Challenge? = null,
    val freePlayChallenge: Challenge? = null,
    val customChallenges: List<Challenge> = emptyList(),
    val loaded: Boolean = false,
)
