package com.duoc.notix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.duoc.notix.R
import com.duoc.notix.domain.*
import com.duoc.notix.ui.auth.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: AuthUiState,
    onEdit: (AuthField, String) -> Unit,
    onEditProfile: (String, String) -> Unit,
    onType: (AccountType) -> Unit,
    onExample: () -> Unit,
    onSubmit: () -> Unit,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val registering = state.mode == AuthMode.REGISTER
    var visible by remember(state.mode, state.accountType) { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (registering) TextButton(onClick = onSwitch, enabled = !state.loading) { Text("‹ Volver") }
            Image(painterResource(R.drawable.ic_notix), null, Modifier.size(48.dp))
            Text("NotariaX", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(if (registering) "Crea tu cuenta" else "Ingresa a NotariaX",
            style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
            Text("Demostración académica. Usa exclusivamente nombres, identificadores, documentos y contraseñas ficticios.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
        }
        if (registering) {
            Text("Tipo de cuenta de prueba", fontWeight = FontWeight.SemiBold)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                AccountType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.accountType == type, onClick = { onType(type) }, enabled = !state.loading,
                        shape = SegmentedButtonDefaults.itemShape(index, AccountType.entries.size),
                    ) { Text(type.label) }
                }
            }
            TextButton(onClick = onExample, enabled = !state.loading) { Text("Completar ejemplo ficticio") }
            if (state.accountType == AccountType.NOTARY) {
                Text("Este perfil representa una revisión simulada y no autoriza trámites reales.", style = MaterialTheme.typography.bodySmall)
            }
            RegistrationRules.fieldsFor(state.accountType).forEach { field ->
                val keyboard = when (field.kind) {
                    FieldKind.TEXT -> KeyboardType.Text
                    FieldKind.EMAIL -> KeyboardType.Email
                    FieldKind.NUMBER -> KeyboardType.Number
                    FieldKind.PHONE -> KeyboardType.Phone
                    FieldKind.ASCII -> KeyboardType.Ascii
                }
                FormField(field.title, state.values[field.key].orEmpty(), state.errors[field.key], !state.loading,
                    keyboard = keyboard, placeholder = field.example,
                    onChange = { onEditProfile(field.key, it) })
            }
        } else {
            FormField("Correo electrónico ficticio", state.email, state.errors["correo"], !state.loading,
                keyboard = KeyboardType.Email, onChange = { onEdit(AuthField.EMAIL, it) })
        }
        FormField("Contraseña", state.password, state.errors["password"], !state.loading,
            keyboard = KeyboardType.Password, hide = !visible,
            trailing = { TextButton(onClick = { visible = !visible }, enabled = !state.loading) { Text(if (visible) "Ocultar" else "Mostrar") } },
            onChange = { onEdit(AuthField.PASSWORD, it) })
        if (registering) {
            FormField("Confirmar contraseña", state.confirmation, state.errors["confirmation"], !state.loading,
                keyboard = KeyboardType.Password, hide = !visible, onChange = { onEdit(AuthField.CONFIRMATION, it) })
            Text("Entre 8 y 128 caracteres. No uses una contraseña real.", style = MaterialTheme.typography.bodySmall)
        }
        AnimatedVisibility(state.message != null) {
            Text(state.message.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = onSubmit, enabled = !state.loading, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Text(if (state.loading) "  Procesando…" else if (registering) "Registrarse" else "Iniciar sesión")
        }
        TextButton(onClick = onSwitch, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (registering) "¿Ya tienes cuenta? Ingresar" else "Crear una cuenta de prueba")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun FormField(
    title: String, value: String, error: String?, enabled: Boolean,
    keyboard: KeyboardType = KeyboardType.Text, placeholder: String = "", hide: Boolean = false,
    trailing: (@Composable () -> Unit)? = null, onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(title) },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
        placeholder = if (placeholder.isEmpty()) null else { { Text(placeholder) } },
        isError = error != null, supportingText = if (error == null) null else { { Text(error) } },
        visualTransformation = if (hide) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        trailingIcon = trailing,
    )
}
