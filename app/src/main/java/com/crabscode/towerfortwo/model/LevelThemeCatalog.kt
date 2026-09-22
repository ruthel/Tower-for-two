package com.crabscode.towerfortwo.model

data class LevelTheme(
    val level: Int,
    val title: String,
    val subtitle: String,
)

object LevelThemeCatalog {
    private val themes = listOf(
        LevelTheme(1, "Briser la glace", "On commence doucement."),
        LevelTheme(2, "Se rapprocher", "La distance diminue."),
        LevelTheme(3, "Séduction", "Le jeu devient plus personnel."),
        LevelTheme(4, "Contact", "Le toucher prend plus de place."),
        LevelTheme(5, "Désir", "La tension monte."),
        LevelTheme(6, "Tension", "On ralentit pour mieux faire durer."),
        LevelTheme(7, "Intimité", "Plus proche, plus assumé."),
        LevelTheme(8, "Montée", "Le jeu entre dans sa dernière phase."),
        LevelTheme(9, "Très chaud", "La retenue devient minimale."),
        LevelTheme(10, "Final", "Le dernier niveau."),
    )

    fun forLevel(level: Int): LevelTheme =
        themes[(level.coerceIn(1, 10)) - 1]
}
