package com.duoc.notix.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Tramite
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
        viewModelScope.launch {
            repo.actualizarEstadoTramite(
                tramite = tramite,
                nuevoEstado = EstadoTramite.FINALIZADO,
                descripcionHistorial = "Aprobado y finalizado por el notario",
                rolHistorial = TipoCuenta.NOTARIO,
                mensajeAviso = "Tu trámite ${tramite.id} (${tramite.formato.nombre}) ha sido aprobado y finalizado por el notario."
            )
        }
    }

    fun devolverTramite(tramite: Tramite, observacion: String) {
        viewModelScope.launch {
            repo.actualizarEstadoTramite(
                tramite = tramite,
                nuevoEstado = EstadoTramite.DEVUELTO,
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
                    TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao()),
                    usuarioId
                )
            }
        }
    }
}
