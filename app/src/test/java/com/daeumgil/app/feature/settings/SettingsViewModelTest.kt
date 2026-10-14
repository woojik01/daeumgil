package com.daeumgil.app.feature.settings

import com.daeumgil.app.domain.settings.AppSettings
import com.daeumgil.app.domain.settings.SettingsRepository
import com.daeumgil.app.domain.settings.ThemeMode
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun writeFailureKeepsLastSavedThemeAndCanBeRetried() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()
        repository.failWrite = true
        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(SettingsError.WRITE, viewModel.state.value.error)
        assertEquals(ThemeMode.SYSTEM, viewModel.state.value.settings.themeMode)
        assertFalse(viewModel.state.value.isSaving)

        repository.failWrite = false
        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, viewModel.state.value.settings.themeMode)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test fun readFailureIsVisibleAndSuccessfulRetryRestoresSettings() = runTest(dispatcher) {
        val repository = FakeRepository()
        repository.failRead = true
        val viewModel = SettingsViewModel(repository)
        advanceUntilIdle()
        assertEquals(SettingsError.READ, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        viewModel.setThemeMode(ThemeMode.LIGHT)
        assertEquals(0, repository.writeCount)

        repository.failRead = false
        repository.value.value = AppSettings(ThemeMode.DARK)
        viewModel.retryRead()
        advanceUntilIdle()
        assertEquals(ThemeMode.DARK, viewModel.state.value.settings.themeMode)
        assertEquals(null, viewModel.state.value.error)
    }

    private class FakeRepository : SettingsRepository {
        val value = MutableStateFlow(AppSettings())
        var failRead = false
        var failWrite = false
        var writeCount = 0
        override val settings: Flow<AppSettings>
            get() = if (failRead) flow { throw IOException("Unavailable") } else value
        override suspend fun setThemeMode(themeMode: ThemeMode) {
            writeCount++
            if (failWrite) throw IOException("Unavailable")
            value.value = AppSettings(themeMode)
        }
    }
}

