package com.duoc.notix.model

data class Aviso(
    val id: String,
    val destinatarioId: String,      // a quién le llega
    val tramiteId: String,           // al tocarlo abre este trámite
    val mensaje: String,             // "El notario devolvió tu trámite TX-2026-0042 con observaciones."
    val fechaHora: String,
    val leido: Boolean = false
)
