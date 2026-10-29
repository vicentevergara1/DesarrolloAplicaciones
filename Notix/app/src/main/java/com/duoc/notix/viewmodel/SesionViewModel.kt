package com.duoc.notix.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore

class SesionViewModel : ViewModel() {
    var usuarioId by mutableStateOf("")
    var generacion by mutableStateOf(0)
    val pantallas = ViewModelStore()
    fun cerrar() { pantallas.clear(); usuarioId = ""; generacion++ }
    override fun onCleared() { pantallas.clear() }
}
