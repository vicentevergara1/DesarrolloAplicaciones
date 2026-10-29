package com.duoc.notix.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.repository.AuthRepository
import com.duoc.notix.data.repository.AuthResult
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(NotixDatabase.getDatabase(app).usuarioDao())

    var error by mutableStateOf<String?>(null)
        private set

    fun ingresar(tipo: TipoCuenta, correo: String, clave: String, onOk: (Usuario) -> Unit) {
        viewModelScope.launch {
            when (val r = repo.login(tipo, correo, clave)) {
                is AuthResult.Success -> { error = null; onOk(r.usuario) }
                is AuthResult.Error -> error = r.message
            }
        }
    }
}