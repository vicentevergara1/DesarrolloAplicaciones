package com.duoc.notix

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class LocalPersistenceTest {
    private lateinit var database: NotixDatabase
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private fun open() = Room.databaseBuilder(context, NotixDatabase::class.java, "persistence-test.db")
        .addMigrations(NotixDatabase.MIGRATION_1_2).build()
    @Before fun setup() { context.deleteDatabase("persistence-test.db"); database = open() }
    @After fun cleanup() { database.close(); context.deleteDatabase("persistence-test.db") }

    @Test fun accountsProfilesAndDraftsSurviveDatabaseReopening() = runBlocking {
        val auth = LocalAuthRepository(database.userDao())
        val first = (auth.register(registration("DEMO@example.com ")) as AuthResult.Success).user
        val second = (auth.register(registration("otra@example.com")) as AuthResult.Success).user
        val stored = database.userDao().findByEmail("demo@example.com")!!
        assertEquals(32, stored.passwordHash.size)
        assertEquals(16, stored.passwordSalt.size)
        assertFalse(stored.passwordHash.contentEquals("prueba123".toByteArray()))
        assertTrue(stored.profileJson.contains("111"))
        assertFalse(stored.profileJson.contains("prueba123"))
        assertEquals(AuthResult.DuplicateEmail, auth.register(registration("demo@EXAMPLE.com")))
        assertEquals(AuthResult.InvalidCredentials, auth.login("demo@example.com", "incorrecta"))
        LocalProcedureRepository(database).createDraft(first, ProcedureType.DECLARATION)
        database.close()
        database = open()
        val reopened = LocalAuthRepository(database.userDao())
        assertEquals(first, (reopened.login(" demo@EXAMPLE.COM ", "prueba123") as AuthResult.Success).user)
        val repository = LocalProcedureRepository(database)
        val item = repository.observe(first).first().single()
        assertEquals(1, item.documents.size)
        assertEquals(1, item.history.size)
        assertTrue(repository.observe(second).first().isEmpty())
    }

    @Test fun samePasswordUsesDifferentSaltsAndRoleIsPreserved() = runBlocking {
        val auth = LocalAuthRepository(database.userDao())
        auth.register(registration("uno@example.com"))
        auth.register(registration("notario@example.com", AccountType.NOTARY, "Notario Demo"))
        val one = database.userDao().findByEmail("uno@example.com")!!
        val two = database.userDao().findByEmail("notario@example.com")!!
        assertFalse(one.passwordSalt.contentEquals(two.passwordSalt))
        assertFalse(one.passwordHash.contentEquals(two.passwordHash))
        assertEquals(AccountType.NOTARY, (auth.login("notario@example.com", "prueba123") as AuthResult.Success).user.type)
    }

    @Test fun invalidRegistrationCannotBypassTheViewModel() = runBlocking {
        val auth = LocalAuthRepository(database.userDao())
        try {
            auth.register(registration(password = "        "))
            fail("Debe rechazar la contraseña en blanco.")
        } catch (_: DomainException) { }
        assertNull(database.userDao().findByEmail("demo@example.com"))
    }
}
