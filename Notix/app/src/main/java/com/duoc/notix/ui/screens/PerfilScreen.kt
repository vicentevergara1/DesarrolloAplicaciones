package com.duoc.notix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(usuarioId: String, onVolver: () -> Unit, onCerrarSesion: () -> Unit,
    vm: PerfilViewModel = viewModel(key = "perfil_$usuarioId", factory = PerfilViewModel.factory(LocalContext.current, usuarioId))) {
    val usuario by vm.usuario.collectAsState()
    var confirmar by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize(), color = com.duoc.notix.ui.theme.NotixColors.Fondo) {
        Column(Modifier.safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onVolver) { Text("Volver a mis trámites") }
            Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
            usuario?.let { u ->
                Text(u.nombreCompleto)
                Text(u.correo)
                Text("Perfil: ${u.tipoCuenta.etiqueta}")
                Text("Comuna: ${u.comuna}")
            }
            Text("Simulación académica con datos ficticios. La sesión se cierra al reiniciar la aplicación.")
            Button(onClick = { confirmar = true }) { Text("Cerrar sesión") }
        }
    }
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("¿Cerrar sesión?") },
        text = { Text("Tus trámites guardados seguirán disponibles al ingresar nuevamente.") },
        confirmButton = { TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } })
}
