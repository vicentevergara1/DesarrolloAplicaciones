package com.duoc.notix.model

enum class EstadoTramite(val etiquetaPersona: String, val etiquetaNotario: String) {
    ENVIADO("Enviado", "Pendiente"),
    DEVUELTO("Observado", "Devuelto"),
    FINALIZADO("Finalizado", "Finalizado")
}
