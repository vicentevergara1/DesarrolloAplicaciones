package com.duoc.notix.data

import com.duoc.notix.data.local.UserDao
import com.duoc.notix.data.local.UserEntity
import com.duoc.notix.domain.AuthRepository
import com.duoc.notix.domain.AuthResult
import com.duoc.notix.domain.AuthValidation
import com.duoc.notix.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAuthRepository(
    private val users: UserDao,
    private val hasher: PasswordHasher = PasswordHasher(),
) : AuthRepository {
    override suspend fun register(name: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val normalizedEmail = AuthValidation.normalizeEmail(email)
            if (users.findByEmail(normalizedEmail) != null) return@withContext AuthResult.DuplicateEmail
            val salt = hasher.newSalt()
            val entity = UserEntity(
                displayName = name.trim(), email = normalizedEmail,
                passwordHash = hasher.hash(password, salt), passwordSalt = salt,
            )
            val id = users.insert(entity)
            if (id == -1L) AuthResult.DuplicateEmail
            else AuthResult.Success(User(id, entity.displayName, entity.email))
        }

    override suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val user = users.findByEmail(AuthValidation.normalizeEmail(email))
            if (user != null && hasher.matches(password, user.passwordSalt, user.passwordHash)) {
                AuthResult.Success(User(user.id, user.displayName, user.email))
            } else {
                AuthResult.InvalidCredentials
            }
        }
}
