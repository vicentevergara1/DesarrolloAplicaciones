package com.duoc.notix.data.model

enum class EstadoTramite(val etiquetaPersona: String, val etiquetaNotario: String) {
    ENVIADO("Enviado", "Pendiente"),
    DEVUELTO("Observado", "Devuelto"),   // la persona lo ve como "Observado"
    FINALIZADO("Finalizado", "Finalizado")
}