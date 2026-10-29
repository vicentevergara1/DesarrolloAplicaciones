package com.duoc.notix.model

data class Tramite(
    val id: String,
    val formato: FormatoTramite,
    val solicitanteId: String,
    val solicitanteNombre: String,
    val actuaEnNombreDe: String? = null,
    val tipoFirma: TipoFirma,
    val estado: EstadoTramite,
    val documentos: List<Documento>,
    val antecedentes: Antecedentes,
    val observacion: String? = null,
    val legibilidadConfirmada: Boolean = false,
    val historial: List<EventoHistorial>,
    val firmaConfirmada: Boolean = false,
    val textoSolicitud: String = ""
)

data class Antecedentes(
    val nombreCompleto: String,
    val documentoIdentidad: String,
    val correoContacto: String,
    val comuna: String
)

data class Documento(
    val nombre: String,
    val tamanoKb: Int,
    val estado: EstadoDocumento = EstadoDocumento.ACEPTADO,
    val rutaLocal: String? = null,
    val mime: String? = null
)

enum class EstadoDocumento(val etiqueta: String) {
    ACEPTADO("Aceptado"),
    OBSERVADO("Observado")
}

data class EventoHistorial(
    val descripcion: String,
    val fechaHora: String,
    val rol: TipoCuenta
)
