package com.duoc.notix.data.validation

object RutValidator {
    fun validar(valor: String, etiqueta: String = "RUN"): String? {
        val rut = valor.filterNot { it == '.' || it == '-' || it.isWhitespace() }.uppercase()
        if (rut.isEmpty()) return "Ingresa el $etiqueta ficticio."
        if (rut.length !in 8..9 || rut.dropLast(1).any { it !in '0'..'9' } ||
            rut.last() !in "0123456789K" || rut.first() == '0') {
            return "Ingresa 7 u 8 dígitos y un verificador del 0 al 9 o K para el $etiqueta ficticio."
        }
        return null
    }
}
