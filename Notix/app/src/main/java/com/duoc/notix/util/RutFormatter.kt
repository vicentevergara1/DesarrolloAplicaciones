package com.duoc.notix.util

fun limpiarRut(raw: String): String {
    val sb = StringBuilder()
    for (c in raw.uppercase()) {
        val hayK = sb.lastOrNull() == 'K'
        when {
            c in '0'..'9' && sb.length < 9 && !hayK -> sb.append(c)
            c == 'K' && sb.length in 7..8 && !hayK -> sb.append(c)
        }
    }
    return sb.toString()
}

fun formatearRut(raw: String): String {
    val l = limpiarRut(raw)
    if (l.length <= 1) return l
    val cuerpo = l.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
    return "$cuerpo-${l.last()}"
}
