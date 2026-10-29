package com.duoc.notix.data.validation

object RutValidator {
    /** Acepta entradas con o sin puntos y guion; K y k son equivalentes. */
    fun validar(valor: String, etiqueta: String = "RUN"): String? {
        val rut = valor.filterNot { it == '.' || it == '-' || it.isWhitespace() }.uppercase()
        if (rut.isEmpty()) return "Ingresa el $etiqueta ficticio."
        if (rut.length !in 8..9 || rut.dropLast(1).any { it !in '0'..'9' } ||
            rut.last() !in "0123456789K" || rut.first() == '0') {
            return "Ingresa 7 u 8 dígitos y el verificador del $etiqueta ficticio."
        }
        val esperado = calcularDv(rut.dropLast(1))
        return if (rut.last() == esperado) null
        else "El dígito verificador del $etiqueta ficticio debe ser $esperado."
    }

    fun calcularDv(cuerpo: String): Char {
        require(cuerpo.length in 7..8 && cuerpo.all { it in '0'..'9' })
        val suma = cuerpo.reversed().mapIndexed { i, c -> (c - '0') * (2 + i % 6) }.sum()
        return when (val resto = 11 - suma % 11) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }
}
