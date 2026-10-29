package com.duoc.notix.repository

import androidx.room.withTransaction
import com.duoc.notix.data.local.*
import com.duoc.notix.model.*
import com.duoc.notix.util.fechaHoraActual
import kotlinx.coroutines.flow.*
import java.util.UUID

class TramiteRepository(
    private val tramiteDao: TramiteDao,
    private val avisoDao: AvisoDao,
    private val usuarioDao: UsuarioDao,
    private val db: NotixDatabase
) {
    fun observarMisTramites(usuarioId: String): Flow<List<Tramite>> =
        tramiteDao.getTramitesPorSolicitante(usuarioId).map { lista -> lista.map { it.toDomain() } }
    fun observarAvisosSinLeer(usuarioId: String): Flow<Boolean> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { avisos -> avisos.any { !it.leido } }
    fun observarAvisos(usuarioId: String): Flow<List<Aviso>> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { lista -> lista.map { it.toDomain() } }
    suspend fun marcarAvisoLeido(avisoId: String) = avisoDao.marcarAvisoLeido(avisoId)
    fun observarUsuario(usuarioId: String): Flow<Usuario?> =
        usuarioDao.getUsuarioPorId(usuarioId).map { it?.toDomain()?.copy(contrasena = "") }
    suspend fun obtenerUsuario(usuarioId: String): Usuario? = observarUsuario(usuarioId).firstOrNull()
    suspend fun obtenerTramite(id: String): Tramite? = tramiteDao.getTramiteConDetalles(id).first()?.toDomain()
    fun observarTodosLosTramites(): Flow<List<Tramite>> =
        tramiteDao.getTodosLosTramites().map { lista -> lista.map { it.toDomain() } }

    private suspend fun actor(id: String): Usuario = obtenerUsuario(id) ?: error("La cuenta no está disponible.")
    private fun aviso(t: Tramite, mensaje: String) = AvisoEntity(
        "AVS-${UUID.randomUUID()}", t.solicitanteId, t.id, mensaje, fechaHoraActual())

    suspend fun crearTramite(tramite: Tramite, usuarioId: String) = db.withTransaction {
        val usuario = actor(usuarioId)
        require(usuario.tipoCuenta != TipoCuenta.NOTARIO && tramite.solicitanteId == usuario.id)
        validarSolicitud(tramite)
        val (te, de, he) = tramite.toEntities()
        tramiteDao.insertTramiteCompleto(te, de, he)
        avisoDao.insertAviso(aviso(tramite, "Tu trámite ${tramite.id} fue enviado al notario con éxito."))
    }

    suspend fun reenviarTramite(tramite: Tramite, usuarioId: String) = db.withTransaction {
        val usuario = actor(usuarioId)
        val actual = tramiteDao.buscarTramite(tramite.id) ?: error("No se encuentra el trámite.")
        require(usuario.tipoCuenta != TipoCuenta.NOTARIO && actual.solicitanteId == usuario.id && tramite.solicitanteId == usuario.id)
        require(actual.estado == EstadoTramite.DEVUELTO) { "Solo puedes corregir un trámite devuelto." }
        validarSolicitud(tramite)
        val (te, de, _) = tramite.copy(estado = EstadoTramite.ENVIADO, observacion = null, legibilidadConfirmada = false).toEntities()
        tramiteDao.updateTramite(te)
        tramiteDao.borrarDocumentos(tramite.id)
        tramiteDao.insertDocumentos(de)
        tramiteDao.insertHistorial(listOf(EventoHistorialEntity(tramiteId = tramite.id,
            descripcion = "Antecedentes corregidos y reenviados al notario", fechaHora = fechaHoraActual(), rol = usuario.tipoCuenta)))
        avisoDao.insertAviso(aviso(tramite, "Tu trámite ${tramite.id} fue corregido y reenviado."))
    }

    private fun validarSolicitud(t: Tramite) {
        require(t.antecedentes.nombreCompleto.trim().length >= 3 && t.antecedentes.comuna.trim().length >= 3)
        require(com.duoc.notix.data.validation.RutValidator.validar(t.antecedentes.documentoIdentidad) == null)
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(t.antecedentes.correoContacto).matches())
        com.duoc.notix.data.validation.SolicitudValidator.validarTexto(t.textoSolicitud)?.let { throw IllegalArgumentException(it) }
        require(t.documentos.all { it.rutaLocal != null && it.tamanoKb in 1..5120 }) { "Revisa el documento de respaldo ficticio." }
        require(t.firmaConfirmada) { "Confirma la firma simulada." }
    }

    suspend fun actualizarEstadoTramite(
        tramite: Tramite,
        nuevoEstado: EstadoTramite,
        observacion: String? = null,
        descripcionHistorial: String,
        rolHistorial: TipoCuenta,
        mensajeAviso: String? = null,
        notarioId: String,
        legibilidadConfirmada: Boolean = false
    ) = db.withTransaction {
        val notario = actor(notarioId)
        require(notario.tipoCuenta == TipoCuenta.NOTARIO && rolHistorial == TipoCuenta.NOTARIO)
        val actual = tramiteDao.buscarTramite(tramite.id) ?: error("No se encuentra el trámite.")
        require(actual.estado == EstadoTramite.ENVIADO) { "El trámite ya fue evaluado o está pendiente de corrección." }
        require(nuevoEstado == EstadoTramite.DEVUELTO || nuevoEstado == EstadoTramite.FINALIZADO)
        if (nuevoEstado == EstadoTramite.DEVUELTO) require(!observacion.isNullOrBlank()) { "Ingresa el motivo de devolución." }
        if (nuevoEstado == EstadoTramite.FINALIZADO) require(legibilidadConfirmada) { "Confirma la revisión de legibilidad." }
        tramiteDao.updateTramite(actual.copy(estado = nuevoEstado, observacion = observacion?.trim(), legibilidadConfirmada = legibilidadConfirmada))
        tramiteDao.insertHistorial(listOf(EventoHistorialEntity(tramiteId = tramite.id,
            descripcion = descripcionHistorial, fechaHora = fechaHoraActual(), rol = TipoCuenta.NOTARIO)))
        mensajeAviso?.let { avisoDao.insertAviso(aviso(tramite, it)) }
    }
}
