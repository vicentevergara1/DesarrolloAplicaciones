package com.duoc.notix.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val meses = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

fun fechaHoraActual(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

fun fechaCorta(fechaHora: String?): String {
    if (fechaHora == null) return ""
    val partes = fechaHora.substringBefore(" ").split("/")
    if (partes.size != 3) return fechaHora
    val dia = partes[0].toIntOrNull() ?: return fechaHora
    val mes = partes[1].toIntOrNull()?.let { meses.getOrNull(it - 1) } ?: return fechaHora
    return "$dia $mes ${partes[2]}"
}
