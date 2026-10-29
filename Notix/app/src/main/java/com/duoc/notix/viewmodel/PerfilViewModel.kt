package com.duoc.notix.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.local.toDomain
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PerfilViewModel(context: Context, usuarioId: String) : ViewModel() {
    val usuario = NotixDatabase.getDatabase(context).usuarioDao().getUsuarioPorId(usuarioId)
        .map { it?.toDomain()?.copy(contrasena = "") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer { PerfilViewModel(context.applicationContext, usuarioId) }
        }
    }
}
