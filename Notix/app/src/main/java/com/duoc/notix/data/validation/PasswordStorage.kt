package com.duoc.notix.data.validation

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Guarda sal y derivación, nunca la clave; conserva el nombre de columna para migrar v2. */
object PasswordStorage {
    private const val PREFIX = "pbkdf2:"
    fun esHash(valor: String) = valor.startsWith(PREFIX)
    private fun derivar(clave: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), sal, 210_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
    private fun ByteArray.hex() = joinToString("") { "%02x".format(it.toInt() and 255) }
    private fun bytes(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    fun guardar(clave: String): String {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return PREFIX + sal.hex() + ":" + derivar(clave, sal).hex()
    }
    fun coincide(clave: String, valor: String): Boolean = if (!esHash(valor)) {
        MessageDigest.isEqual(clave.toByteArray(), valor.toByteArray())
    } else runCatching {
        val partes = valor.removePrefix(PREFIX).split(':')
        require(partes.size == 2 && partes[0].length == 32 && partes[1].length == 64)
        MessageDigest.isEqual(derivar(clave, bytes(partes[0])), bytes(partes[1]))
    }.getOrDefault(false)
}
