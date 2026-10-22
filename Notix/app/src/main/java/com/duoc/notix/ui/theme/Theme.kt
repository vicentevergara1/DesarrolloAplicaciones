package com.duoc.notix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = NavyLight, secondary = GoldLight, tertiary = TealLight,
    background = Color(0xFF101D25), surface = Color(0xFF101D25),
    surfaceContainerHighest = Color(0xFF283740),
)
private val LightColors = lightColorScheme(
    primary = Navy, secondary = Gold, tertiary = Teal,
    background = Color(0xFFFAF8F4), surface = Color(0xFFFAF8F4),
    surfaceContainerHighest = Color(0xFFECE7DE),
    secondaryContainer = Color(0xFFF3E3C5), onSecondaryContainer = Color(0xFF463311),
)

@Composable
fun NotixTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography, content = content)
}
