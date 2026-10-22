package com.duoc.notix.ui.procedures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.domain.Procedure
import com.duoc.notix.domain.ProcedureRepository
import com.duoc.notix.domain.ProcedureType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProceduresUiState(
    val items: List<Procedure> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val showCreateDialog: Boolean = false,
    val message: String? = null,
)

class ProceduresViewModel(
    private val userId: Long,
    private val repository: ProcedureRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProceduresUiState())
    val state = mutableState.asStateFlow()
    private var observation: kotlinx.coroutines.Job? = null

    init { load() }

    fun load() {
        if (observation?.isActive == true) return
        mutableState.update { it.copy(loading = true, message = null) }
        observation = viewModelScope.launch {
            repository.observeForUser(userId)
                .catch { mutableState.update { it.copy(loading = false, message = "No se pudieron cargar tus trámites.") } }
                .collect { items -> mutableState.update { it.copy(items = items, loading = false) } }
        }
    }

    fun showCreateDialog(show: Boolean) {
        if (!state.value.saving) mutableState.update { it.copy(showCreateDialog = show, message = null) }
    }

    fun createDraft(type: ProcedureType) {
        if (state.value.saving) return
        mutableState.update { it.copy(saving = true, message = null) }
        viewModelScope.launch {
            try {
                repository.createDraft(userId, type)
                if (observation?.isActive != true) load()
                mutableState.update { it.copy(saving = false, showCreateDialog = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(saving = false, message = "No se pudo guardar el borrador. Intenta nuevamente.") }
            }
        }
    }
}
