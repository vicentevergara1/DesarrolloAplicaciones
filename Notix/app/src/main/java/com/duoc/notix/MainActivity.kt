package com.duoc.notix

import android.os.Bundle
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.duoc.notix.model.catalogoFormatos
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.duoc.notix.data.local.DatabaseSeeder
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.ui.screens.AvisosScreen
import com.duoc.notix.ui.screens.BandejaNotarioScreen
import com.duoc.notix.ui.screens.CatalogoFormatosScreen
import com.duoc.notix.ui.screens.CrearCuentaScreen
import com.duoc.notix.ui.screens.CrearTramiteScreen
import com.duoc.notix.ui.screens.LoginScreen
import com.duoc.notix.ui.screens.MisTramitesScreen
import com.duoc.notix.ui.screens.WelcomeScreen
import com.duoc.notix.ui.screens.PerfilScreen
import com.duoc.notix.ui.theme.NotixTheme

enum class Pantalla {
    Welcome,
    Login,
    CrearCuenta,
    MisTramites,
    CatalogoFormatos,
    CrearTramite,
    BandejaNotario,
    Avisos,
    Perfil
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sesion = androidx.lifecycle.ViewModelProvider(this)[com.duoc.notix.viewmodel.SesionViewModel::class.java]
        setContent {
            NotixTheme {
                var inicializada by remember { mutableStateOf(false) }
                var errorInicio by remember { mutableStateOf<String?>(null) }
                var intentoInicio by remember { mutableStateOf(0) }
                LaunchedEffect(intentoInicio) {
                    try { DatabaseSeeder.seedIfEmpty(applicationContext); sesion.restaurar(); inicializada = true; errorInicio = null }
                    catch (e: kotlinx.coroutines.CancellationException) { throw e }
                    catch (_: Exception) { errorInicio = "No se pudieron cargar los datos locales." }
                }
                if (!inicializada) {
                    androidx.compose.foundation.layout.Column(Modifier.padding(24.dp)) {
                        androidx.compose.material3.Text(errorInicio ?: "Preparando Notix…")
                        if (errorInicio != null) androidx.compose.material3.TextButton(onClick = { intentoInicio++ }) {
                            androidx.compose.material3.Text("Reintentar")
                        }
                    }
                    return@NotixTheme
                }

                var pantallaActual by rememberSaveable { mutableStateOf(Pantalla.Welcome) }
                val usuarioId = sesion.usuarioId
                var formatoId by rememberSaveable { mutableStateOf<String?>(null) }
                val formatoSeleccionado = catalogoFormatos.find { it.id == formatoId }
                var tramiteCorreccionId by rememberSaveable { mutableStateOf<String?>(null) }
                var solicitudKey by rememberSaveable { mutableStateOf("") }
                val sesionKey = sesion.generacion

                val sessionStore = sesion.pantallas
                val owner = remember(sessionStore) {
                    object : androidx.lifecycle.ViewModelStoreOwner, androidx.lifecycle.HasDefaultViewModelProviderFactory {
                        override val viewModelStore = sessionStore
                        override val defaultViewModelProviderFactory get() = this@MainActivity.defaultViewModelProviderFactory
                        override val defaultViewModelCreationExtras get() = this@MainActivity.defaultViewModelCreationExtras
                    }
                }
                val cerrarSesion = {
                    sesion.cerrar {
                        formatoId = null
                        tramiteCorreccionId = null
                        pantallaActual = Pantalla.Login
                    }
                }
                sesion.error?.let { mensaje ->
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = sesion::limpiarError,
                        title = { androidx.compose.material3.Text("Cerrar sesión") },
                        text = { androidx.compose.material3.Text(mensaje) },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = sesion::limpiarError) {
                                androidx.compose.material3.Text("Aceptar")
                            }
                        }
                    )
                }
                if (sesion.redirigirAlInicio) {
                    LaunchedEffect(Unit) {
                        pantallaActual = if (sesion.tipoCuenta == TipoCuenta.NOTARIO) Pantalla.BandejaNotario else Pantalla.MisTramites
                        formatoId = null
                        tramiteCorreccionId = null
                        sesion.confirmarRestauracion()
                    }
                    return@NotixTheme
                }
                if (usuarioId.isEmpty() && pantallaActual !in listOf(Pantalla.Welcome, Pantalla.Login, Pantalla.CrearCuenta)) {
                    LaunchedEffect(Unit) { pantallaActual = Pantalla.Welcome }
                    return@NotixTheme
                }
                BackHandler(enabled = pantallaActual != Pantalla.Welcome) {
                    pantallaActual = when (pantallaActual) {
                        Pantalla.Login, Pantalla.CrearCuenta -> Pantalla.Welcome
                        Pantalla.CrearTramite -> { sessionStore.clear(); Pantalla.CatalogoFormatos }
                        Pantalla.CatalogoFormatos, Pantalla.Avisos, Pantalla.Perfil -> Pantalla.MisTramites
                        else -> pantallaActual
                    }
                }
                androidx.compose.runtime.CompositionLocalProvider(androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner provides owner) {
                key(sesionKey) {

                when (pantallaActual) {
                    Pantalla.Welcome -> WelcomeScreen(
                        onNavigateToLogin = { pantallaActual = Pantalla.Login },
                        onNavigateToRegister = { pantallaActual = Pantalla.CrearCuenta }
                    )
                    Pantalla.Login -> LoginScreen(
                        onLoginSuccess = { usuario ->
                            sesion.abrir(usuario)
                            if (usuario.tipoCuenta == TipoCuenta.NOTARIO) {
                                pantallaActual = Pantalla.BandejaNotario
                            } else {
                                pantallaActual = Pantalla.MisTramites
                            }
                        },
                        onCreateAccount = { pantallaActual = Pantalla.CrearCuenta }
                    )
                    Pantalla.CrearCuenta -> CrearCuentaScreen(
                        onVolver = { pantallaActual = Pantalla.Welcome },
                        onIngresar = { pantallaActual = Pantalla.Login },
                        onRegistrado = { usuario ->
                            sesion.abrir(usuario)
                            if (usuario.tipoCuenta == TipoCuenta.NOTARIO) {
                                pantallaActual = Pantalla.BandejaNotario
                            } else {
                                pantallaActual = Pantalla.MisTramites
                            }
                        }
                    )
                    Pantalla.MisTramites -> MisTramitesScreen(
                        usuarioId = usuarioId,
                        onNuevoTramite = { pantallaActual = Pantalla.CatalogoFormatos },
                        onPerfil = { pantallaActual = Pantalla.Perfil },
                        onCorregir = { tramite ->
                            tramiteCorreccionId = tramite.id
                            formatoId = tramite.formato.id
                            solicitudKey = java.util.UUID.randomUUID().toString()
                            pantallaActual = Pantalla.CrearTramite
                        },

                        onAvisos = { pantallaActual = Pantalla.Avisos }
                    )
                    Pantalla.CatalogoFormatos -> CatalogoFormatosScreen(
                        onSeleccionarFormato = { formato ->
                            formatoId = formato.id
                            tramiteCorreccionId = null
                            solicitudKey = java.util.UUID.randomUUID().toString()
                            pantallaActual = Pantalla.CrearTramite
                        },
                        onVolver = { pantallaActual = Pantalla.MisTramites }
                    )
                    Pantalla.CrearTramite -> {
                        formatoSeleccionado?.let { formato ->
                            CrearTramiteScreen(
                                usuarioId = usuarioId,
                                formato = formato,
                                solicitudKey = solicitudKey, tramiteId = tramiteCorreccionId,
                                onVolver = { sessionStore.clear(); pantallaActual = Pantalla.CatalogoFormatos },
                                onTramiteCreado = { sessionStore.clear(); pantallaActual = Pantalla.MisTramites }
                            )
                        } ?: run {
                            LaunchedEffect(Unit) { pantallaActual = Pantalla.CatalogoFormatos }
                        }
                    }
                    Pantalla.BandejaNotario -> BandejaNotarioScreen(
                        usuarioId = usuarioId,
                        onCerrarSesion = cerrarSesion
                    )
                    Pantalla.Avisos -> AvisosScreen(
                        usuarioId = usuarioId,
                        onVolver = { pantallaActual = Pantalla.MisTramites },
                        onPerfil = { pantallaActual = Pantalla.Perfil }
                    )
                    Pantalla.Perfil -> PerfilScreen(usuarioId = usuarioId,
                        onVolver = { pantallaActual = Pantalla.MisTramites }, onCerrarSesion = cerrarSesion)
                }
                }
                }
            }
        }
    }
}
