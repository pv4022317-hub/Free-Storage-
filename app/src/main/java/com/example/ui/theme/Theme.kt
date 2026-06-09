package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = NeonPurple,
    tertiary = NeonMagenta,
    background = CosmicDeepSpace,
    surface = CosmicSoot,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = CosmicNavyOutline,
    outline = CosmicNavyOutline
)

private val LightColorScheme = lightColorScheme(
    primary = FrostPill,
    secondary = NeonPurple,
    tertiary = NeonMagenta,
    background = FrostWhite,
    surface = FrostCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = FrostOutline,
    outline = FrostOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
