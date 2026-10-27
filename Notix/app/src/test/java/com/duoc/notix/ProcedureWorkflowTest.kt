package com.duoc.notix

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.duoc.notix.data.LocalAuthRepository
import com.duoc.notix.data.LocalProcedureRepository
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.domain.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ProcedureWorkflowTest {
    private lateinit var db: NotixDatabase
    private lateinit var repository: LocalProcedureRepository
    private lateinit var owner: User
    private lateinit var reviewer: User
    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NotixDatabase::class.java).build()
        repository = LocalProcedureRepository(db)
        val auth = LocalAuthRepository(db.userDao())
        owner = (auth.register(registration()) as AuthResult.Success).user
        reviewer = (auth.register(registration("notario@example.com", AccountType.NOTARY, "Notario Demo")) as AuthResult.Success).user
    }
    @After fun cleanup() { db.close() }
    private suspend fun expectDenied(action: suspend () -> Unit) {
        try { action(); fail("La operación debería rechazarse.") } catch (_: DomainException) { }
    }

    @Test fun completeCycleKeepsObservationsDocumentsAndOrderedHistory() = runBlocking {
        val id = repository.createDraft(owner, ProcedureType.DECLARATION)
        assertTrue(repository.observe(reviewer).first().isEmpty())
        val initial = ProcedureForm("Antecedentes ficticios iniciales.", SignatureType.ADVANCED, true)
        repository.save(owner, id, initial, true)
        assertEquals(ProcedureStatus.UNDER_REVIEW, repository.observe(reviewer).first().single().status)
        repository.returnForCorrection(reviewer, id, "Corrige la descripción del documento de prueba.")
        repository.attach(owner, id, Document(name = "correccion.txt", mimeType = "text/plain", sizeBytes = 10, templateText = "Corrección ficticia"))
        assertEquals("Corrige la descripción del documento de prueba.", repository.observe(owner).first().single().observation)
        repository.save(owner, id, initial.copy(details = "Antecedentes corregidos con datos ficticios."), true)
        repository.complete(reviewer, id)
        val item = repository.observe(owner).first().single()
        assertEquals(ProcedureStatus.COMPLETED, item.status)
        assertEquals(2, item.documents.size)
        assertEquals(listOf(ProcedureStatus.DRAFT, ProcedureStatus.UNDER_REVIEW, ProcedureStatus.OBSERVED,
            ProcedureStatus.OBSERVED, ProcedureStatus.UNDER_REVIEW, ProcedureStatus.COMPLETED), item.history.map { it.status })
        assertEquals(AccountType.NOTARY, item.history.last().actorRole)
        assertTrue(item.history.all { it.createdAt > 0 })
        expectDenied { repository.save(owner, id, initial, false) }
        expectDenied { repository.complete(reviewer, id) }
        assertEquals(6, repository.observe(owner).first().single().history.size)
    }

    @Test fun usersCannotEditOtherAccountsOrReviewTheirOwnRequests() = runBlocking {
        val auth = LocalAuthRepository(db.userDao())
        val other = (auth.register(registration("otro@example.com")) as AuthResult.Success).user
        val id = repository.createDraft(owner, ProcedureType.AUTHORIZATION)
        assertTrue(repository.observe(other).first().isEmpty())
        expectDenied { repository.save(other, id, ProcedureForm(), false) }
        expectDenied { repository.complete(owner, id) }
        expectDenied { repository.createDraft(reviewer, ProcedureType.DECLARATION) }
        expectDenied { repository.observe(owner.copy(type = AccountType.NOTARY)).first() }
        assertEquals(1, repository.observe(owner).first().single().history.size)
    }

    @Test fun submissionRequiresDetailsDocumentAndSignatureConfirmation() = runBlocking {
        val id = repository.createDraft(owner, ProcedureType.DECLARATION)
        expectDenied { repository.save(owner, id, ProcedureForm(), true) }
        val form = ProcedureForm("Antecedentes de prueba válidos.", signatureConfirmed = true)
        val document = repository.observe(owner).first().single().documents.single()
        repository.removeDocument(owner, id, document.id)
        expectDenied { repository.save(owner, id, form, true) }
        assertEquals(ProcedureStatus.DRAFT, repository.observe(owner).first().single().status)
    }

    @Test fun observationRequiresReviewStateAndMeaningfulNote() = runBlocking {
        val id = repository.createDraft(owner, ProcedureType.DECLARATION)
        expectDenied { repository.returnForCorrection(reviewer, id, "Corrige el documento ficticio.") }
        repository.save(owner, id, ProcedureForm("Antecedentes de prueba.", signatureConfirmed = true), true)
        expectDenied { repository.returnForCorrection(reviewer, id, "  ") }
        val item = repository.observe(owner).first().single()
        assertEquals(ProcedureStatus.UNDER_REVIEW, item.status)
        assertEquals(2, item.history.size)
    }
}
