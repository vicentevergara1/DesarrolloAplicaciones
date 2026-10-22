package com.duoc.notix

import com.duoc.notix.domain.Procedure
import com.duoc.notix.domain.ProcedureRepository
import com.duoc.notix.domain.ProcedureType
import com.duoc.notix.ui.procedures.ProceduresViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
class ProceduresViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val items = MutableStateFlow<List<Procedure>>(emptyList())
    private var calls = 0
    private var failingSave = false
    private var failingLoad = false
    private val repository = object : ProcedureRepository {
        override fun observeForUser(userId: Long) = flow {
            check(userId == 7L)
            if (failingLoad) error("Database unavailable")
            items.collect { emit(it) }
        }
        override suspend fun createDraft(userId: Long, type: ProcedureType) {
            check(userId == 7L)
            calls++
            if (failingSave) error("Disk full")
            items.value = listOf(Procedure(1, type.label, 1))
        }
    }
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun creatingDraftUpdatesListAndPreventsDoubleSave() = runTest(dispatcher) {
        val model = ProceduresViewModel(7, repository)
        advanceUntilIdle()
        model.showCreateDialog(true)
        model.createDraft(ProcedureType.DECLARATION)
        model.createDraft(ProcedureType.DECLARATION)
        advanceUntilIdle()
        assertEquals(1, calls)
        assertEquals(1, model.state.value.items.size)
        assertFalse(model.state.value.showCreateDialog)
        assertFalse(model.state.value.saving)
    }

    @Test fun failedSaveKeepsDialogOpenForRetry() = runTest(dispatcher) {
        val model = ProceduresViewModel(7, repository)
        advanceUntilIdle()
        model.showCreateDialog(true)
        failingSave = true
        model.createDraft(ProcedureType.DECLARATION)
        advanceUntilIdle()
        assertTrue(model.state.value.showCreateDialog)
        assertNotNull(model.state.value.message)
        assertFalse(model.state.value.saving)
        failingSave = false
        model.createDraft(ProcedureType.DECLARATION)
        advanceUntilIdle()
        assertEquals(1, model.state.value.items.size)
    }

    @Test fun failedLoadCanBeRetried() = runTest(dispatcher) {
        failingLoad = true
        val model = ProceduresViewModel(7, repository)
        advanceUntilIdle()
        assertNotNull(model.state.value.message)
        assertFalse(model.state.value.loading)
        failingLoad = false
        model.load()
        advanceUntilIdle()
        assertNull(model.state.value.message)
        assertFalse(model.state.value.loading)
    }
}
