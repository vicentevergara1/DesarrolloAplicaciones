package com.duoc.notix.viewmodel

import android.content.Context
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

enum class FiltroTramites(val etiqueta: String) {
    TODOS("Todos"),
    EN_CURSO("En curso"),
    OBSERVADOS("Observados"),
    FINALIZADOS("Finalizados")
}

data class MisTramitesUiState(
    val cargando: Boolean = true,
    val perfil: String = "",
    val nombre: String = "",
    val tramites: List<Tramite> = emptyList(),
    val enRevision: Int = 0,
    val observados: Int = 0,
    val finalizados: Int = 0,
    val alerta: Tramite? = null,
    val filtro: FiltroTramites = FiltroTramites.TODOS,
    val avisosSinLeer: Boolean = false
)

class MisTramitesViewModel(
    repo: TramiteRepository,
    usuarioId: String
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroTramites.TODOS)

    val uiState: StateFlow<MisTramitesUiState> = combine(
        repo.observarUsuario(usuarioId),
        repo.observarMisTramites(usuarioId),
        repo.observarAvisosSinLeer(usuarioId),
        filtro
    ) { usuario, todos, sinLeer, f ->
        val ordenados = todos.sortedByDescending { it.id }
        MisTramitesUiState(
            cargando = false,
            perfil = when (usuario?.tipoCuenta) {
                TipoCuenta.PERSONA -> "Persona natural"
                TipoCuenta.EMPRESA -> "Empresa"
                TipoCuenta.NOTARIO -> "Notario"
                null -> ""
            },
            nombre = usuario?.nombreCompleto.orEmpty().substringBefore(" "),
            tramites = when (f) {
                FiltroTramites.TODOS -> ordenados
                FiltroTramites.EN_CURSO -> ordenados.filter { it.estado != EstadoTramite.FINALIZADO }
                FiltroTramites.OBSERVADOS -> ordenados.filter { it.estado == EstadoTramite.DEVUELTO }
                FiltroTramites.FINALIZADOS -> ordenados.filter { it.estado == EstadoTramite.FINALIZADO }
            },

            enRevision = todos.count { it.estado == EstadoTramite.ENVIADO },
            observados = todos.count { it.estado == EstadoTramite.DEVUELTO },
            finalizados = todos.count { it.estado == EstadoTramite.FINALIZADO },
            alerta = ordenados.firstOrNull { it.estado == EstadoTramite.DEVUELTO },
            filtro = f,
            avisosSinLeer = sinLeer
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MisTramitesUiState())

    fun onFiltro(nuevo: FiltroTramites) { filtro.value = nuevo }

    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                MisTramitesViewModel(
                    TramiteRepository(db.tramiteDao(), db.avisoDao(), db.usuarioDao(), db),
                    usuarioId
                )
            }
        }
    }
}
