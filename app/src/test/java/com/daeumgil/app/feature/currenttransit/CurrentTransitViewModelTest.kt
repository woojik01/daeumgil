package com.daeumgil.app.feature.currenttransit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.daeumgil.app.data.transit.SampleTransitSelectionProvider
import com.daeumgil.app.domain.transit.CurrentTransitContext
import com.daeumgil.app.domain.transit.CurrentTransitRepository
import com.daeumgil.app.domain.transit.TransitInputIssue
import com.daeumgil.app.domain.transit.TransportType
import com.daeumgil.app.domain.transit.stopChoices
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class CurrentTransitViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val provider = SampleTransitSelectionProvider()
    private val store = ViewModelStore()
    private var number = 0
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun model(repository: FakeRepository, handle: SavedStateHandle = SavedStateHandle()): CurrentTransitViewModel {
        val model = CurrentTransitViewModel(repository, provider, handle) { 9999 }
        store.put("vm-${number++}", model)
        return model
    }
    private fun walk(model: CurrentTransitViewModel) {
        model.selectTransport(TransportType.WALKING)
        model.selectStop(provider.stopChoices(model.state.value.draft).first())
    }

    @Test fun missingFieldsPreventStorageWrites() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = model(repository)
        advanceUntilIdle()
        model.save()
        assertEquals(listOf(TransitInputIssue.TRANSPORT_REQUIRED), model.state.value.issues)
        assertEquals(0, repository.writes)
    }

    @Test fun writeFailurePreservesDraftAndPreviousSavedValueAndAllowsRetry() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = model(repository)
        advanceUntilIdle()
        walk(model)
        val draft = model.state.value.draft
        repository.failWrite = true
        model.save()
        advanceUntilIdle()
        assertEquals(draft, model.state.value.draft)
        assertNull(model.state.value.saved)
        assertEquals(TransitStorageError.WRITE, model.state.value.error)
        assertFalse(model.state.value.saveSucceeded)
        repository.failWrite = false
        model.save()
        advanceUntilIdle()
        assertEquals(9999L, model.state.value.saved?.confirmationTime)
        assertTrue(model.state.value.saveSucceeded)
    }

    @Test fun sameTransportSelectionKeepsPositionAndChangingTransportResetsIt() = runTest(dispatcher) {
        val model = model(FakeRepository())
        advanceUntilIdle()
        walk(model)
        val draft = model.state.value.draft
        model.selectTransport(TransportType.WALKING)
        assertEquals(draft, model.state.value.draft)
        model.selectTransport(TransportType.BUS)
        assertNull(model.state.value.draft.stopId)
        assertNull(model.state.value.draft.routeId)
    }

    @Test fun savedInstanceDraftRestoresWithoutReplacingItWithDiskSelection() = runTest(dispatcher) {
        val repository = FakeRepository()
        val handle = SavedStateHandle()
        val first = model(repository, handle)
        advanceUntilIdle()
        walk(first)
        val copied = SavedStateHandle(mapOf("current-transit-draft-v1" to handle.get<String>("current-transit-draft-v1")))
        val recreated = model(repository, copied)
        advanceUntilIdle()
        assertEquals(first.state.value.draft, recreated.state.value.draft)
    }

    @Test fun readFailureBlocksOverwritingStoredSelectionAndRetryRecovers() = runTest(dispatcher) {
        val repository = FakeRepository()
        repository.failRead = true
        val model = model(repository)
        advanceUntilIdle()
        assertEquals(TransitStorageError.READ, model.state.value.error)
        walk(model)
        model.save()
        advanceUntilIdle()
        assertEquals(0, repository.writes)
        repository.failRead = false
        model.retryRead()
        advanceUntilIdle()
        assertNull(model.state.value.error)
        walk(model)
        model.save()
        advanceUntilIdle()
        assertEquals(TransportType.WALKING, model.state.value.saved?.transportType)
    }

    @Test fun cancellingEditsDoesNotChangeSavedSelection() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = model(repository)
        advanceUntilIdle()
        walk(model)
        model.save()
        advanceUntilIdle()
        val saved = model.state.value.saved
        model.beginEditing()
        model.selectTransport(TransportType.BUS)
        model.cancelEditing()
        assertEquals(saved, model.state.value.draft)
        assertEquals(saved, repository.value.value)
    }

    private class FakeRepository : CurrentTransitRepository {
        val value = MutableStateFlow<CurrentTransitContext?>(null)
        var failRead = false
        var failWrite = false
        var writes = 0
        override val currentTransit: Flow<CurrentTransitContext?>
            get() = if (failRead) flow { throw IOException("Unavailable") } else value
        override suspend fun save(context: CurrentTransitContext) {
            writes++
            if (failWrite) throw IOException("Unavailable")
            value.value = context
        }
        override suspend fun clear() { value.value = null }
    }
}
