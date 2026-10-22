package com.duoc.notix.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.domain.AuthRepository
import com.duoc.notix.domain.ProcedureRepository
import com.duoc.notix.domain.User
import com.duoc.notix.ui.auth.AuthMode
import com.duoc.notix.ui.auth.AuthViewModel
import com.duoc.notix.ui.procedures.ProceduresViewModel
import com.duoc.notix.ui.screens.AuthScreen
import com.duoc.notix.ui.screens.ProceduresScreen

@Composable
fun NotixApp(authRepository: AuthRepository, procedureRepository: ProcedureRepository) {
    val auth: AuthViewModel = viewModel(factory = viewModelFactory { initializer { AuthViewModel(authRepository) } })
    val state by auth.state.collectAsStateWithLifecycle()
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        val user = state.user
        if (user == null) {
            BackHandler(enabled = state.mode == AuthMode.REGISTER && !state.loading) { auth.switchMode() }
            AuthScreen(state, auth::edit, auth::submit, auth::switchMode, Modifier.padding(padding))
        } else {
            key(user.id) {
                UserSession(user, procedureRepository, auth::logout, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun UserSession(user: User, repository: ProcedureRepository, logout: () -> Unit, modifier: Modifier) {
    // La consulta de trámites se destruye al cerrar esta sesión.
    val owner = remember {
        object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    val model: ProceduresViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = viewModelFactory { initializer { ProceduresViewModel(user.id, repository) } },
    )
    val state by model.state.collectAsStateWithLifecycle()
    var showLogout by rememberSaveable { mutableStateOf(false) }
    BackHandler { showLogout = true }
    ProceduresScreen(
        user = user, state = state,
        onCreate = { model.showCreateDialog(true) },
        onDismiss = { model.showCreateDialog(false) },
        onSave = model::createDraft,
        onRetry = model::load,
        onLogout = { showLogout = true },
        modifier = modifier,
    )
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus cuentas y borradores seguirán guardados en este dispositivo.") },
            confirmButton = { TextButton(onClick = logout) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { showLogout = false }) { Text("Cancelar") } },
        )
    }
}
