package com.duoc.notix.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.domain.*
import com.duoc.notix.ui.auth.*
import com.duoc.notix.ui.procedures.*
import com.duoc.notix.ui.screens.*

@Composable
fun NotixApp(authRepository: AuthRepository, procedureRepository: ProcedureRepository, documentStore: DocumentStore) {
    val auth: AuthViewModel = viewModel(factory = viewModelFactory { initializer { AuthViewModel(authRepository) } })
    val state by auth.state.collectAsStateWithLifecycle()
    Scaffold(Modifier.fillMaxSize()) { padding ->
        val user = state.user
        if (user == null) {
            BackHandler(enabled = state.mode == AuthMode.REGISTER && !state.loading) { auth.switchMode() }
            AuthScreen(state, auth::edit, auth::editProfile, auth::chooseType, auth::fillExample,
                auth::submit, auth::switchMode, Modifier.padding(padding))
        } else {
            key(user.id) {
                UserSession(user, procedureRepository, documentStore, auth::logout, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun UserSession(
    user: User, repository: ProcedureRepository, documentStore: DocumentStore,
    logout: () -> Unit, modifier: Modifier,
) {
    // La clave evita compartir datos entre cuentas; el ViewModel sobrevive a la rotación.
    val model: ProceduresViewModel = viewModel(
        key = "procedures-" + user.id,
        factory = viewModelFactory { initializer { ProceduresViewModel(user, repository, documentStore) } },
    )
    LaunchedEffect(model) { model.start() }
    val state by model.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLogout by rememberSaveable { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Document?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) model.attach(uri.toString())
    }
    BackHandler {
        if (!state.busy) {
            if (state.selectedId != null) model.requestBack() else showLogout = true
        }
    }
    if (state.selectedId == null) {
        ProceduresScreen(user, state, model::open, { model.showDialog(ProcedureDialog.CREATE) },
            { showLogout = true }, model::start, modifier)
    } else {
        ProcedureDetailScreen(
            user, state, model::requestBack, model::editForm,
            onPick = { picker.launch(arrayOf("application/pdf", "text/plain", "image/png", "image/jpeg")) },
            onView = { document ->
                if (document.fileUri == null) preview = document
                else try {
                    context.startActivity(Intent(Intent.ACTION_VIEW)
                        .setDataAndType(document.fileUri.toUri(), document.mimeType)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                } catch (_: ActivityNotFoundException) {
                    model.message("No hay una aplicación para abrir este tipo de archivo en el dispositivo.")
                } catch (_: SecurityException) {
                    model.message("No se pudo abrir la copia local del archivo.")
                }
            },
            onRemove = model::remove, onSave = { model.save(false) }, onSubmit = model::requestSubmit,
            onReturn = { model.showDialog(ProcedureDialog.RETURN) },
            onComplete = { model.showDialog(ProcedureDialog.COMPLETE) }, modifier = modifier,
        )
    }
    ProcedureDialogs(state, { model.showDialog(null) }, model::createDraft, { model.save(true) },
        model::returnForCorrection, model::complete, model::closeDetail, model::note)
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Las cuentas, documentos e historial seguirán guardados en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = { model.endSession(); logout() }, enabled = !state.busy) { Text("Cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancelar") } },
        )
    }
    preview?.let { document ->
        AlertDialog(
            onDismissRequest = { preview = null },
            title = { Text(document.name) },
            text = { Text(document.templateText.orEmpty(), Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { preview = null }) { Text("Cerrar documento") } },
        )
    }
}
