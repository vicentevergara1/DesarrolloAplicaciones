package com.duoc.notix.domain

import java.util.Locale

data class AuthErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmation: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(name, email, password, confirmation).any { it != null }
}

object AuthValidation {
    fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    fun validate(
        registering: Boolean,
        name: String,
        email: String,
        password: String,
        confirmation: String,
    ): AuthErrors {
        val normalizedEmail = normalizeEmail(email)
        val validEmail = normalizedEmail.length <= 254 &&
            Regex("^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+$")
                .matches(normalizedEmail)
        return AuthErrors(
            name = if (registering && name.trim().length !in 2..60)
                "Usa un nombre de prueba de entre 2 y 60 caracteres." else null,
            email = if (!validEmail) "Ingresa un correo válido, por ejemplo demo@example.com." else null,
            password = when {
                password.isBlank() -> "Ingresa una contraseña de prueba."
                registering && password.length !in 8..128 -> "Usa entre 8 y 128 caracteres."
                else -> null
            },
            confirmation = if (registering && password != confirmation)
                "Las contraseñas no coinciden." else null,
        )
    }
}
