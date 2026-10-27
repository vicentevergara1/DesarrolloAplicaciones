package com.duoc.notix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Brand, secondary = Accent, onPrimary = Color.White,
    background = Color(0xFFEEF4F1), surface = Color(0xFFEEF4F1),
    onBackground = Ink, onSurface = Ink,
    secondaryContainer = Color(0xFFECE8FB), onSecondaryContainer = Color(0xFF39217F),
    surfaceContainerHighest = Color(0xFFE1EBE6),
)
private val DarkColors = darkColorScheme(
    primary = BrandLight, secondary = AccentLight,
    background = Color(0xFF0E1917), surface = Color(0xFF0E1917),
    surfaceContainerHighest = Color(0xFF24372F),
)

@Composable
fun NotixTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography, content = content)
}
