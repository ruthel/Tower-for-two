package com.crabscode.towerfortwo.model

object SexPositionCatalog {
    private val positions = mapOf(
        SexualPractice.CARESSES_INTIMES to listOf(
            "Face à face assis",
            "Côte à côte",
            "En cuillère",
            "Un partenaire allongé, l'autre à côté",
            "Debout face à face",
        ),
        SexualPractice.MASTURBATION to listOf(
            "Côte à côte",
            "Face à face assis",
            "Un partenaire allongé, l'autre à côté",
            "En cuillère",
            "Debout / assis face à face",
        ),
        SexualPractice.MASTURBATION_MUTUELLE to listOf(
            "Côte à côte",
            "Face à face assis",
            "Allongés face à face",
            "En cuillère",
            "Assis l'un contre l'autre",
        ),
        SexualPractice.SEXE_ORAL to listOf(
            "Partenaire receveur allongé",
            "Partenaire receveur assis",
            "Sur le côté",
            "69 côte à côte",
            "À genoux devant le/la partenaire",
        ),
        SexualPractice.PENETRATION to listOf(
            "Missionnaire",
            "En cuillère",
            "Partenaire au-dessus",
            "Face à face assis",
            "Côte à côte",
            "Debout face à face",
        ),
    )

    fun random(practice: SexualPractice?, exclude: String? = null): String? {
        if (practice == null || practice == SexualPractice.CHOICE) return null
        val pool = positions[practice].orEmpty()
        if (pool.isEmpty()) return null
        return pool.filterNot { it == exclude }.ifEmpty { pool }.random()
    }
}
