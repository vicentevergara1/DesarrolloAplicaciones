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
    // Comunes (en Empresa son los datos del representante legal)
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val run: String,                  // limpio: "111111111"
    val numeroDocumento: String,      // limpio: "123456789"
    val telefono: String,
    val comuna: String,               // Persona: residencia · Empresa: domicilio comercial · Notario: comuna de la notaría
    // Solo Empresa
    val razonSocialEmpresa: String? = null,
    val rutEmpresa: String? = null,
    val giroEmpresa: String? = null,
    // Solo Notario
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
    @androidx.room.ColumnInfo(defaultValue = "0") val firmaConfirmada: Boolean = false
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
