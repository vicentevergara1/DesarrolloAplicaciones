package com.duoc.notix.ui.procedures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ProcedureDialog { CREATE, SUBMIT, RETURN, COMPLETE, DISCARD }

data class ProceduresUiState(
    val items: List<Procedure> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val selectedId: Long? = null,
    val form: ProcedureForm = ProcedureForm(),
    val dirty: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val dialog: ProcedureDialog? = null,
    val reviewNote: String = "",
    val message: String? = null,
) {
    val selected: Procedure? get() = items.firstOrNull { it.id == selectedId }
}

class ProceduresViewModel(
    val user: User,
    private val repository: ProcedureRepository,
    private val documents: DocumentStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProceduresUiState())
    val state = mutableState.asStateFlow()
    private var observation: Job? = null

    init { start() }

    fun start() {
        if (observation?.isActive == true) return
        mutableState.update { it.copy(loading = true, message = null) }
        observation = viewModelScope.launch {
            repository.observe(user)
                .catch { error -> mutableState.update { it.copy(loading = false, message = errorText(error, "No se pudieron cargar los trámites. Reintenta.")) } }
                .collect { items ->
                    mutableState.update { old ->
                        val selected = items.firstOrNull { it.id == old.selectedId }
                        old.copy(items = items, loading = false,
                            form = if (!old.dirty && selected != null) selected.form else old.form)
                    }
                }
        }
    }

    fun endSession() {
        viewModelScope.coroutineContext.cancelChildren()
        observation = null
        mutableState.value = ProceduresUiState()
    }

    fun open(id: Long) {
        if (state.value.busy) return
        val item = state.value.items.firstOrNull { it.id == id } ?: return
        mutableState.update { it.copy(selectedId = id, form = item.form, dirty = false, errors = emptyMap(), message = null) }
    }

    fun requestBack() {
        if (state.value.busy) return
        if (state.value.dirty) showDialog(ProcedureDialog.DISCARD)
        else closeDetail()
    }

    fun closeDetail() {
        mutableState.update { it.copy(selectedId = null, dirty = false, dialog = null, errors = emptyMap(), message = null) }
    }

    fun editForm(form: ProcedureForm) {
        val item = state.value.selected ?: return
        if (state.value.busy || !canEdit(item)) return
        mutableState.update { it.copy(form = form, dirty = form != item.form, errors = emptyMap(), message = null) }
    }

    private fun canEdit(item: Procedure) = user.type != AccountType.NOTARY && item.userId == user.id && item.status.editable

    fun showDialog(dialog: ProcedureDialog?) {
        if (!state.value.busy) mutableState.update { it.copy(dialog = dialog, errors = emptyMap(), message = null, reviewNote = "") }
    }

    fun note(value: String) { mutableState.update { it.copy(reviewNote = value, errors = it.errors - "note") } }
    fun message(value: String) { mutableState.update { it.copy(message = value) } }

    fun createDraft(type: ProcedureType) = action {
        val id = repository.createDraft(user, type)
        mutableState.update { it.copy(selectedId = id, form = ProcedureForm(), dirty = false, dialog = null) }
    }

    fun requestSubmit() {
        val input = state.value
        val item = input.selected ?: return
        if (input.busy || !canEdit(item)) return
        val errors = ProcedureRules.errors(input.form, item.documents.size, true)
        mutableState.update { it.copy(errors = errors, message = if (errors.isEmpty()) null else "Revisa los antecedentes, documentos y firma.", dialog = if (errors.isEmpty()) ProcedureDialog.SUBMIT else null) }
    }

    fun save(submit: Boolean) {
        val input = state.value
        val item = input.selected ?: return
        if (!canEdit(item)) return
        val errors = ProcedureRules.errors(input.form, item.documents.size, submit)
        if (errors.isNotEmpty()) {
            mutableState.update { it.copy(errors = errors, dialog = null, message = "Revisa los campos del trámite.") }
            return
        }
        action {
            repository.save(user, item.id, input.form, submit)
            mutableState.update { it.copy(dirty = false, dialog = null, message = if (submit) "Trámite enviado a revisión." else "Cambios guardados.") }
        }
    }

    fun attach(uri: String) {
        val item = state.value.selected ?: return
        if (!canEdit(item)) return
        action {
            val copied = documents.copyFrom(uri)
            try {
                repository.attach(user, item.id, copied)
            } catch (error: Exception) {
                withContext(NonCancellable) { documents.delete(copied) }
                throw error
            }
            mutableState.update { it.copy(message = "Documento ficticio guardado en este dispositivo.") }
        }
    }

    fun remove(document: Document) {
        val item = state.value.selected ?: return
        if (!canEdit(item)) return
        action {
            repository.removeDocument(user, item.id, document.id)
            documents.delete(document)
            mutableState.update { it.copy(message = "Documento retirado.") }
        }
    }

    fun returnForCorrection() {
        val input = state.value
        val item = input.selected ?: return
        if (input.reviewNote.trim().length !in 5..1000) {
            mutableState.update { it.copy(errors = mapOf("note" to "Escribe entre 5 y 1000 caracteres.")) }
            return
        }
        action {
            repository.returnForCorrection(user, item.id, input.reviewNote)
            mutableState.update { it.copy(dialog = null, reviewNote = "", message = "Observaciones enviadas al solicitante.") }
        }
    }

    fun complete() {
        val item = state.value.selected ?: return
        action {
            repository.complete(user, item.id)
            mutableState.update { it.copy(dialog = null, message = "Trámite de prueba finalizado.") }
        }
    }

    private fun action(block: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            try {
                block()
                if (observation?.isActive != true) start()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(message = errorText(error, "No se pudo completar la operación. Intenta nuevamente.")) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }

    private fun errorText(error: Throwable, fallback: String) = if (error is DomainException) error.message ?: fallback else fallback
}
