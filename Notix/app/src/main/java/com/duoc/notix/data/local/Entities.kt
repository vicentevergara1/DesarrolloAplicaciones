package com.duoc.notix.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.duoc.notix.model.EstadoDocumento
import com.duoc.notix.model.EstadoTramite
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.TipoFirma

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val tipoCuenta: TipoCuenta,

    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val run: String,
    val numeroDocumento: String,
    val telefono: String,
    val comuna: String,

    val razonSocialEmpresa: String? = null,
    val rutEmpresa: String? = null,
    val giroEmpresa: String? = null,

    val numeroNotaria: Int? = null
)

@Entity(tableName = "tramites")
data class TramiteEntity(
    @PrimaryKey val id: String,
    val formatoId: String,
    val formatoNombre: String,
    val formatoCategoria: String,
    val solicitanteId: String,
    val solicitanteNombre: String,
    val actuaEnNombreDe: String? = null,
    val tipoFirma: TipoFirma,
    val estado: EstadoTramite,
    val antecedentesNombreCompleto: String,
    val antecedentesDocumentoIdentidad: String,
    val antecedentesCorreoContacto: String,
    val antecedentesComuna: String,
    val observacion: String? = null,
    val legibilidadConfirmada: Boolean = false,
    @androidx.room.ColumnInfo(defaultValue = "0") val firmaConfirmada: Boolean = false,
    @androidx.room.ColumnInfo(defaultValue = "''") val textoSolicitud: String = ""
)

@Entity(
    tableName = "documentos",
    foreignKeys = [ForeignKey(entity = TramiteEntity::class, parentColumns = ["id"],
        childColumns = ["tramiteId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tramiteId")]
)
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tramiteId: String,
    val nombre: String,
    val tamanoKb: Int,
    val estado: EstadoDocumento = EstadoDocumento.ACEPTADO,
    val rutaLocal: String? = null,
    val mime: String? = null
)

@Entity(
    tableName = "historial_eventos",
    foreignKeys = [ForeignKey(entity = TramiteEntity::class, parentColumns = ["id"],
        childColumns = ["tramiteId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tramiteId")]
)
data class EventoHistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tramiteId: String,
    val descripcion: String,
    val fechaHora: String,
    val rol: TipoCuenta
)

@Entity(tableName = "avisos")
data class AvisoEntity(
    @PrimaryKey val id: String,
    val destinatarioId: String,
    val tramiteId: String,
    val mensaje: String,
    val fechaHora: String,
    val leido: Boolean = false
)
