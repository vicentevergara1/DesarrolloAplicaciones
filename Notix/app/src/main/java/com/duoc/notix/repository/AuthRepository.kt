package com.duoc.notix.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Patterns
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Usuario
import com.duoc.notix.data.validation.PasswordStorage
import com.duoc.notix.data.validation.RutValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

sealed interface AuthResult {
    data class Success(val usuario: Usuario) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(private val usuarioDao: UsuarioDao) {
    suspend fun registrar(usuario: Usuario): AuthResult = withContext(Dispatchers.IO) {
        val correo = usuario.correo.trim().lowercase(Locale.ROOT)
        val error = when {
            usuario.nombreCompleto.trim().length < 3 -> "Ingresa el nombre ficticio completo."
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Ingresa un correo ficticio válido."
            usuario.contrasena.isBlank() || usuario.contrasena.length !in 6..128 -> "La contraseña de prueba debe tener entre 6 y 128 caracteres y no estar vacía."
            RutValidator.validar(usuario.run) != null -> RutValidator.validar(usuario.run)
            usuario.tipoCuenta == TipoCuenta.EMPRESA && (usuario.empresa == null || RutValidator.validar(usuario.empresa.rutEmpresa, "RUT") != null) -> "Revisa el RUT ficticio de la empresa."
            else -> null
        }
        if (error != null) return@withContext AuthResult.Error(error)
        if (usuarioDao.getUsuarioPorCorreo(correo) != null)
            return@withContext AuthResult.Error("Ya existe una cuenta con ese correo.")
        try {
            val nuevo = usuario.copy(correo = correo, contrasena = PasswordStorage.guardar(usuario.contrasena))
            usuarioDao.insertUsuario(nuevo.toEntity())
            AuthResult.Success(nuevo.copy(contrasena = ""))
        } catch (_: SQLiteConstraintException) {
            AuthResult.Error("No se pudo crear la cuenta: datos duplicados.")
        }
    }

    suspend fun login(tipo: TipoCuenta, correo: String, contrasena: String): AuthResult = withContext(Dispatchers.IO) {
        val u = usuarioDao.getUsuarioPorCorreo(correo.trim().lowercase(Locale.ROOT))?.toDomain()
            ?: return@withContext AuthResult.Error("Correo, contraseña o perfil incorrectos.")
        if (u.tipoCuenta != tipo || !PasswordStorage.coincide(contrasena, u.contrasena))
            return@withContext AuthResult.Error("Correo, contraseña o perfil incorrectos.")

        if (!PasswordStorage.esHash(u.contrasena)) usuarioDao.actualizarClave(u.id, PasswordStorage.guardar(contrasena))
        AuthResult.Success(u.copy(contrasena = ""))
    }
}
