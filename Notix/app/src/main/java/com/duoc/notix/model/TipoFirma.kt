package com.duoc.notix.data.model

enum class TipoFirma(val etiqueta: String, val descripcion: String) {
    SIMPLE("Firma simple", "Dibuja tu firma en pantalla"),
    AVANZADA("Firma avanzada", "Simulación de e-token o certificado (mock)"),
    NOTARIADA("Firma notariada", "Autorización ante notario (simulada)")
}