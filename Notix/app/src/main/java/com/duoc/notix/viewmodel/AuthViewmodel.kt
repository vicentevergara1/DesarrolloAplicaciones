package com.duoc.notix.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.model.*
import com.duoc.notix.repository.AuthRepository
import com.duoc.notix.repository.AuthResult
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AuthRepository(NotixDatabase.getDatabase(app).usuarioDao())

    var errorRegistro by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    fun registrar(tipo: TipoCuenta, v: Map<String, String>, pass: String, onOk: (Usuario) -> Unit) {
        if (cargando) return
        cargando = true
        errorRegistro = null
        viewModelScope.launch {
            try {
            when (val r = repo.registrar(construirUsuario(tipo, v, pass))) {
                is AuthResult.Success -> { errorRegistro = null; onOk(r.usuario) }
                is AuthResult.Error -> errorRegistro = r.message
            }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { errorRegistro = "No se pudo acceder a la cuenta. Inténtalo nuevamente." }
            finally { cargando = false }
        }
    }

    private fun construirUsuario(tipo: TipoCuenta, v: Map<String, String>, pass: String): Usuario {
        fun g(k: String) = v[k].orEmpty().trim()
        val id = "USR-" + UUID.randomUUID().toString().take(8).uppercase()
        return when (tipo) {
            TipoCuenta.PERSONA -> Usuario(
                id, tipo, g("nombre"), g("correo"), pass, g("rut"), g("doc"), g("telefono"), g("comuna")
            )
            TipoCuenta.EMPRESA -> Usuario(
                id, tipo, g("rep_nombre"), g("correo"), pass, g("rep_run"), g("rep_doc"), g("telefono"), g("comuna"),
                empresa = DatosEmpresa(g("razon"), g("rut"), g("giro"))
            )
            TipoCuenta.NOTARIO -> Usuario(
                id, tipo, g("nombre"), g("correo"), pass, g("run"), g("doc"), g("telefono"), g("notaria_comuna"),
                numeroNotaria = g("notaria_n").toIntOrNull()
            )
        }
    }
}
