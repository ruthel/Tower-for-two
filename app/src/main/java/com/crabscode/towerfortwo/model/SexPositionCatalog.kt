package com.crabscode.towerfortwo.model

object SexPositionCatalog {
    private val positions = listOf(
        SexPositionSpec(
            id = "caresses_face_to_face",
            practice = SexualPractice.CARESSES_INTIMES,
            label = "Face à face assis",
            setup = "{giver} et {receiver} s'assoient face à face, proches l'un de l'autre.",
            sticker = SexStickerPose.FACE_TO_FACE,
        ),
        SexPositionSpec(
            id = "caresses_side_by_side",
            practice = SexualPractice.CARESSES_INTIMES,
            label = "Côte à côte",
            setup = "{giver} et {receiver} s'allongent côte à côte, tournés l'un vers l'autre.",
            sticker = SexStickerPose.SIDE_BY_SIDE,
        ),
        SexPositionSpec(
            id = "caresses_spoon",
            practice = SexualPractice.CARESSES_INTIMES,
            label = "En cuillère",
            setup = "{giver} se place derrière {receiver}, tous les deux sur le côté.",
            sticker = SexStickerPose.SPOON,
        ),
        SexPositionSpec(
            id = "caresses_standing",
            practice = SexualPractice.CARESSES_INTIMES,
            label = "Debout face à face",
            setup = "{giver} et {receiver} restent debout face à face, très proches.",
            sticker = SexStickerPose.STANDING_FACE_TO_FACE,
            standing = true,
            minLevel = 8,
        ),

        SexPositionSpec(
            id = "masturbation_receiver_lying",
            practice = SexualPractice.MASTURBATION,
            label = "Receveur allongé",
            setup = "{receiver} s'allonge confortablement pendant que {giver} se place à côté.",
            sticker = SexStickerPose.RECEIVER_LYING,
        ),
        SexPositionSpec(
            id = "masturbation_side_by_side",
            practice = SexualPractice.MASTURBATION,
            label = "Côte à côte",
            setup = "{giver} et {receiver} s'allongent côte à côte.",
            sticker = SexStickerPose.SIDE_BY_SIDE,
        ),
        SexPositionSpec(
            id = "masturbation_face_to_face",
            practice = SexualPractice.MASTURBATION,
            label = "Face à face assis",
            setup = "{giver} et {receiver} s'assoient face à face.",
            sticker = SexStickerPose.FACE_TO_FACE,
        ),
        SexPositionSpec(
            id = "masturbation_receiver_seated",
            practice = SexualPractice.MASTURBATION,
            label = "Receveur assis",
            setup = "{receiver} s'assoit confortablement et {giver} se place face à lui/elle.",
            sticker = SexStickerPose.RECEIVER_SEATED,
        ),

        SexPositionSpec(
            id = "mutual_side_by_side",
            practice = SexualPractice.MASTURBATION_MUTUELLE,
            label = "Côte à côte",
            setup = "{p1} et {p2} s'allongent côte à côte, suffisamment proches pour se toucher facilement.",
            sticker = SexStickerPose.SIDE_BY_SIDE,
        ),
        SexPositionSpec(
            id = "mutual_face_to_face",
            practice = SexualPractice.MASTURBATION_MUTUELLE,
            label = "Face à face assis",
            setup = "{p1} et {p2} s'assoient face à face.",
            sticker = SexStickerPose.SEATED_EMBRACE,
        ),
        SexPositionSpec(
            id = "mutual_spoon",
            practice = SexualPractice.MASTURBATION_MUTUELLE,
            label = "En cuillère",
            setup = "{p1} et {p2} se placent sur le côté, l'un derrière l'autre.",
            sticker = SexStickerPose.SPOON,
        ),
        SexPositionSpec(
            id = "mutual_lying_face",
            practice = SexualPractice.MASTURBATION_MUTUELLE,
            label = "Allongés face à face",
            setup = "{p1} et {p2} s'allongent sur le côté, face à face.",
            sticker = SexStickerPose.FACE_TO_FACE,
        ),

        SexPositionSpec(
            id = "oral_receiver_lying",
            practice = SexualPractice.SEXE_ORAL,
            label = "Receveur allongé",
            setup = "{receiver} s'allonge confortablement pendant que {giver} se place devant lui/elle.",
            sticker = SexStickerPose.RECEIVER_LYING,
        ),
        SexPositionSpec(
            id = "oral_receiver_seated",
            practice = SexualPractice.SEXE_ORAL,
            label = "Receveur assis",
            setup = "{receiver} s'assoit au bord du lit ou du canapé et {giver} se place devant.",
            sticker = SexStickerPose.RECEIVER_SEATED,
        ),
        SexPositionSpec(
            id = "oral_kneeling",
            practice = SexualPractice.SEXE_ORAL,
            label = "À genoux devant le partenaire",
            setup = "{receiver} reste debout ou assis tandis que {giver} se place à genoux devant.",
            sticker = SexStickerPose.KNEELING,
            standing = true,
            minLevel = 9,
        ),
        SexPositionSpec(
            id = "oral_side",
            practice = SexualPractice.SEXE_ORAL,
            label = "Sur le côté",
            setup = "{receiver} s'allonge sur le côté et {giver} se place confortablement face à lui/elle.",
            sticker = SexStickerPose.SIDE_BY_SIDE,
        ),

        SexPositionSpec(
            id = "penetration_missionary",
            practice = SexualPractice.PENETRATION,
            label = "Face à face allongés",
            setup = "{receiver} s'allonge sur le dos et {giver} se place face à lui/elle.",
            sticker = SexStickerPose.RECEIVER_LYING,
        ),
        SexPositionSpec(
            id = "penetration_spoon",
            practice = SexualPractice.PENETRATION,
            label = "En cuillère",
            setup = "{giver} se place derrière {receiver}, tous les deux sur le côté.",
            sticker = SexStickerPose.SPOON,
        ),
        SexPositionSpec(
            id = "penetration_partner_on_top",
            practice = SexualPractice.PENETRATION,
            label = "Partenaire receveur au-dessus",
            setup = "{giver} s'allonge et {receiver} se place au-dessus pour contrôler davantage le mouvement.",
            sticker = SexStickerPose.PARTNER_ON_TOP,
        ),
        SexPositionSpec(
            id = "penetration_seated",
            practice = SexualPractice.PENETRATION,
            label = "Face à face assis",
            setup = "{giver} s'assoit de façon stable et {receiver} se place face à lui/elle.",
            sticker = SexStickerPose.SEATED_EMBRACE,
        ),
        SexPositionSpec(
            id = "penetration_standing",
            practice = SexualPractice.PENETRATION,
            label = "Debout face à face",
            setup = "{giver} et {receiver} se placent debout face à face avec un appui stable à proximité.",
            sticker = SexStickerPose.STANDING_FACE_TO_FACE,
            standing = true,
        ),
    )

    fun position(idOrLabel: String?): SexPositionSpec? {
        if (idOrLabel == null) return null
        return positions.firstOrNull { it.id == idOrLabel || it.label == idOrLabel }
    }

    fun eligiblePractices(level: Int, settings: AppSettings): List<SexualPractice> {
        val selected = settings.allowedSexualPractices.ifEmpty { SexualPractice.playable.toSet() }
        return selected
            .filter { it != SexualPractice.CHOICE && it.minLevel <= level }
            .filter { practice ->
                positions.any {
                    it.practice == practice &&
                        it.minLevel <= level &&
                        (settings.allowStandingSexPositions || !it.standing)
                }
            }
    }

    fun positionsFor(practice: SexualPractice, level: Int, settings: AppSettings): List<SexPositionSpec> =
        positions.filter {
            it.practice == practice &&
                it.minLevel <= level &&
                (settings.allowStandingSexPositions || !it.standing)
        }

    fun resolve(
        challenge: Challenge,
        game: GameState,
        settings: AppSettings,
        actor: Int,
    ): ResolvedSexualAction? {
        if (!challenge.sexual) return null

        val eligiblePractices = eligiblePractices(challenge.level, settings)
        if (eligiblePractices.isEmpty()) return null

        val requested = challenge.sexualPractice?.takeUnless { it == SexualPractice.CHOICE }
        val basePracticePool = if (requested != null && requested in eligiblePractices) {
            listOf(requested)
        } else {
            eligiblePractices
        }
        val weightedPracticePool = basePracticePool.flatMap { practice ->
            if (practice in settings.preferredSexualPractices) {
                listOf(practice, practice, practice)
            } else {
                listOf(practice)
            }
        }

        val recentPractices = game.recentSexualPractices.takeLast(2).toSet()
        val practice = weightedPracticePool.filterNot { it.name in recentPractices }
            .ifEmpty { weightedPracticePool }
            .random()

        val pool = positionsFor(practice, challenge.level, settings)
        if (pool.isEmpty()) return null

        val recentPositions = game.recentSexPositionIds.takeLast(4).toSet()
        val recentFamilies = game.recentSexPositionFamilies.takeLast(2).toSet()
        val position = pool
            .filterNot { it.id in recentPositions || it.family.name in recentFamilies }
            .ifEmpty { pool.filterNot { it.id in recentPositions } }
            .ifEmpty { pool.filterNot { it.family.name in recentFamilies } }
            .ifEmpty { pool }
            .random()

        val duration = durationsFor(practice, challenge.level).random()
        val mutual = practice == SexualPractice.MASTURBATION_MUTUELLE

        return ResolvedSexualAction(
            practice = practice,
            positionId = position.id,
            durationSec = duration,
            giverPlayerIndex = if (mutual) null else actor.coerceIn(0, 1),
            receiverPlayerIndex = if (mutual) null else 1 - actor.coerceIn(0, 1),
        )
    }

    fun reroll(
        current: ResolvedSexualAction,
        level: Int,
        game: GameState,
        settings: AppSettings,
    ): ResolvedSexualAction {
        val pool = positionsFor(current.practice, level, settings)
        val recent = (game.recentSexPositionIds.takeLast(4) + current.positionId).toSet()
        val currentFamily = position(current.positionId)?.family?.name
        val recentFamilies = (game.recentSexPositionFamilies.takeLast(2) + listOfNotNull(currentFamily)).toSet()
        val next = pool
            .filterNot { it.id in recent || it.family.name in recentFamilies }
            .ifEmpty { pool.filterNot { it.id in recent } }
            .ifEmpty { pool.filterNot { it.id == current.positionId } }
            .ifEmpty { pool }
            .random()

        return current.copy(
            positionId = next.id,
            durationSec = durationsFor(current.practice, level).random(),
        )
    }

    fun instruction(action: ResolvedSexualAction, game: GameState): String {
        val position = position(action.positionId) ?: return ""
        val p1 = game.player1
        val p2 = game.player2
        val giver = action.giverPlayerIndex?.let(game::playerName) ?: p1
        val receiver = action.receiverPlayerIndex?.let(game::playerName) ?: p2
        val setup = position.setup
            .replace("{giver}", giver)
            .replace("{receiver}", receiver)
            .replace("{p1}", p1)
            .replace("{p2}", p2)

        val duration = durationLabel(action.durationSec)
        val actionText = when (action.practice) {
            SexualPractice.CARESSES_INTIMES ->
                "$giver commence les caresses intimes sur $receiver pendant $duration. $receiver guide les zones, la pression et le rythme."
            SexualPractice.MASTURBATION ->
                "$giver masturbe $receiver pendant $duration. $receiver guide le rythme et peut demander de ralentir, changer ou arrêter immédiatement."
            SexualPractice.MASTURBATION_MUTUELLE ->
                "$p1 et $p2 se stimulent mutuellement pendant $duration, chacun guidant clairement ce qui lui convient."
            SexualPractice.SEXE_ORAL ->
                "$giver donne du sexe oral à $receiver pendant $duration. $receiver guide le rythme et peut interrompre l'action à tout moment."
            SexualPractice.PENETRATION ->
                "$giver et $receiver commencent la pénétration lentement pendant $duration dans cette position. $receiver peut ajuster le rythme, le mouvement ou demander l'arrêt à tout moment."
            SexualPractice.CHOICE -> ""
        }
        return "$setup $actionText"
    }

    private fun durationsFor(practice: SexualPractice, level: Int): List<Int> = when (practice) {
        SexualPractice.CARESSES_INTIMES -> if (level >= 9) listOf(60, 90) else listOf(45, 60)
        SexualPractice.MASTURBATION -> if (level >= 9) listOf(60, 90) else listOf(45, 60)
        SexualPractice.MASTURBATION_MUTUELLE -> listOf(60, 90)
        SexualPractice.SEXE_ORAL -> if (level >= 9) listOf(60, 90) else listOf(45, 60)
        SexualPractice.PENETRATION -> listOf(60, 90, 120)
        SexualPractice.CHOICE -> listOf(60)
    }

    fun durationLabel(seconds: Int): String =
        if (seconds >= 60 && seconds % 60 == 0) {
            val minutes = seconds / 60
            if (minutes == 1) "1 minute" else "$minutes minutes"
        } else {
            "$seconds secondes"
        }
}
