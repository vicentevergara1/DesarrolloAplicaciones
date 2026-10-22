package com.duoc.notix

import com.duoc.notix.domain.AuthRepository
import com.duoc.notix.domain.AuthResult
import com.duoc.notix.domain.User
import com.duoc.notix.ui.auth.AuthField
import com.duoc.notix.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var calls = 0
    private var result: AuthResult = AuthResult.Success(User(1, "Demo", "demo@example.com"))
    private val repository = object : AuthRepository {
        override suspend fun login(email: String, password: String): AuthResult { calls++; return result }
        override suspend fun register(name: String, email: String, password: String): AuthResult { calls++; return result }
    }
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun invalidInputDoesNotCallRepository() = runTest(dispatcher) {
        val model = AuthViewModel(repository)
        model.submit()
        advanceUntilIdle()
        assertEquals(0, calls)
        assertTrue(model.state.value.errors.hasErrors)
    }

    @Test fun loginBlocksDuplicateSubmissionsAndClearsCredentialsAfterSuccess() = runTest(dispatcher) {
        val model = AuthViewModel(repository)
        model.edit(AuthField.EMAIL, "demo@example.com")
        model.edit(AuthField.PASSWORD, "prueba123")
        model.submit()
        model.submit()
        advanceUntilIdle()
        assertEquals(1, calls)
        assertNotNull(model.state.value.user)
        assertEquals("", model.state.value.password)
        model.logout()
        assertNull(model.state.value.user)
    }

    @Test fun invalidCredentialsKeepUserOnLoginAndAllowRetry() = runTest(dispatcher) {
        result = AuthResult.InvalidCredentials
        val model = AuthViewModel(repository)
        model.edit(AuthField.EMAIL, "demo@example.com")
        model.edit(AuthField.PASSWORD, "incorrecta")
        model.submit()
        advanceUntilIdle()
        assertNull(model.state.value.user)
        assertNotNull(model.state.value.message)
        assertFalse(model.state.value.loading)
        result = AuthResult.Success(User(1, "Demo", "demo@example.com"))
        model.edit(AuthField.PASSWORD, "prueba123")
        model.submit()
        advanceUntilIdle()
        assertNotNull(model.state.value.user)
    }
}
