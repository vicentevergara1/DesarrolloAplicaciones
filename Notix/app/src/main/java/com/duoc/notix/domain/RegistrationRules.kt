package com.duoc.notix.domain

enum class FieldKind { TEXT, EMAIL, NUMBER, PHONE, ASCII }

data class RegistrationField(
    val key: String, val title: String, val example: String,
    val kind: FieldKind = FieldKind.TEXT,
)

object RegistrationRules {
    const val DEMO_PASSWORD = "Demo2026!"
    private fun name(key: String, title: String, example: String) = RegistrationField(key, title, example)
    private fun rut(key: String, title: String, example: String = "11.111.111-1") =
        RegistrationField(key, title, example, FieldKind.ASCII)
    private fun document(key: String, title: String) =
        RegistrationField(key, title, "123.456.789", FieldKind.NUMBER)
    private fun email(example: String, title: String = "Correo electrónico ficticio") =
        RegistrationField("correo", title, example, FieldKind.EMAIL)
    private val phone = RegistrationField("telefono", "Teléfono ficticio", "000000000", FieldKind.PHONE)

    fun fieldsFor(type: AccountType): List<RegistrationField> = when (type) {
        AccountType.PERSON -> listOf(
            name("nombre", "Nombre completo ficticio", "Ana Demo Prueba"),
            rut("rut", "RUT ficticio"), document("doc", "N° de documento ficticio"),
            email("persona.demo@example.com"), phone,
            name("comuna", "Comuna de prueba", "Comuna Demo")
        )
        AccountType.COMPANY -> listOf(
            name("razon", "Nombre de empresa ficticia", "Comercial Demo SpA"),
            rut("rut", "RUT de empresa ficticio", "76.111.111-6"),
            name("giro", "Giro de prueba", "Venta de insumos ficticios"),
            name("rep_nombre", "Representante ficticio", "Pedro Demo Prueba"),
            rut("rep_run", "RUN del representante ficticio"),
            document("rep_doc", "N° de documento del representante ficticio"),
            email("empresa.demo@example.com", "Correo de contacto ficticio"), phone,
            name("comuna", "Comuna comercial de prueba", "Comuna Demo")
        )
        AccountType.NOTARY -> listOf(
            name("nombre", "Nombre del notario ficticio", "Luis Demo Prueba"),
            rut("run", "RUN del notario ficticio", "22.222.222-2"),
            document("doc", "N° de documento ficticio"),
            RegistrationField("notaria_n", "N° de notaría de prueba", "12", FieldKind.NUMBER),
            name("notaria_comuna", "Comuna de notaría de prueba", "Comuna Demo"),
            email("notario.demo@example.com", "Correo institucional ficticio"), phone
        )
    }

    fun example(type: AccountType): Map<String, String> = fieldsFor(type).associate { it.key to it.example }

    fun format(key: String, value: String): String = when (key) {
        "rut", "run", "rep_run" -> {
            val clean = value.filter { it in '0'..'9' || it == 'k' || it == 'K' }.uppercase().take(9)
            if (clean.length <= 1) clean
            else clean.dropLast(1).reversed().chunked(3).joinToString(".").reversed() + "-" + clean.last()
        }
        "doc", "rep_doc" -> digits(value).take(9).chunked(3).joinToString(".")
        "telefono" -> digits(value).take(9)
        "notaria_n" -> digits(value).take(3)
        else -> value
    }

    private fun digits(value: String) = value.filter { it in '0'..'9' }

    fun validRut(value: String): Boolean {
        val clean = value.filter { it in '0'..'9' || it == 'k' || it == 'K' }.uppercase()
        if (clean.length !in 8..9 || !clean.dropLast(1).all { it in '0'..'9' }) return false
        val sum = clean.dropLast(1).reversed().mapIndexed { index, char ->
            (char - '0') * (index % 6 + 2)
        }.sum()
        val result = 11 - sum % 11
        val expected = when (result) { 11 -> '0'; 10 -> 'K'; else -> '0' + result }
        return expected == clean.last()
    }

    fun validate(type: AccountType, values: Map<String, String>, password: String, confirmation: String): Map<String, String> =
        buildMap {
            fieldsFor(type).forEach { field ->
                val value = values[field.key].orEmpty()
                val error = when (field.key) {
                    "rut", "run", "rep_run" -> if (!validRut(value)) "Revisa el identificador ficticio y su dígito verificador." else null
                    "doc", "rep_doc" -> if (digits(value).length != 9) "Ingresa los 9 dígitos del documento ficticio." else null
                    "telefono" -> if (digits(value).length != 9) "Ingresa 9 dígitos de prueba." else null
                    "notaria_n" -> if ((value.toIntOrNull() ?: 0) !in 1..999) "Usa un número de 1 a 999." else null
                    "correo" -> AuthValidation.emailError(value)
                    else -> if (value.trim().length !in 3..100) "Usa entre 3 y 100 caracteres de prueba." else null
                }
                if (error != null) put(field.key, error)
            }
            AuthValidation.passwordError(password, registering = true)?.let { put("password", it) }
            if (confirmation != password || confirmation.isEmpty()) put("confirmation", "Las contraseñas no coinciden.")
        }
}
