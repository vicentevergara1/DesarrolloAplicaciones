package com.duoc.notix.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.duoc.notix.ui.theme.NotixColors

@Composable
fun notixTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(

    focusedTextColor = NotixColors.Tinta,
    unfocusedTextColor = NotixColors.Tinta,
    errorTextColor = NotixColors.Tinta,

    focusedLabelColor = NotixColors.Brand,
    unfocusedLabelColor = NotixColors.Gris,
    errorLabelColor = MaterialTheme.colorScheme.error,

    focusedPlaceholderColor = NotixColors.Gris,
    unfocusedPlaceholderColor = NotixColors.Gris,

    focusedBorderColor = NotixColors.Brand,
    unfocusedBorderColor = NotixColors.Borde,
    errorBorderColor = MaterialTheme.colorScheme.error,

    cursorColor = NotixColors.Brand,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    errorContainerColor = Color.White,

    focusedTrailingIconColor = NotixColors.Gris,
    unfocusedTrailingIconColor = NotixColors.Gris,
    focusedSupportingTextColor = NotixColors.Gris,
    unfocusedSupportingTextColor = NotixColors.Gris,
    errorSupportingTextColor = MaterialTheme.colorScheme.error
)
