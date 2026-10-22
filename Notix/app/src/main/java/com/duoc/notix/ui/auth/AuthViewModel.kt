package com.duoc.notix.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.domain.AuthErrors
import com.duoc.notix.domain.AuthRepository
import com.duoc.notix.domain.AuthResult
import com.duoc.notix.domain.AuthValidation
import com.duoc.notix.domain.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, REGISTER }
enum class AuthField { NAME, EMAIL, PASSWORD, CONFIRMATION }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val errors: AuthErrors = AuthErrors(),
    val message: String? = null,
    val loading: Boolean = false,
    val user: User? = null,
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()

    fun switchMode() {
        if (state.value.loading) return
        mutableState.update { AuthUiState(mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN) }
    }

    fun edit(field: AuthField, value: String) {
        if (state.value.loading) return
        mutableState.update {
            val cleared = it.copy(message = null, errors = AuthErrors())
            when (field) {
                AuthField.NAME -> cleared.copy(name = value)
                AuthField.EMAIL -> cleared.copy(email = value)
                AuthField.PASSWORD -> cleared.copy(password = value)
                AuthField.CONFIRMATION -> cleared.copy(confirmation = value)
            }
        }
    }

    fun submit() {
        val input = state.value
        if (input.loading || input.user != null) return
        val registering = input.mode == AuthMode.REGISTER
        val errors = AuthValidation.validate(registering, input.name, input.email, input.password, input.confirmation)
        if (errors.hasErrors) {
            mutableState.update { it.copy(errors = errors, message = null) }
            return
        }
        mutableState.update { it.copy(loading = true, message = null, errors = AuthErrors()) }
        viewModelScope.launch {
            try {
                val result = if (registering) repository.register(input.name, input.email, input.password)
                else repository.login(input.email, input.password)
                mutableState.update {
                    when (result) {
                        is AuthResult.Success -> AuthUiState(user = result.user)
                        AuthResult.DuplicateEmail -> it.copy(loading = false, errors = AuthErrors(email = "Este correo ya está registrado."))
                        AuthResult.InvalidCredentials -> it.copy(loading = false, message = "Correo o contraseña incorrectos.")
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, message = "No se pudo acceder a los datos locales. Intenta nuevamente.") }
            }
        }
    }

    fun logout() { mutableState.value = AuthUiState() }
}
