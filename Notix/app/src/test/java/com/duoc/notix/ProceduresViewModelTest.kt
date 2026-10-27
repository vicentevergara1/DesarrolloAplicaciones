package com.duoc.notix

import com.duoc.notix.domain.*
import com.duoc.notix.ui.procedures.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProceduresViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val user = User(7, "Persona Demo", "demo@example.com")
    private val items = MutableStateFlow<List<Procedure>>(emptyList())
    private val documents = FakeDocumentStore()
    private var calls = 0
    private var failingSave = false
    private var failingLoad = false
    private var failingAttach = false
    private val repository = object : ProcedureRepository {
        override fun observe(actor: User) = flow {
            check(actor == user)
            if (failingLoad) error("Database unavailable")
            items.collect { emit(it) }
        }
        override suspend fun createDraft(actor: User, type: ProcedureType): Long {
            calls++
            if (failingSave) error("Disk full")
            items.value = listOf(testProcedure())
            return 1
        }
        override suspend fun save(actor: User, id: Long, form: ProcedureForm, submit: Boolean) {
            items.value = items.value.map { it.copy(form = form, status = if (submit) ProcedureStatus.UNDER_REVIEW else it.status) }
        }
        override suspend fun attach(actor: User, id: Long, document: Document) {
            if (failingAttach) throw DomainException("No se pudo asociar.")
        }
        override suspend fun removeDocument(actor: User, id: Long, documentId: Long) = Unit
        override suspend fun returnForCorrection(actor: User, id: Long, note: String) = Unit
        override suspend fun complete(actor: User, id: Long) = Unit
    }
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    private fun model() = ProceduresViewModel(user, repository, documents)

    @Test fun creatingDraftPreventsDoubleSubmissionAndOpensDetail() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        model.showDialog(ProcedureDialog.CREATE)
        model.createDraft(ProcedureType.DECLARATION)
        model.createDraft(ProcedureType.DECLARATION)
        advanceUntilIdle()
        assertEquals(1, calls)
        assertEquals(1L, model.state.value.selectedId)
        assertNull(model.state.value.dialog)
    }

    @Test fun failedCreateKeepsDialogOpenForRetry() = runTest(dispatcher) {
        val model = model()
        advanceUntilIdle()
        model.showDialog(ProcedureDialog.CREATE)
        failingSave = true
        model.createDraft(ProcedureType.DECLARATION)
        advanceUntilIdle()
        assertEquals(ProcedureDialog.CREATE, model.state.value.dialog)
        assertNotNull(model.state.value.message)
        failingSave = false
        model.createDraft(ProcedureType.DECLARATION)
        advanceUntilIdle()
        assertNotNull(model.state.value.selected)
    }

    @Test fun failedLoadCanBeRetried() = runTest(dispatcher) {
        failingLoad = true
        val model = model()
        advanceUntilIdle()
        assertNotNull(model.state.value.message)
        failingLoad = false
        model.start()
        advanceUntilIdle()
        assertNull(model.state.value.message)
        assertFalse(model.state.value.loading)
    }

    @Test fun unsavedEditsRequireConfirmationAndInvalidSubmissionIsBlocked() = runTest(dispatcher) {
        items.value = listOf(testProcedure())
        val model = model()
        advanceUntilIdle()
        model.open(1)
        model.requestSubmit()
        assertTrue(model.state.value.errors.containsKey("details"))
        model.editForm(ProcedureForm("Datos ficticios sin guardar."))
        model.requestBack()
        assertEquals(ProcedureDialog.DISCARD, model.state.value.dialog)
        model.closeDetail()
        assertNull(model.state.value.selectedId)
    }

    @Test fun failedAttachmentDeletesOnlyTheNewPrivateCopy() = runTest(dispatcher) {
        items.value = listOf(testProcedure())
        val model = model()
        advanceUntilIdle()
        model.open(1)
        failingAttach = true
        model.attach("content://prueba/documento")
        advanceUntilIdle()
        assertEquals(1, documents.deleted.size)
        assertEquals("content://prueba/documento", documents.deleted.single().fileUri)
        assertNotNull(model.state.value.message)
    }

    @Test fun logoutClearsVisibleDataAndStopsObservation() = runTest(dispatcher) {
        items.value = listOf(testProcedure())
        val model = model()
        advanceUntilIdle()
        model.open(1)
        model.endSession()
        items.value = listOf(testProcedure(2))
        advanceUntilIdle()
        assertTrue(model.state.value.items.isEmpty())
        assertNull(model.state.value.selectedId)
        model.start()
        advanceUntilIdle()
        assertEquals(2L, model.state.value.items.single().id)
    }
}
