package com.duoc.notix.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario

sealed interface AuthResult {
    data class Success(val usuario: Usuario) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(private val usuarioDao: UsuarioDao) {

    suspend fun registrar(usuario: Usuario): AuthResult {
        val correo = usuario.correo.trim().lowercase()
        if (usuarioDao.getUsuarioPorCorreo(correo) != null)
            return AuthResult.Error("Ya existe una cuenta con ese correo.")
        return try {
            val nuevo = usuario.copy(correo = correo)
            usuarioDao.insertUsuario(nuevo.toEntity())
            AuthResult.Success(nuevo)
        } catch (e: SQLiteConstraintException) {
            AuthResult.Error("No se pudo crear la cuenta: datos duplicados.")
        }
    }

    suspend fun login(tipo: TipoCuenta, correo: String, contrasena: String): AuthResult {
        val u = usuarioDao.getUsuarioPorCorreo(correo.trim().lowercase())?.toDomain()
            ?: return AuthResult.Error("No existe una cuenta con ese correo.")
        if (u.tipoCuenta != tipo) return AuthResult.Error("Esa cuenta no es de tipo ${tipo.etiqueta}.")
        if (u.contrasena != contrasena) return AuthResult.Error("Contraseña incorrecta.")
        return AuthResult.Success(u)
    }
}