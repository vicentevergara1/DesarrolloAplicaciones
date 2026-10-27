package com.duoc.notix.domain

import java.util.Locale

object AuthValidation {
    private val emailPattern = Regex("^[a-z0-9.!#$%&'*+/=?^_\u0060{|}~-]+@[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+$")

    fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    fun emailError(email: String): String? {
        val normalized = normalizeEmail(email)
        return if (normalized.length <= 254 && emailPattern.matches(normalized)) null
        else "Ingresa un correo válido, por ejemplo demo@example.com."
    }

    fun passwordError(password: String, registering: Boolean): String? = when {
        password.isBlank() -> "La contraseña no puede estar en blanco."
        password.length > 128 -> "Usa como máximo 128 caracteres."
        registering && password.length < 8 -> "Usa al menos 8 caracteres."
        else -> null
    }

    fun loginErrors(email: String, password: String): Map<String, String> = buildMap {
        emailError(email)?.let { put("correo", it) }
        passwordError(password, registering = false)?.let { put("password", it) }
    }
}
