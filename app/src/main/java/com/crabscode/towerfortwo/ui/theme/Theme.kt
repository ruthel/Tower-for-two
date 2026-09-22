package com.crabscode.towerfortwo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.crabscode.towerfortwo.R

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

private val InterFontFamily = FontFamily(
    Font(R.font.inter_variable, weight = FontWeight.Normal),
    Font(R.font.inter_variable, weight = FontWeight.Medium),
    Font(R.font.inter_variable, weight = FontWeight.SemiBold),
    Font(R.font.inter_variable, weight = FontWeight.Bold),
    Font(R.font.inter_variable, weight = FontWeight.Black),
)

private val DefaultTypography = Typography()

private fun TextStyle.withInter() = copy(fontFamily = InterFontFamily)

private val InterTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.withInter(),
    displayMedium = DefaultTypography.displayMedium.withInter(),
    displaySmall = DefaultTypography.displaySmall.withInter(),
    headlineLarge = DefaultTypography.headlineLarge.withInter(),
    headlineMedium = DefaultTypography.headlineMedium.withInter(),
    headlineSmall = DefaultTypography.headlineSmall.withInter(),
    titleLarge = DefaultTypography.titleLarge.withInter(),
    titleMedium = DefaultTypography.titleMedium.withInter(),
    titleSmall = DefaultTypography.titleSmall.withInter(),
    bodyLarge = DefaultTypography.bodyLarge.withInter(),
    bodyMedium = DefaultTypography.bodyMedium.withInter(),
    bodySmall = DefaultTypography.bodySmall.withInter(),
    labelLarge = DefaultTypography.labelLarge.withInter(),
    labelMedium = DefaultTypography.labelMedium.withInter(),
    labelSmall = DefaultTypography.labelSmall.withInter(),
)

@Composable
fun TowerForTwoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = InterTypography,
        content = content,
    )
}
