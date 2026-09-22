package com.crabscode.towerfortwo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF5C8A),
    onPrimary = Color(0xFF2B0814),
    primaryContainer = Color(0xFF5B1B35),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFD8A5FF),
    secondaryContainer = Color(0xFF42245A),
    tertiary = Color(0xFFFFB0C8),
    background = Color(0xFF0D0B0F),
    onBackground = Color(0xFFF6EDF3),
    surface = Color(0xFF171219),
    onSurface = Color(0xFFF6EDF3),
    surfaceVariant = Color(0xFF2A222C),
    onSurfaceVariant = Color(0xFFD9CBD4),
    error = Color(0xFFFFB4AB),
)

@Composable
fun TowerForTwoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}