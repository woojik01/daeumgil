package com.daeumgil.app.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.daeumgil.app.domain.settings.ThemeMode
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferencesSettingsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun defaultThemeIsSystemAndPersistsAfterStoreReopening() = runBlocking {
        val file = File(folder.root, "settings.preferences_pb")
        val firstJob = SupervisorJob()
        val firstStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(firstJob + Dispatchers.IO)) { file }
        val repository = PreferencesSettingsRepository(firstStore)
        try {
            assertEquals(ThemeMode.SYSTEM, repository.settings.first().themeMode)
            repository.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, repository.settings.first().themeMode)
        } finally {
            firstJob.cancelAndJoin()
        }

        val reopenedJob = SupervisorJob()
        val reopenedStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(reopenedJob + Dispatchers.IO)) { file }
        try {
            val reopened = PreferencesSettingsRepository(reopenedStore)
            assertEquals(ThemeMode.DARK, reopened.settings.first().themeMode)
            reopened.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, reopened.settings.first().themeMode)
            reopened.setThemeMode(ThemeMode.SYSTEM)
            assertEquals(ThemeMode.SYSTEM, reopened.settings.first().themeMode)
        } finally {
            reopenedJob.cancelAndJoin()
        }
    }
}
