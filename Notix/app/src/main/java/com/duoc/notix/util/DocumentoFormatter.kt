package com.duoc.notix.util

fun limpiarDocumento(raw: String): String =
    raw.filter { it in '0'..'9' }.take(9)

fun formatearDocumento(raw: String): String =
    limpiarDocumento(raw).reversed().chunked(3).joinToString(".").reversed()
