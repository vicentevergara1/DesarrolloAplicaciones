package com.duoc.notix.model

data class Aviso(
    val id: String,
    val destinatarioId: String,
    val tramiteId: String,
    val mensaje: String,
    val fechaHora: String,
    val leido: Boolean = false
)
