package com.duoc.notix.data

import com.duoc.notix.data.local.UserDao
import com.duoc.notix.data.local.UserEntity
import com.duoc.notix.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LocalAuthRepository(private val users: UserDao, private val hasher: PasswordHasher = PasswordHasher()) : AuthRepository {
    override suspend fun register(registration: Registration): AuthResult = withContext(Dispatchers.IO) {
        val errors = RegistrationRules.validate(registration.type, registration.fields, registration.password, registration.password)
        if (errors.isNotEmpty()) throw DomainException("Revisa los datos de la cuenta de prueba.")
        if (users.findByEmail(registration.email) != null) return@withContext AuthResult.DuplicateEmail
        val salt = hasher.newSalt()
        val fields = RegistrationRules.fieldsFor(registration.type).associate { it.key to registration.fields[it.key].orEmpty().trim() } + ("correo" to registration.email)
        val entity = UserEntity(
            displayName = registration.name, email = registration.email,
            passwordHash = hasher.hash(registration.password, salt), passwordSalt = salt,
            accountType = registration.type.name, profileJson = JSONObject(fields).toString(),
        )
        val id = users.insert(entity)
        if (id == -1L) AuthResult.DuplicateEmail else AuthResult.Success(entity.toUser(id))
    }

    override suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val user = users.findByEmail(AuthValidation.normalizeEmail(email))
        if (user != null && hasher.matches(password, user.passwordSalt, user.passwordHash)) AuthResult.Success(user.toUser())
        else AuthResult.InvalidCredentials
    }
}

internal fun UserEntity.toUser(userId: Long = id) = User(userId, displayName, email, AccountType.valueOf(accountType))
internal fun decodeProfile(json: String): Map<String, String> {
    val value = JSONObject(json)
    return value.keys().asSequence().associateWith { value.optString(it) }
}
