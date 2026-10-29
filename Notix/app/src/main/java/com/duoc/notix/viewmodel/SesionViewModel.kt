package com.duoc.notix.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.model.TipoCuenta
import com.duoc.notix.model.Usuario
import com.duoc.notix.repository.SesionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SesionViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SesionRepository(app, NotixDatabase.getDatabase(app).usuarioDao())
    private var restauracionCompletada = false
    private var cerrando = false

    var usuarioId by mutableStateOf("")
        private set
    var tipoCuenta by mutableStateOf<TipoCuenta?>(null)
        private set
    var redirigirAlInicio by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var generacion by mutableStateOf(0)
        private set
    val pantallas = ViewModelStore()

    suspend fun restaurar() {
        if (restauracionCompletada) return
        val usuario = repo.restaurar()
        if (usuario != null) {
            abrir(usuario)
            redirigirAlInicio = true
        }
        restauracionCompletada = true
    }

    fun abrir(usuario: Usuario) {
        usuarioId = usuario.id
        tipoCuenta = usuario.tipoCuenta
    }

    fun confirmarRestauracion() { redirigirAlInicio = false }
    fun limpiarError() { error = null }

    fun cerrar(onCerrada: () -> Unit) {
        if (cerrando) return
        cerrando = true
        viewModelScope.launch {
            try {
                repo.borrar()
                pantallas.clear()
                usuarioId = ""
                tipoCuenta = null
                generacion++
                error = null
                onCerrada()
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { error = "No se pudo cerrar la sesión. Inténtalo nuevamente." }
            finally { cerrando = false }
        }
    }

    override fun onCleared() { pantallas.clear() }
}
