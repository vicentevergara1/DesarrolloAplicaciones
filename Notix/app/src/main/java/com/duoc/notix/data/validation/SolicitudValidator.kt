package com.duoc.notix.data.validation

object SolicitudValidator {
    const val MIN_TEXTO = 10
    const val MAX_TEXTO = 5000

    fun validarTexto(valor: String): String? = when {
        valor.trim().length < MIN_TEXTO -> "Describe la solicitud con al menos $MIN_TEXTO caracteres."
        valor.trim().length > MAX_TEXTO -> "La solicitud admite hasta $MAX_TEXTO caracteres."
        else -> null
    }
}
