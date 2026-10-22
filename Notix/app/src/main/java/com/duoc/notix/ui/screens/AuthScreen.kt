package com.duoc.notix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.duoc.notix.R
import com.duoc.notix.ui.auth.AuthField
import com.duoc.notix.ui.auth.AuthMode
import com.duoc.notix.ui.auth.AuthUiState

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEdit: (AuthField, String) -> Unit,
    onSubmit: () -> Unit,
    onSwitchMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val registering = state.mode == AuthMode.REGISTER
    var showPassword by remember(state.mode) { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.ic_notix), contentDescription = null, modifier = Modifier.size(56.dp))
            Column {
                Text("Notix", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Tus trámites, más claros", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("DEMOSTRACIÓN ACADÉMICA · NOTARIAX", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(if (registering) "Crea tu cuenta de prueba" else "Bienvenido de nuevo", style = MaterialTheme.typography.headlineSmall)
        Text("Usa solo datos ficticios. La información se guarda en este dispositivo.", style = MaterialTheme.typography.bodyMedium)
        if (registering) {
            AuthTextField("Nombre de prueba", state.name, state.errors.name, !state.loading,
                onChange = { onEdit(AuthField.NAME, it) })
        }
        AuthTextField("Correo electrónico ficticio", state.email, state.errors.email, !state.loading,
            keyboardType = KeyboardType.Email, onChange = { onEdit(AuthField.EMAIL, it) })
        AuthTextField("Contraseña", state.password, state.errors.password, !state.loading,
            keyboardType = KeyboardType.Password, password = !showPassword,
            trailing = {
                TextButton(onClick = { showPassword = !showPassword }, enabled = !state.loading) {
                    Text(if (showPassword) "Ocultar" else "Mostrar")
                }
            }, onChange = { onEdit(AuthField.PASSWORD, it) })
        if (registering) {
            AuthTextField("Confirmar contraseña", state.confirmation, state.errors.confirmation, !state.loading,
                keyboardType = KeyboardType.Password, password = !showPassword,
                onChange = { onEdit(AuthField.CONFIRMATION, it) })
            Text("La contraseña debe tener entre 8 y 128 caracteres.", style = MaterialTheme.typography.bodySmall)
        }
        AnimatedVisibility(state.message != null) {
            Text(state.message.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = onSubmit, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            if (state.loading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Text("  Procesando…")
            } else {
                Text(if (registering) "Crear cuenta" else "Iniciar sesión")
            }
        }
        TextButton(onClick = onSwitchMode, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (registering) "Ya tengo una cuenta" else "Crear una cuenta de prueba")
        }
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    error: String?,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, isError = error != null,
        supportingText = if (error == null) null else { { Text(error) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = trailing,
    )
}
