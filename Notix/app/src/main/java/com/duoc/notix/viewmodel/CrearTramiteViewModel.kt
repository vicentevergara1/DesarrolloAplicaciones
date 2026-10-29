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
import com.duoc.notix.model.Antecedentes
import com.duoc.notix.model.Documento
import com.duoc.notix.model.EstadoTramite
import com.duoc.notix.model.EventoHistorial
import com.duoc.notix.model.FormatoTramite
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Tramite
import com.duoc.notix.model.Usuario
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
    val formato: FormatoTramite,
    private val documentosLocales: com.duoc.notix.data.local.DocumentosLocales,
    private val tramiteId: String? = null
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
    var textoSolicitud by mutableStateOf("")
        private set
    var textoError by mutableStateOf<String?>(null)
        private set

    fun onTextoSolicitudChange(nuevo: String) {
        textoSolicitud = nuevo
        textoError = com.duoc.notix.data.validation.SolicitudValidator.validarTexto(nuevo)
    }

    private var documento by mutableStateOf<Documento?>(null)
    private var original: Tramite? = null
    private var archivoNuevo = false
    val documentoAdjuntoNombre: String? get() = documento?.nombre
    var importando by mutableStateOf(false)
        private set
    var firmaConfirmada by mutableStateOf(false)
        private set
    fun onFirmaChange(valor: Boolean) { firmaConfirmada = valor }

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
            try {
            val usuario = repo.obtenerUsuario(usuarioId)
            require(usuario != null && usuario.tipoCuenta != TipoCuenta.NOTARIO) { "No se encuentra una cuenta solicitante." }
            if (tramiteId != null) {
                original = repo.obtenerTramite(tramiteId)
                require(original?.solicitanteId == usuario.id && original?.estado == EstadoTramite.DEVUELTO) { "Este trámite no está disponible para corregir." }
                val t = requireNotNull(original)
                textoSolicitud = t.textoSolicitud
                nombreCompleto = t.antecedentes.nombreCompleto
                run = t.antecedentes.documentoIdentidad
                correo = t.antecedentes.correoContacto
                comuna = t.antecedentes.comuna
                actuaEnNombreDe = t.actuaEnNombreDe.orEmpty()
                documento = t.documentos.firstOrNull { it.rutaLocal != null }
                uiState = uiState.copy(usuario = usuario, cargandoUsuario = false)
                return@launch
            }
            run {
                nombreCompleto = usuario.nombreCompleto
                run = usuario.run
                correo = usuario.correo
                comuna = usuario.comuna
                if (usuario.tipoCuenta == TipoCuenta.EMPRESA && usuario.empresa != null) {
                    actuaEnNombreDe = usuario.empresa.razonSocial
                }
            }
            uiState = uiState.copy(usuario = usuario, cargandoUsuario = false)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { uiState = uiState.copy(cargandoUsuario = false, errorGeneral = e.message ?: "No se pudo cargar el trámite.") }
        }
    }

    fun onNombreChange(nuevo: String) {
        nombreCompleto = nuevo
        nombreError = if (nuevo.trim().length < 3) "Ingresa al menos 3 caracteres" else null
    }

    fun onRunChange(nuevo: String) {
        run = com.duoc.notix.util.limpiarRut(nuevo)
        runError = RutValidator.validar(run, "RUN")
    }

    fun onCorreoChange(nuevo: String) {
        correo = nuevo
        correoError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nuevo.trim()).matches()) "Correo inválido" else null
    }

    fun onComunaChange(nuevo: String) {
        comuna = nuevo
        comunaError = if (nuevo.trim().length < 3) "Ingresa la comuna" else null
    }

    fun onActuaEnNombreDeChange(nuevo: String) {
        actuaEnNombreDe = nuevo
    }

    fun onDocumentoAdjuntado(uri: android.net.Uri) {
        if (importando || uiState.guardando) return
        importando = true
        viewModelScope.launch {
            try {
                val nuevo = documentosLocales.importar(uri)
                if (archivoNuevo) documento?.let(documentosLocales::borrar)
                documento = nuevo
                archivoNuevo = true
                uiState = uiState.copy(errorGeneral = null)
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { uiState = uiState.copy(errorGeneral = e.message ?: "No se pudo adjuntar el archivo.") }
            finally { importando = false }
        }
    }

    fun quitarDocumento() {
        if (importando || uiState.guardando) return
        if (archivoNuevo) documento?.let(documentosLocales::borrar)
        documento = null
        archivoNuevo = false
    }

    override fun onCleared() {
        if (archivoNuevo && !uiState.guardadoExitoso) documento?.let(documentosLocales::borrar)
        super.onCleared()
    }

    fun validarFormulario(): Boolean {
        textoError = com.duoc.notix.data.validation.SolicitudValidator.validarTexto(textoSolicitud)
        val errNombre = if (nombreCompleto.trim().length < 3) "Ingresa el nombre completo" else null
        val errRun = RutValidator.validar(run, "RUN")
        val errCorreo = if (correo.isBlank()) "Ingresa el correo" else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) "Correo inválido" else null
        val errComuna = if (comuna.trim().length < 3) "Ingresa la comuna" else null

        nombreError = errNombre
        runError = errRun
        correoError = errCorreo
        comunaError = errComuna

        val extra = when {
            importando -> "Espera a que termine de adjuntarse el archivo."
            !firmaConfirmada -> "Confirma la firma simulada antes de enviar."
            else -> null
        }
        uiState = uiState.copy(errorGeneral = extra)
        return errNombre == null && errRun == null && errCorreo == null && errComuna == null && textoError == null && extra == null
    }

    fun guardarTramite(onExito: () -> Unit) {
        if (uiState.guardando || uiState.guardadoExitoso || uiState.cargandoUsuario) return

        if (!validarFormulario()) return

        val usuarioActual = uiState.usuario ?: return

        viewModelScope.launch {
            uiState = uiState.copy(guardando = true, errorGeneral = null)
            try {
                val nuevoId = tramiteId ?: "TX-${System.currentTimeMillis()}-${java.util.UUID.randomUUID().toString().take(8)}"
                val nuevoTramite = Tramite(
                    id = nuevoId,
                    formato = formato,
                    solicitanteId = usuarioActual.id,
                    solicitanteNombre = usuarioActual.nombreCompleto,
                    actuaEnNombreDe = actuaEnNombreDe.ifBlank { null },
                    tipoFirma = formato.firmaRequerida,
                    estado = EstadoTramite.ENVIADO,
                    documentos = listOfNotNull(documento),
                    textoSolicitud = textoSolicitud.trim(),
                    firmaConfirmada = firmaConfirmada,
                    antecedentes = Antecedentes(
                        nombreCompleto = nombreCompleto.trim(),
                        documentoIdentidad = run.trim(),
                        correoContacto = correo.trim(),
                        comuna = comuna.trim()
                    ),
                    historial = original?.historial ?: listOf(
                        EventoHistorial(
                            descripcion = "Trámite ingresado con ${formato.firmaRequerida.etiqueta.lowercase()} simulada confirmada",
                            fechaHora = fechaHoraActual(),
                            rol = usuarioActual.tipoCuenta
                        )
                    )
                )

                if (tramiteId == null) repo.crearTramite(nuevoTramite, usuarioId)
                else repo.reenviarTramite(nuevoTramite, usuarioId)
                original?.documentos?.filter { it.rutaLocal != documento?.rutaLocal }?.forEach(documentosLocales::borrar)
                uiState = uiState.copy(guardando = false, guardadoExitoso = true)
                onExito()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) {
                uiState = uiState.copy(
                    guardando = false,
                    errorGeneral = "Error al guardar el trámite: ${e.localizedMessage ?: "Error desconocido"}"
                )
            }
        }
    }

    companion object {
        fun factory(context: Context, usuarioId: String, formato: FormatoTramite, tramiteId: String? = null) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                CrearTramiteViewModel(
                    TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao(), db),
                    usuarioId,
                    formato,
                    com.duoc.notix.data.local.DocumentosLocales(context.applicationContext),
                    tramiteId
                )
            }
        }
    }
}
