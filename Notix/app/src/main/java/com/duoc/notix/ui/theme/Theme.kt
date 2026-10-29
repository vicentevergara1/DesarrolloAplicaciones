package com.duoc.notix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Paleta = lightColorScheme(
    primary = NotixColors.Brand, onPrimary = Color.White,
    background = NotixColors.Fondo, onBackground = NotixColors.Tinta,
    surface = Color.White, onSurface = NotixColors.Tinta,
    onSurfaceVariant = NotixColors.Gris
)

@Composable
fun NotixTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Paleta, typography = Typography, content = content)
}
