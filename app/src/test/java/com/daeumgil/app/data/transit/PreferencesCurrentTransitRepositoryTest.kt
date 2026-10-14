package com.daeumgil.app.data.transit

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.daeumgil.app.domain.transit.CurrentTransitContext
import com.daeumgil.app.domain.transit.StopAnchor
import com.daeumgil.app.domain.transit.TransportType
import com.daeumgil.app.domain.transit.VehicleServiceType
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferencesCurrentTransitRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun selectionPersistsAfterClosingAndReopeningAndCanBeCleared() = runBlocking {
        val file = File(folder.root, "transit.preferences_pb")
        val expected = CurrentTransitContext(TransportType.SUBWAY, "sample-v1", "sample-east", "sample-subway-a",
            null, true, VehicleServiceType.EXPRESS, "sample-start", null, StopAnchor.LAST_PASSED, true, 1234)
        val firstJob = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(firstJob + Dispatchers.IO)) { file }
        try {
            val repository = PreferencesCurrentTransitRepository(store)
            assertNull(repository.currentTransit.first())
            repository.save(expected)
        } finally { firstJob.cancelAndJoin() }
        val secondJob = SupervisorJob()
        val reopened = PreferenceDataStoreFactory.create(scope = CoroutineScope(secondJob + Dispatchers.IO)) { file }
        try {
            val repository = PreferencesCurrentTransitRepository(reopened)
            assertEquals(expected, repository.currentTransit.first())
            repository.clear()
            assertNull(repository.currentTransit.first())
        } finally { secondJob.cancelAndJoin() }
    }

    @Test fun corruptedSelectionSurfacesReadFailureInsteadOfInventingDefaults() = runBlocking {
        val file = File(folder.root, "corrupt.preferences_pb")
        val job = SupervisorJob()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(job + Dispatchers.IO)) { file }
        try {
            store.edit { it[stringPreferencesKey("current_transit_v1")] = "broken!" }
            try {
                PreferencesCurrentTransitRepository(store).currentTransit.first()
                fail("Corrupt selection must be reported")
            } catch (_: IOException) { /* Expected read error. */ }
        } finally { job.cancelAndJoin() }
    }
}
