package com.duoc.notix.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class TramiteConDetalles(
    @Embedded val tramite: TramiteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tramiteId"
    )
    val documentos: List<DocumentoEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tramiteId"
    )
    val historial: List<EventoHistorialEntity>
)

@Dao
interface TramiteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTramite(tramite: TramiteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentos(documentos: List<DocumentoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: List<EventoHistorialEntity>)

    @Transaction
    suspend fun insertTramiteCompleto(
        tramite: TramiteEntity,
        documentos: List<DocumentoEntity>,
        historial: List<EventoHistorialEntity>
    ) {
        insertTramite(tramite)
        insertDocumentos(documentos)
        insertHistorial(historial)
    }

    @Transaction
    @Query("SELECT * FROM tramites WHERE id = :id")
    fun getTramiteConDetalles(id: String): Flow<TramiteConDetalles?>

    @Transaction
    @Query("SELECT * FROM tramites WHERE solicitanteId = :solicitanteId")
    fun getTramitesPorSolicitante(solicitanteId: String): Flow<List<TramiteConDetalles>>

    @Transaction
    @Query("SELECT * FROM tramites")
    fun getTodosLosTramites(): Flow<List<TramiteConDetalles>>

    @Query("DELETE FROM documentos WHERE tramiteId = :id")
    suspend fun borrarDocumentos(id: String)

    @Query("SELECT * FROM tramites WHERE id = :id")
    suspend fun buscarTramite(id: String): TramiteEntity?

    @Update
    suspend fun updateTramite(tramite: TramiteEntity)
}
@Dao
interface UsuarioDao {
    @Query("UPDATE usuarios SET contrasena = :clave WHERE id = :id")
    suspend fun actualizarClave(id: String, clave: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun getUsuarioPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getUsuarioPorId(id: String): Flow<UsuarioEntity?>
}

@Dao
interface AvisoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAviso(aviso: AvisoEntity)

    @Query("SELECT * FROM avisos WHERE destinatarioId = :destinatarioId ORDER BY rowid DESC")
    fun getAvisosPorDestinatario(destinatarioId: String): Flow<List<AvisoEntity>>

    @Query("UPDATE avisos SET leido = 1 WHERE id = :id")
    suspend fun marcarAvisoLeido(id: String)
}
