package com.duoc.notix

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.domain.AuthResult
import com.duoc.notix.domain.ProcedureType
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
    private fun open() = Room.databaseBuilder(context, NotixDatabase::class.java, "persistence-test.db").build()
    @Before fun setup() {
        context.deleteDatabase("persistence-test.db")
        database = open()
    }
    @After fun cleanup() {
        database.close()
        context.deleteDatabase("persistence-test.db")
    }

    @Test fun accountAndDraftSurviveDatabaseReopeningAndRemainPrivateToUser() = runBlocking {
        val auth = LocalAuthRepository(database.userDao())
        val first = (auth.register("Demo Uno", "DEMO@example.com ", "prueba123") as AuthResult.Success).user
        val second = (auth.register("Demo Dos", "otra@example.com", "prueba456") as AuthResult.Success).user
        val stored = database.userDao().findByEmail("demo@example.com")!!
        assertEquals(32, stored.passwordHash.size)
        assertEquals(16, stored.passwordSalt.size)
        assertFalse(stored.passwordHash.contentEquals("prueba123".toByteArray()))
        assertEquals(AuthResult.DuplicateEmail, auth.register("Otro Demo", "demo@EXAMPLE.com", "prueba999"))
        assertEquals(AuthResult.InvalidCredentials, auth.login("demo@example.com", "incorrecta"))
        LocalProcedureRepository(database.procedureDao()).createDraft(first.id, ProcedureType.DECLARATION)
        database.close()
        database = open()
        val reopenedAuth = LocalAuthRepository(database.userDao())
        assertEquals(first, (reopenedAuth.login(" demo@EXAMPLE.COM ", "prueba123") as AuthResult.Success).user)
        val drafts = LocalProcedureRepository(database.procedureDao())
        assertEquals(1, drafts.observeForUser(first.id).first().size)
        assertTrue(drafts.observeForUser(second.id).first().isEmpty())
    }

    @Test fun samePasswordUsesDifferentSaltsForDifferentAccounts() = runBlocking {
        val auth = LocalAuthRepository(database.userDao())
        auth.register("Uno Demo", "uno@example.com", "prueba123")
        auth.register("Dos Demo", "dos@example.com", "prueba123")
        val one = database.userDao().findByEmail("uno@example.com")!!
        val two = database.userDao().findByEmail("dos@example.com")!!
        assertFalse(one.passwordSalt.contentEquals(two.passwordSalt))
        assertFalse(one.passwordHash.contentEquals(two.passwordHash))
    }
}
