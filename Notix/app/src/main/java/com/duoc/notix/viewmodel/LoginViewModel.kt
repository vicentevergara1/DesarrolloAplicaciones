package com.duoc.notix.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Usuario
import com.duoc.notix.repository.AuthRepository
import com.duoc.notix.repository.AuthResult
import com.duoc.notix.repository.SesionRepository
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(NotixDatabase.getDatabase(app).usuarioDao())

    private val sesiones = SesionRepository(app, NotixDatabase.getDatabase(app).usuarioDao())

    var recordar by mutableStateOf(false)
        private set

    fun onRecordarChange(valor: Boolean) { if (!cargando) recordar = valor }

    var error by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    fun ingresar(tipo: TipoCuenta, correo: String, clave: String, onOk: (Usuario) -> Unit) {
        if (cargando) return
        val mantenerSesion = recordar
        cargando = true
        error = null
        viewModelScope.launch {
            try {
            when (val r = repo.login(tipo, correo, clave)) {
                is AuthResult.Success -> {
                    sesiones.guardar(r.usuario.id, mantenerSesion)
                    error = null
                    onOk(r.usuario)
                }
                is AuthResult.Error -> error = r.message
            }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { error = "No se pudo acceder a la cuenta. Inténtalo nuevamente." }
            finally { cargando = false }
        }
    }
}
