package com.duoc.notix.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duoc.notix.R
import com.duoc.notix.domain.*
import com.duoc.notix.ui.procedures.*
import java.text.DateFormat
import java.util.Date

private fun date(time: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(time))

@Composable
fun StatusBadge(status: ProcedureStatus) {
    // La transición anima cambios reales del trámite, recibidos desde el Flow de Room.
    AnimatedContent(targetState = status, label = "Cambio de estado del trámite") { current ->
        val background = when (current) {
            ProcedureStatus.DRAFT -> Color(0xFFE5E8EC)
            ProcedureStatus.UNDER_REVIEW -> Color(0xFFDCEBFA)
            ProcedureStatus.OBSERVED -> Color(0xFFFFE9C5)
            ProcedureStatus.COMPLETED -> Color(0xFFD8EEE2)
        }
        Surface(color = background, contentColor = Color(0xFF17352D), shape = MaterialTheme.shapes.small) {
            Text(current.label, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ProceduresScreen(
    user: User, state: ProceduresUiState, onOpen: (Long) -> Unit, onCreate: () -> Unit,
    onLogout: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier,
) {
    val reviewer = user.type == AccountType.NOTARY
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_notix), null, Modifier.size(44.dp))
                Text("NotariaX", Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onLogout, enabled = !state.busy) { Text("Cerrar sesión") }
            }
        }
        item {
            Text("Hola, " + user.displayName, style = MaterialTheme.typography.titleMedium)
            Text(if (reviewer) "Bandeja del notario" else "Mis trámites", style = MaterialTheme.typography.headlineLarge)
            Text(user.type.label + " · Perfil de prueba", color = MaterialTheme.colorScheme.primary)
            Text(if (reviewer) "Revisa documentos ficticios y registra observaciones o finaliza la simulación."
                else "Crea, corrige y consulta tus solicitudes de demostración.")
        }
        if (!reviewer) item {
            Button(onClick = onCreate, enabled = !state.loading && !state.busy, modifier = Modifier.fillMaxWidth()) {
                Text("Nuevo trámite de prueba")
            }
        }
        if (state.loading) item { CircularProgressIndicator() }
        state.message?.let { message -> item {
            Text(message, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onRetry) { Text("Actualizar listado") }
        } }
        if (!state.loading && state.items.isEmpty()) item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painterResource(R.drawable.ic_notix), null, Modifier.size(64.dp))
                    Text(if (reviewer) "No hay trámites enviados" else "Aún no tienes trámites", style = MaterialTheme.typography.titleLarge)
                    Text(if (reviewer) "Ingresa como solicitante, crea un trámite y envíalo para que aparezca aquí."
                        else "Elige uno de los formatos ficticios para comenzar.")
                }
            }
        }
        items(state.items, key = { it.id }) { item ->
            Card(onClick = { onOpen(item.id) }, modifier = Modifier.fillMaxWidth(), enabled = !state.busy) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TRÁMITE #" + item.id, style = MaterialTheme.typography.labelSmall)
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    if (reviewer) Text("Solicitante: " + item.ownerName)
                    StatusBadge(item.status)
                    Text(date(item.createdAt), style = MaterialTheme.typography.bodySmall)
                    Text("Ver documentos e historial", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text("MVP académico con datos ficticios. No realiza trámites notariales reales.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun ProcedureDetailScreen(
    user: User, state: ProceduresUiState, onBack: () -> Unit, onForm: (ProcedureForm) -> Unit,
    onPick: () -> Unit, onView: (Document) -> Unit, onRemove: (Document) -> Unit,
    onSave: () -> Unit, onSubmit: () -> Unit, onReturn: () -> Unit, onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = state.selected
    if (item == null) {
        Column(modifier.padding(20.dp)) {
            TextButton(onClick = onBack) { Text("Volver al listado") }
            if (state.loading || state.busy) CircularProgressIndicator()
            else Text("El trámite no está disponible. Actualiza el listado.")
        }
        return
    }
    val editable = user.type != AccountType.NOTARY && item.userId == user.id && item.status.editable
    Column(modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, enabled = !state.busy) {
            Text(if (user.type == AccountType.NOTARY) "‹ Volver a la bandeja" else "‹ Volver a mis trámites")
        }
        Text("TRÁMITE #" + item.id, style = MaterialTheme.typography.labelLarge)
        Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        StatusBadge(item.status)
        Text("Demostración académica · Sin validez legal", style = MaterialTheme.typography.bodySmall)

        if (item.status == ProcedureStatus.OBSERVED) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Correcciones solicitadas", style = MaterialTheme.typography.titleMedium)
                    Text(item.observation.orEmpty())
                    if (editable) Text("Actualiza los antecedentes o documentos y reenvía la solicitud.")
                }
            }
        }

        Text("Antecedentes ficticios", style = MaterialTheme.typography.titleLarge)
        if (editable) {
            OutlinedTextField(
                value = state.form.details, onValueChange = { onForm(state.form.copy(details = it)) },
                label = { Text("Descripción del trámite de prueba") }, minLines = 3,
                modifier = Modifier.fillMaxWidth(), enabled = !state.busy,
                isError = state.errors["details"] != null,
                supportingText = { Text(state.errors["details"] ?: "Entre 10 y 2000 caracteres para enviar.") },
            )
        } else Text(item.form.details)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Solicitante de prueba", style = MaterialTheme.typography.titleMedium)
                Text(item.ownerName + " · " + item.ownerType.label)
                Text(item.ownerEmail)
                if (user.type == AccountType.NOTARY) {
                    RegistrationRules.fieldsFor(item.ownerType)
                        .filter { it.key != item.ownerType.nameKey && it.key != "correo" }.forEach { field ->
                            item.ownerProfile[field.key]?.let { Text(field.title + ": " + it, style = MaterialTheme.typography.bodySmall) }
                        }
                }
            }
        }

        Text("Documentos de prueba", style = MaterialTheme.typography.titleLarge)
        Text("Hasta cinco documentos. Los formatos incluidos contienen texto ficticio; los archivos adjuntos se copian al dispositivo.",
            style = MaterialTheme.typography.bodySmall)
        if (editable) {
            OutlinedButton(onClick = onPick, enabled = !state.busy && item.documents.size < 5, modifier = Modifier.fillMaxWidth()) {
                Text("Adjuntar archivo ficticio")
            }
            Text("Selector nativo de Android · PDF, TXT, PNG o JPG · máximo 5 MB.", style = MaterialTheme.typography.bodySmall)
        }
        item.documents.forEach { document ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(document.name, fontWeight = FontWeight.SemiBold)
                    Text(document.mimeType + " · " + document.sizeBytes + " bytes", style = MaterialTheme.typography.bodySmall)
                    Row {
                        TextButton(onClick = { onView(document) }, enabled = !state.busy) { Text("Ver documento") }
                        if (editable) TextButton(onClick = { onRemove(document) }, enabled = !state.busy) { Text("Quitar documento") }
                    }
                }
            }
        }
        state.errors["documents"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text("Representación de firma", style = MaterialTheme.typography.titleLarge)
        if (editable) {
            SignatureType.entries.forEach { type ->
                Row(Modifier.fillMaxWidth().selectable(selected = state.form.signature == type,
                    enabled = !state.busy, role = Role.RadioButton,
                    onClick = { onForm(state.form.copy(signature = type, signatureConfirmed = false)) }).padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = state.form.signature == type, onClick = null)
                    Text(type.label, Modifier.padding(start = 8.dp))
                }
            }
            Text(state.form.signature.explanation, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth().toggleable(
                value = state.form.signatureConfirmed, enabled = !state.busy, role = Role.Checkbox,
                onValueChange = { onForm(state.form.copy(signatureConfirmed = it)) },
            ), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.form.signatureConfirmed, onCheckedChange = null)
                Text("Confirmo la firma simulada", Modifier.padding(start = 8.dp))
            }
            state.errors["signature"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } else {
            Text(item.form.signature.label)
            Text(item.form.signature.explanation, style = MaterialTheme.typography.bodySmall)
        }

        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        if (state.busy) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp))
                Text("  Guardando…")
            }
        }
        if (editable) {
            OutlinedButton(onClick = onSave, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (item.status == ProcedureStatus.OBSERVED) "Guardar corrección" else "Guardar borrador")
            }
            Button(onClick = onSubmit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (item.status == ProcedureStatus.OBSERVED) "Reenviar corrección" else "Enviar a revisión")
            }
        } else if (user.type == AccountType.NOTARY && item.status == ProcedureStatus.UNDER_REVIEW) {
            OutlinedButton(onClick = onReturn, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Devolver con observaciones") }
            Button(onClick = onComplete, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Finalizar trámite") }
        }

        Text("Historial del trámite", style = MaterialTheme.typography.titleLarge)
        Column(Modifier.fillMaxWidth().animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item.history.forEach { event ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(event.status.label + " · " + event.actorRole.label, fontWeight = FontWeight.SemiBold)
                        Text(event.actorName + " · " + date(event.createdAt), style = MaterialTheme.typography.bodySmall)
                        Text(event.note)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun ProcedureDialogs(
    state: ProceduresUiState, onDismiss: () -> Unit, onCreate: (ProcedureType) -> Unit,
    onSubmit: () -> Unit, onReturn: () -> Unit, onComplete: () -> Unit,
    onDiscard: () -> Unit, onNote: (String) -> Unit,
) {
    val dialog = state.dialog ?: return
    var selectedType by rememberSaveable { mutableStateOf(ProcedureType.DECLARATION.name) }
    val title = when (dialog) {
        ProcedureDialog.CREATE -> "Nuevo trámite ficticio"
        ProcedureDialog.SUBMIT -> "¿Enviar a revisión?"
        ProcedureDialog.RETURN -> "Observaciones para corregir"
        ProcedureDialog.COMPLETE -> "¿Finalizar la simulación?"
        ProcedureDialog.DISCARD -> "¿Descartar cambios sin guardar?"
    }
    val confirm = when (dialog) {
        ProcedureDialog.CREATE -> "Crear borrador"
        ProcedureDialog.SUBMIT -> "Confirmar envío"
        ProcedureDialog.RETURN -> "Enviar observaciones"
        ProcedureDialog.COMPLETE -> "Confirmar finalización"
        ProcedureDialog.DISCARD -> "Descartar cambios"
    }
    AlertDialog(
        onDismissRequest = { if (!state.busy) onDismiss() },
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (dialog) {
                    ProcedureDialog.CREATE -> {
                        Text("El formato elegido se agregará como documento ficticio al borrador.")
                        ProcedureType.entries.forEach { type ->
                            Row(Modifier.fillMaxWidth().selectable(selected = selectedType == type.name,
                                enabled = !state.busy, role = Role.RadioButton,
                                onClick = { selectedType = type.name }).padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == type.name, onClick = null)
                                Text(type.label, Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    ProcedureDialog.RETURN -> OutlinedTextField(
                        value = state.reviewNote, onValueChange = onNote, label = { Text("Observación de prueba") },
                        minLines = 3, enabled = !state.busy, isError = state.errors["note"] != null,
                        supportingText = { Text(state.errors["note"] ?: "Entre 5 y 1000 caracteres.") },
                    )
                    ProcedureDialog.SUBMIT -> Text("La documentación quedará disponible para el perfil Notario de prueba. Mientras esté en revisión, no podrás editarla.")
                    ProcedureDialog.COMPLETE -> Text("Se guardará la finalización en el historial. Esta revisión es simulada y no tiene validez legal.")
                    ProcedureDialog.DISCARD -> Text("Se conservarán los datos ya guardados y los documentos adjuntos; se perderán las ediciones pendientes del formulario.")
                }
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !state.busy, onClick = {
                when (dialog) {
                    ProcedureDialog.CREATE -> onCreate(ProcedureType.valueOf(selectedType))
                    ProcedureDialog.SUBMIT -> onSubmit()
                    ProcedureDialog.RETURN -> onReturn()
                    ProcedureDialog.COMPLETE -> onComplete()
                    ProcedureDialog.DISCARD -> onDiscard()
                }
            }) { Text(if (state.busy) "Guardando…" else confirm) }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !state.busy) { Text("Cancelar") } },
    )
}
