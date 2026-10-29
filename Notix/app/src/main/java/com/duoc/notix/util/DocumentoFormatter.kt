package com.duoc.notix.util

/** Solo dígitos, máx. 9. Es lo que se guarda. */
fun limpiarDocumento(raw: String): String =
    raw.filter { it in '0'..'9' }.take(9)

/** "123456789" -> "123.456.789". Solo presentación. */
fun formatearDocumento(raw: String): String =
    limpiarDocumento(raw).reversed().chunked(3).joinToString(".").reversed()
