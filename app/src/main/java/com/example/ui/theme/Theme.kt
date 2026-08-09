package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WTrackerDarkColorScheme = darkColorScheme(
    primary = MutedGold,
    onPrimary = ObsidianBackground,
    primaryContainer = CarbonMistElevated,
    onPrimaryContainer = MutedGold,
    secondary = StateActiveGreen,
    onSecondary = TextPrimary,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = GraphiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = CarbonMistElevated,
    onSurfaceVariant = TextSecondary,
    outline = SteelLineBorder,
    error = StateConflictRed,
    onError = TextPrimary
)

@Composable
fun WTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WTrackerDarkColorScheme,
        typography = Typography,
        content = content
    )
}
