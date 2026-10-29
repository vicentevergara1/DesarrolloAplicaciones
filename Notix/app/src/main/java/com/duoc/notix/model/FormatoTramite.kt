package com.duoc.notix.model

data class FormatoTramite(
    val id: String,
    val nombre: String,
    val categoria: String,
    val firmaRequerida: TipoFirma
)

val catalogoFormatos = listOf(
    FormatoTramite("F01", "Poder simple", "Poderes", TipoFirma.SIMPLE),
    FormatoTramite("F02", "Declaración jurada simple", "Declaraciones", TipoFirma.AVANZADA),
    FormatoTramite("F03", "Autorización de viaje", "Familia", TipoFirma.NOTARIADA)
)
