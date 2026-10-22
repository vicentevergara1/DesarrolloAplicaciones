package com.duoc.notix.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.notix.R
import com.duoc.notix.domain.ProcedureType
import com.duoc.notix.domain.User
import com.duoc.notix.ui.procedures.ProceduresUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun ProceduresScreen(
    user: User,
    state: ProceduresUiState,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (ProcedureType) -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Notix", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onLogout, enabled = !state.saving) { Text("Cerrar sesión") }
            }
        }
        item {
            Text("Hola, ${user.displayName}", style = MaterialTheme.typography.titleMedium)
            Text("Mis trámites", style = MaterialTheme.typography.headlineLarge)
            Text("Borradores de demostración guardados en tu dispositivo.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Button(onClick = onCreate, enabled = !state.loading && !state.saving, modifier = Modifier.fillMaxWidth()) {
                Text("Nuevo trámite de prueba")
            }
        }
        if (state.loading) {
            item { CircularProgressIndicator() }
        }
        if (state.message != null && !state.showCreateDialog) {
            item {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("Reintentar") }
            }
        }
        if (!state.loading && state.items.isEmpty() && state.message == null) {
            item {
                Card(Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(painterResource(R.drawable.ic_notix), contentDescription = null, modifier = Modifier.size(72.dp))
                        Text("Aún no tienes trámites", style = MaterialTheme.typography.titleLarge)
                        Text("Crea tu primer borrador con un formato ficticio. Podrás encontrarlo aquí al volver a iniciar sesión.")
                    }
                }
            }
        }
        items(state.items, key = { it.id }) { procedure ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TRÁMITE #${procedure.id}", style = MaterialTheme.typography.labelSmall)
                    Text(procedure.title, style = MaterialTheme.typography.titleMedium)
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text("Borrador", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(procedure.createdAt)), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Text("MVP académico: estos borradores todavía no se envían a un notario.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(24.dp))
        }
    }
    if (state.showCreateDialog) {
        var selectedName by rememberSaveable { mutableStateOf(ProcedureType.DECLARATION.name) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nuevo borrador") },
            text = {
                Column {
                    Text("Selecciona un formato de prueba.")
                    ProcedureType.entries.forEach { type ->
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = selectedName == type.name, enabled = !state.saving,
                                role = Role.RadioButton, onClick = { selectedName = type.name },
                            ).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selectedName == type.name, onClick = null, enabled = !state.saving)
                            Text(type.label, Modifier.padding(start = 8.dp))
                        }
                    }
                    state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = { onSave(ProcedureType.valueOf(selectedName)) }, enabled = !state.saving) {
                    Text(if (state.saving) "Guardando…" else "Guardar borrador")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss, enabled = !state.saving) { Text("Cancelar") } },
        )
    }
}
