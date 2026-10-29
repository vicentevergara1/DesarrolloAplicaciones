package com.duoc.notix.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.EventoHistorial
import com.duoc.notix.data.model.FormatoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.validation.RutValidator
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.util.fechaHoraActual
import kotlinx.coroutines.launch

data class CrearTramiteUiState(
    val usuario: Usuario? = null,
    val cargandoUsuario: Boolean = true,
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val errorGeneral: String? = null
)

class CrearTramiteViewModel(
    private val repo: TramiteRepository,
    private val usuarioId: String,
    val formato: FormatoTramite
) : ViewModel() {

    var uiState by mutableStateOf(CrearTramiteUiState())
        private set

    var nombreCompleto by mutableStateOf("")
        private set
    var run by mutableStateOf("")
        private set
    var correo by mutableStateOf("")
        private set
    var comuna by mutableStateOf("")
        private set
    var actuaEnNombreDe by mutableStateOf("")
        private set
    var documentoAdjuntoNombre by mutableStateOf<String?>(null)
        private set

    var nombreError by mutableStateOf<String?>(null)
        private set
    var runError by mutableStateOf<String?>(null)
        private set
    var correoError by mutableStateOf<String?>(null)
        private set
    var comunaError by mutableStateOf<String?>(null)
        private set

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            uiState = uiState.copy(cargandoUsuario = true)
            val usuario = repo.obtenerUsuario(usuarioId)
            if (usuario != null) {
                nombreCompleto = usuario.nombreCompleto
                run = usuario.run
                correo = usuario.correo
                comuna = usuario.comuna
                if (usuario.tipoCuenta == TipoCuenta.EMPRESA && usuario.empresa != null) {
                    actuaEnNombreDe = usuario.empresa.razonSocial
                }
            }
            uiState = uiState.copy(usuario = usuario, cargandoUsuario = false)
        }
    }

    fun onNombreChange(nuevo: String) {
        nombreCompleto = nuevo
        nombreError = if (nuevo.isBlank()) "El nombre no puede estar vacío" else null
    }

    fun onRunChange(nuevo: String) {
        run = nuevo
        runError = null
    }

    fun onCorreoChange(nuevo: String) {
        correo = nuevo
        correoError = if (nuevo.isBlank() || !nuevo.contains("@")) "Correo inválido" else null
    }

    fun onComunaChange(nuevo: String) {
        comuna = nuevo
        comunaError = if (nuevo.isBlank()) "Ingresa la comuna" else null
    }

    fun onActuaEnNombreDeChange(nuevo: String) {
        actuaEnNombreDe = nuevo
    }

    fun onDocumentoAdjuntado(nombre: String?) {
        documentoAdjuntoNombre = nombre
    }

    fun validarFormulario(): Boolean {
        val errNombre = if (nombreCompleto.isBlank()) "Ingresa el nombre completo" else null
        val errRun = RutValidator.validar(run, "RUN")
        val errCorreo = if (correo.isBlank()) "Ingresa el correo" else if (!correo.contains("@")) "Correo inválido" else null
        val errComuna = if (comuna.isBlank()) "Ingresa la comuna" else null

        nombreError = errNombre
        runError = errRun
        correoError = errCorreo
        comunaError = errComuna

        return errNombre == null && errRun == null && errCorreo == null && errComuna == null
    }

    fun guardarTramite(onExito: () -> Unit) {
        if (uiState.guardando) return // previene envíos duplicados

        if (!validarFormulario()) return

        val usuarioActual = uiState.usuario ?: return

        viewModelScope.launch {
            uiState = uiState.copy(guardando = true, errorGeneral = null)
            try {
                val nuevoId = "TX-2026-${(1000..9999).random()}"
                val docNombre = documentoAdjuntoNombre ?: "${formato.nombre.lowercase().replace(" ", "_")}_solicitud.pdf"
                val nuevoTramite = Tramite(
                    id = nuevoId,
                    formato = formato,
                    solicitanteId = usuarioActual.id,
                    solicitanteNombre = usuarioActual.nombreCompleto,
                    actuaEnNombreDe = actuaEnNombreDe.ifBlank { null },
                    tipoFirma = formato.firmaRequerida,
                    estado = EstadoTramite.ENVIADO,
                    documentos = listOf(
                        Documento(
                            nombre = docNombre,
                            tamanoKb = (120..350).random(),
                            estado = EstadoDocumento.ACEPTADO
                        )
                    ),
                    antecedentes = Antecedentes(
                        nombreCompleto = nombreCompleto.trim(),
                        documentoIdentidad = run.trim(),
                        correoContacto = correo.trim(),
                        comuna = comuna.trim()
                    ),
                    historial = listOf(
                        EventoHistorial(
                            descripcion = "Trámite ingresado correctamente",
                            fechaHora = fechaHoraActual(),
                            rol = usuarioActual.tipoCuenta
                        )
                    )
                )

                repo.crearTramite(nuevoTramite)

                val avisoIngreso = com.duoc.notix.data.model.Aviso(
                    id = "AVS-${System.currentTimeMillis().toString().takeLast(6)}",
                    destinatarioId = usuarioActual.id,
                    tramiteId = nuevoId,
                    mensaje = "Tu trámite $nuevoId (${formato.nombre}) fue enviado al notario con éxito.",
                    fechaHora = fechaHoraActual(),
                    leido = false
                )
                repo.crearAviso(avisoIngreso)

                uiState = uiState.copy(guardando = false, guardadoExitoso = true)
                onExito()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    guardando = false,
                    errorGeneral = "Error al guardar el trámite: ${e.localizedMessage ?: "Error desconocido"}"
                )
            }
        }
    }

    companion object {
        fun factory(context: Context, usuarioId: String, formato: FormatoTramite) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                CrearTramiteViewModel(
                    TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao()),
                    usuarioId,
                    formato
                )
            }
        }
    }
}
