package com.duoc.notix.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.model.EstadoTramite
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Tramite
import com.duoc.notix.repository.TramiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BandejaNotarioUiState(
    val cargando: Boolean = true,
    val nombreNotario: String = "",
    val numeroNotaria: Int = 1,
    val tramites: List<Tramite> = emptyList(),
    val enRevision: Int = 0,
    val observados: Int = 0,
    val finalizados: Int = 0,
    val filtro: FiltroTramites = FiltroTramites.TODOS
)

class BandejaNotarioViewModel(
    private val repo: TramiteRepository,
    private val usuarioId: String
) : ViewModel() {

    var procesando by androidx.compose.runtime.mutableStateOf(false)
        private set
    var error by androidx.compose.runtime.mutableStateOf<String?>(null)
        private set

    private fun evaluar(accion: suspend () -> Unit) {
        if (procesando) return
        procesando = true
        error = null
        viewModelScope.launch {
            try { accion() }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "No se pudo evaluar la solicitud." }
            finally { procesando = false }
        }
    }

    private val filtro = MutableStateFlow(FiltroTramites.TODOS)

    val uiState: StateFlow<BandejaNotarioUiState> = combine(
        repo.observarUsuario(usuarioId),
        repo.observarTodosLosTramites(),
        filtro
    ) { usuario, todos, f ->
        val ordenados = todos.sortedByDescending { it.id }
        BandejaNotarioUiState(
            cargando = false,
            nombreNotario = usuario?.nombreCompleto ?: "Notario Demo",
            numeroNotaria = usuario?.numeroNotaria ?: 1,
            tramites = when (f) {
                FiltroTramites.TODOS -> ordenados
                FiltroTramites.EN_CURSO -> ordenados.filter { it.estado == EstadoTramite.ENVIADO }
                FiltroTramites.OBSERVADOS -> ordenados.filter { it.estado == EstadoTramite.DEVUELTO }
                FiltroTramites.FINALIZADOS -> ordenados.filter { it.estado == EstadoTramite.FINALIZADO }
            },
            enRevision = todos.count { it.estado == EstadoTramite.ENVIADO },
            observados = todos.count { it.estado == EstadoTramite.DEVUELTO },
            finalizados = todos.count { it.estado == EstadoTramite.FINALIZADO },
            filtro = f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BandejaNotarioUiState())

    fun onFiltro(nuevo: FiltroTramites) {
        filtro.value = nuevo
    }

    fun aprobarTramite(tramite: Tramite) {
        evaluar {
            repo.actualizarEstadoTramite(
                tramite = tramite,
                nuevoEstado = EstadoTramite.FINALIZADO,
                notarioId = usuarioId, legibilidadConfirmada = true,
                descripcionHistorial = "Aprobado y finalizado por el notario",
                rolHistorial = TipoCuenta.NOTARIO,
                mensajeAviso = "Tu trámite ${tramite.id} (${tramite.formato.nombre}) ha sido aprobado y finalizado por el notario."
            )
        }
    }

    fun devolverTramite(tramite: Tramite, observacion: String) {
        evaluar {
            repo.actualizarEstadoTramite(
                tramite = tramite,
                nuevoEstado = EstadoTramite.DEVUELTO,
                notarioId = usuarioId,
                observacion = observacion,
                descripcionHistorial = "Devuelto con observaciones: $observacion",
                rolHistorial = TipoCuenta.NOTARIO,
                mensajeAviso = "Tu trámite ${tramite.id} (${tramite.formato.nombre}) ha sido devuelto con observaciones: $observacion"
            )
        }
    }

    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                BandejaNotarioViewModel(
                    TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao(), db),
                    usuarioId
                )
            }
        }
    }
}
