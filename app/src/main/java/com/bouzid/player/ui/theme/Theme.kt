package com.bouzid.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BouzidColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = BackgroundDark,
    primaryContainer = GoldDark,
    secondary = GoldLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = BackgroundDark
)

@Composable
fun BouzidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BouzidColorScheme,
        typography = BouzidTypography,
        content = content
    )
}
