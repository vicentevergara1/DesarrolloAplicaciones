package com.duoc.notix.data.model

data class Tramite(
    val id: String,                          // TX-2026-0043
    val formato: FormatoTramite,             // Poder simple, etc.
    val solicitanteId: String,               // id del Usuario que lo creó
    val solicitanteNombre: String,           // "Ana Demo"
    val actuaEnNombreDe: String? = null,     // razón social si lo crea una Empresa
    val tipoFirma: TipoFirma,
    val estado: EstadoTramite,
    val documentos: List<Documento>,
    val antecedentes: Antecedentes,
    val observacion: String? = null,         // texto del notario al devolver
    val legibilidadConfirmada: Boolean = false, // el notario la marca antes de aprobar
    val historial: List<EventoHistorial>
)

data class Antecedentes(
    val nombreCompleto: String,
    val documentoIdentidad: String,          // ficticio: DEMO-0001
    val correoContacto: String,
    val comuna: String                       // obligatoria (la regla del mapa)
)

data class Documento(
    val nombre: String,                      // poder_demo.pdf
    val tamanoKb: Int,
    val estado: EstadoDocumento = EstadoDocumento.ACEPTADO
)

enum class EstadoDocumento(val etiqueta: String) {
    ACEPTADO("Aceptado"),
    OBSERVADO("Observado")                   // el anexo ilegible del ejemplo
}

data class EventoHistorial(
    val descripcion: String,                 // "Enviado al notario"
    val fechaHora: String,                   // simulada: "28/10/2026 14:30"
    val rol: TipoCuenta                      // quién hizo el cambio
)