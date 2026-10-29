package com.duoc.notix.data.validation

object RutValidator {

    /** @param rutLimpio sin puntos ni guion, ej. "111111111" o "12345678K". */
    fun validar(rutLimpio: String, etiqueta: String = "RUN"): String? {
        if (rutLimpio.isBlank()) return "Ingresa el $etiqueta."
        if (rutLimpio.length < 8) return "El $etiqueta está incompleto."

        val cuerpo = rutLimpio.dropLast(1)
        val dv = rutLimpio.last()

        if (!cuerpo.all { it.isDigit() }) return "El $etiqueta solo puede tener números y una K al final."
        if (dv != calcularDv(cuerpo)) return "El dígito verificador del $etiqueta no coincide."
        return null
    }

    fun calcularDv(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (c in cuerpo.reversed()) {
            suma += c.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }
}