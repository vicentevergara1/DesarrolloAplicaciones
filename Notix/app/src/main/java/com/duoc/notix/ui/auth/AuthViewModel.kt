package com.duoc.notix.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, REGISTER }
enum class AuthField { EMAIL, PASSWORD, CONFIRMATION }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val accountType: AccountType = AccountType.PERSON,
    val profiles: Map<AccountType, Map<String, String>> = emptyMap(),
    val password: String = "",
    val confirmation: String = "",
    val errors: Map<String, String> = emptyMap(),
    val validationAttempted: Boolean = false,
    val message: String? = null,
    val loading: Boolean = false,
    val user: User? = null,
) {
    val values: Map<String, String> get() = profiles[accountType].orEmpty()
    val email: String get() = values["correo"].orEmpty()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()

    fun switchMode() {
        if (!state.value.loading) mutableState.update { AuthUiState(mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN) }
    }

    fun chooseType(type: AccountType) {
        if (state.value.loading || state.value.accountType == type) return
        mutableState.update { it.copy(accountType = type, password = "", confirmation = "", errors = emptyMap(), validationAttempted = false, message = null) }
    }

    fun fillExample() {
        if (state.value.loading) return
        mutableState.update {
            it.copy(profiles = it.profiles + (it.accountType to RegistrationRules.example(it.accountType)),
                password = RegistrationRules.DEMO_PASSWORD, confirmation = RegistrationRules.DEMO_PASSWORD,
                errors = emptyMap(), validationAttempted = false, message = null)
        }
    }

    fun editProfile(key: String, value: String) {
        if (state.value.loading) return
        mutableState.update {
            val profile = it.values + (key to RegistrationRules.format(key, value))
            revalidate(it.copy(profiles = it.profiles + (it.accountType to profile)), setOf(key))
        }
    }

    fun edit(field: AuthField, value: String) {
        when (field) {
            AuthField.EMAIL -> editProfile("correo", value)
            else -> if (!state.value.loading) mutableState.update {
                val edited = if (field == AuthField.PASSWORD) it.copy(password = value) else it.copy(confirmation = value)
                revalidate(edited, setOf("password", "confirmation"))
            }
        }
    }

    private fun errorsFor(input: AuthUiState): Map<String, String> =
        if (input.mode == AuthMode.REGISTER)
            RegistrationRules.validate(input.accountType, input.values, input.password, input.confirmation)
        else AuthValidation.loginErrors(input.email, input.password)

    /** Tras un intento, un cambio de texto no equivale a que el campo ya sea válido. */
    private fun revalidate(input: AuthUiState, changedKeys: Set<String>): AuthUiState {
        val localErrors = if (input.validationAttempted) errorsFor(input).filterKeys { it in changedKeys } else emptyMap()
        return input.copy(errors = (input.errors - changedKeys) + localErrors, message = null)
    }

    fun submit() {
        val input = state.value
        if (input.loading || input.user != null) return
        val registering = input.mode == AuthMode.REGISTER
        val errors = errorsFor(input)
        if (errors.isNotEmpty()) {
            mutableState.update { it.copy(errors = errors, validationAttempted = true, message = "Revisa los campos marcados.") }
            return
        }
        mutableState.update { it.copy(loading = true, validationAttempted = true, message = null, errors = emptyMap()) }
        viewModelScope.launch {
            try {
                val result = if (registering) repository.register(Registration(input.accountType, input.values, input.password))
                else repository.login(input.email, input.password)
                mutableState.update {
                    when (result) {
                        is AuthResult.Success -> AuthUiState(user = result.user)
                        AuthResult.DuplicateEmail -> it.copy(loading = false, errors = mapOf("correo" to "Este correo ya está registrado. Inicia sesión."))
                        AuthResult.InvalidCredentials -> it.copy(loading = false, message = "Correo o contraseña incorrectos.")
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(loading = false, message = if (error is DomainException) error.message else "No se pudo acceder a los datos locales. Intenta nuevamente.") }
            }
        }
    }

    fun logout() { mutableState.value = AuthUiState() }
}
