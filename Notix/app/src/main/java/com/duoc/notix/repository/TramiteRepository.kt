package com.duoc.notix.repository

import com.duoc.notix.data.local.AvisoDao
import com.duoc.notix.data.local.TramiteDao
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.data.local.toEntities
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class TramiteRepository(
    private val tramiteDao: TramiteDao,
    private val avisoDao: AvisoDao,
    private val usuarioDao: UsuarioDao
) {
    fun observarMisTramites(usuarioId: String): Flow<List<Tramite>> =
        tramiteDao.getTramitesPorSolicitante(usuarioId).map { lista -> lista.map { it.toDomain() } }

    fun observarAvisosSinLeer(usuarioId: String): Flow<Boolean> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { avisos -> avisos.any { !it.leido } }

    fun observarAvisos(usuarioId: String): Flow<List<com.duoc.notix.data.model.Aviso>> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { lista -> lista.map { it.toDomain() } }

    suspend fun marcarAvisoLeido(avisoId: String) {
        avisoDao.marcarAvisoLeido(avisoId)
    }

    suspend fun crearAviso(aviso: com.duoc.notix.data.model.Aviso) {
        avisoDao.insertAviso(aviso.toEntity())
    }

    fun observarUsuario(usuarioId: String): Flow<Usuario?> =
        usuarioDao.getUsuarioPorId(usuarioId).map { it?.toDomain() }

    suspend fun obtenerUsuario(usuarioId: String): Usuario? =
        usuarioDao.getUsuarioPorId(usuarioId).firstOrNull()?.toDomain()

    suspend fun crearTramite(tramite: Tramite) {
        val (te, de, he) = tramite.toEntities()
        tramiteDao.insertTramiteCompleto(te, de, he)
    }

    fun observarTodosLosTramites(): Flow<List<Tramite>> =
        tramiteDao.getTodosLosTramites().map { lista -> lista.map { it.toDomain() } }

    suspend fun actualizarEstadoTramite(
        tramite: Tramite,
        nuevoEstado: com.duoc.notix.data.model.EstadoTramite,
        observacion: String? = null,
        descripcionHistorial: String,
        rolHistorial: com.duoc.notix.data.model.TipoCuenta,
        mensajeAviso: String? = null
    ) {
        val tramiteActualizado = tramite.copy(
            estado = nuevoEstado,
            observacion = observacion ?: tramite.observacion
        )
        val (te, _, _) = tramiteActualizado.toEntities()
        tramiteDao.updateTramite(te)

        val nuevoEvento = com.duoc.notix.data.local.EventoHistorialEntity(
            tramiteId = tramite.id,
            descripcion = descripcionHistorial,
            fechaHora = com.duoc.notix.util.fechaHoraActual(),
            rol = rolHistorial
        )
        tramiteDao.insertHistorial(listOf(nuevoEvento))

        if (mensajeAviso != null) {
            val aviso = com.duoc.notix.data.local.AvisoEntity(
                id = "AVS-${System.currentTimeMillis().toString().takeLast(6)}",
                destinatarioId = tramite.solicitanteId,
                tramiteId = tramite.id,
                mensaje = mensajeAviso,
                fechaHora = com.duoc.notix.util.fechaHoraActual(),
                leido = false
            )
            avisoDao.insertAviso(aviso)
        }
    }
}