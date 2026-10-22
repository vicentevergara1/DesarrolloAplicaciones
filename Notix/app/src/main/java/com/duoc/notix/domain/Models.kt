package com.duoc.notix.domain

data class User(val id: Long, val displayName: String, val email: String)

// Estos borradores todavía no incluyen envío ni revisión notarial.
data class Procedure(val id: Long, val title: String, val createdAt: Long)

enum class ProcedureType(val label: String) {
    DECLARATION("Declaración jurada de prueba"),
    AUTHORIZATION("Autorización de prueba"),
    POWER_OF_ATTORNEY("Poder simple de prueba"),
}

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data object DuplicateEmail : AuthResult
    data object InvalidCredentials : AuthResult
}

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
}

interface ProcedureRepository {
    fun observeForUser(userId: Long): kotlinx.coroutines.flow.Flow<List<Procedure>>
    suspend fun createDraft(userId: Long, type: ProcedureType)
}
