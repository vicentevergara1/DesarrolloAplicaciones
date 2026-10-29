package com.duoc.notix.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.duoc.notix.ui.theme.NotixColors

@Composable
fun notixTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    // Texto que escribe el usuario
    focusedTextColor = NotixColors.Tinta,
    unfocusedTextColor = NotixColors.Tinta,
    errorTextColor = NotixColors.Tinta,

    // Etiqueta ("Correo", "Ingresa tu clave")
    focusedLabelColor = NotixColors.Brand,
    unfocusedLabelColor = NotixColors.Gris,
    errorLabelColor = MaterialTheme.colorScheme.error,

    // Placeholder
    focusedPlaceholderColor = NotixColors.Gris,
    unfocusedPlaceholderColor = NotixColors.Gris,

    // Bordes
    focusedBorderColor = NotixColors.Brand,
    unfocusedBorderColor = NotixColors.Borde,
    errorBorderColor = MaterialTheme.colorScheme.error,

    // Cursor y fondo
    cursorColor = NotixColors.Brand,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,

    // Íconos y texto de ayuda
    focusedTrailingIconColor = NotixColors.Gris,
    unfocusedTrailingIconColor = NotixColors.Gris,
    focusedSupportingTextColor = NotixColors.Gris,
    unfocusedSupportingTextColor = NotixColors.Gris,
    errorSupportingTextColor = MaterialTheme.colorScheme.error
)
