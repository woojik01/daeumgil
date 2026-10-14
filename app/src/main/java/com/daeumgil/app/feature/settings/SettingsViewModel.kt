package com.daeumgil.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daeumgil.app.domain.settings.AppSettings
import com.daeumgil.app.domain.settings.SettingsRepository
import com.daeumgil.app.domain.settings.ThemeMode
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SettingsError { READ, WRITE }

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: SettingsError? = null,
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state = mutableState.asStateFlow()
    private var readJob: Job? = null

    init { retryRead() }

    fun retryRead() {
        readJob?.cancel()
        readJob = viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.settings.collect { settings ->
                    mutableState.update { it.copy(settings = settings, isLoading = false, error = null) }
                }
            } catch (_: IOException) {
                mutableState.update { it.copy(isLoading = false, error = SettingsError.READ) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        if (state.value.isLoading || state.value.isSaving || state.value.error == SettingsError.READ) return
        mutableState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.setThemeMode(mode)
            } catch (_: IOException) {
                mutableState.update { it.copy(error = SettingsError.WRITE) }
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    companion object {
        fun factory(repository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(SettingsViewModel::class.java))
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(repository) as T
                }
            }
    }
}

