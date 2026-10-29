package com.duoc.notix.data.model

data class Usuario(
    val id: String,
    val tipoCuenta: TipoCuenta,
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val run: String,
    val numeroDocumento: String,
    val telefono: String,
    val comuna: String,
    val empresa: DatosEmpresa? = null,
    val numeroNotaria: Int? = null
)

data class DatosEmpresa(
    val razonSocial: String,
    val rutEmpresa: String,
    val giro: String
)