package com.duoc.notix.repository

import android.content.Context
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SesionRepository(context: Context, private val usuarios: UsuarioDao) {
    private val preferencias = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    suspend fun guardar(usuarioId: String, recordar: Boolean) = withContext(Dispatchers.IO) {
        val editor = preferencias.edit()
        if (recordar) {
            require(usuarioId.isNotBlank())
            editor.putString(USUARIO_ID, usuarioId)
        } else editor.remove(USUARIO_ID)
        check(editor.commit()) { "No se pudo guardar la preferencia de sesión." }
    }

    suspend fun restaurar(): Usuario? = withContext(Dispatchers.IO) {
        val id = preferencias.getString(USUARIO_ID, null) ?: return@withContext null
        val usuario = usuarios.getUsuarioPorId(id).first()?.toDomain()?.copy(contrasena = "")
        if (usuario == null) borrar()
        usuario
    }

    suspend fun borrar() = withContext(Dispatchers.IO) {
        check(preferencias.edit().remove(USUARIO_ID).commit()) { "No se pudo eliminar la sesión guardada." }
    }

    companion object {
        const val ARCHIVO = "sesion_recordada"
        private const val USUARIO_ID = "usuario_id"
    }
}
