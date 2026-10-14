package com.daeumgil.app.feature.currenttransit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.daeumgil.app.domain.transit.CurrentTransitCodec
import com.daeumgil.app.domain.transit.CurrentTransitContext
import com.daeumgil.app.domain.transit.CurrentTransitRepository
import com.daeumgil.app.domain.transit.StopAnchor
import com.daeumgil.app.domain.transit.StopChoice
import com.daeumgil.app.domain.transit.TransitInputIssue
import com.daeumgil.app.domain.transit.TransitSelectionProvider
import com.daeumgil.app.domain.transit.TransitSelectionRules
import com.daeumgil.app.domain.transit.TransportType
import com.daeumgil.app.domain.transit.VehicleServiceType
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TransitStorageError { READ, WRITE, INVALID_SAVED_SELECTION }
data class CurrentTransitUiState(
    val saved: CurrentTransitContext? = null,
    val draft: CurrentTransitContext = CurrentTransitContext(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: TransitStorageError? = null,
    val issues: List<TransitInputIssue> = emptyList(),
    val showValidation: Boolean = false,
    val saveSucceeded: Boolean = false,
)

class CurrentTransitViewModel(
    private val repository: CurrentTransitRepository,
    val provider: TransitSelectionProvider,
    private val handle: SavedStateHandle,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    val rules = TransitSelectionRules(provider)
    private val restoredDraft: CurrentTransitContext? = handle.get<String>(DRAFT)?.let {
        try { CurrentTransitCodec.decode(it) } catch (_: IllegalArgumentException) { null }
    }
    private val mutableState = MutableStateFlow(CurrentTransitUiState(draft = restoredDraft ?: CurrentTransitContext()))
    val state = mutableState.asStateFlow()
    private var readJob: Job? = null
    private var hasDraft = restoredDraft != null

    init { retryRead() }

    fun retryRead() {
        readJob?.cancel()
        readJob = viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.currentTransit.collect { saved ->
                    val valid = saved == null || rules.validate(saved).isEmpty()
                    mutableState.update { old ->
                        old.copy(saved = saved.takeIf { valid }, isLoading = false,
                            draft = if (hasDraft) old.draft else saved.takeIf { valid }?.copy(onBoardConfirmed = false, confirmationTime = 0) ?: CurrentTransitContext(),
                            error = if (valid) null else TransitStorageError.INVALID_SAVED_SELECTION)
                    }
                    hasDraft = true
                }
            } catch (_: IOException) {
                mutableState.update { it.copy(isLoading = false, error = TransitStorageError.READ) }
            }
        }
    }

    fun beginEditing() {
        if (state.value.isLoading || state.value.isSaving) return
        change(state.value.saved?.copy(onBoardConfirmed = false, confirmationTime = 0) ?: CurrentTransitContext(), resetValidation = true)
    }

    fun cancelEditing() {
        if (state.value.isSaving) return
        handle.remove<String>(DRAFT)
        mutableState.update { it.copy(draft = it.saved ?: CurrentTransitContext(), showValidation = false, issues = emptyList(), saveSucceeded = false) }
    }

    private fun change(context: CurrentTransitContext, resetValidation: Boolean = false) {
        if (state.value.isLoading || state.value.isSaving || state.value.error == TransitStorageError.READ) return
        hasDraft = true
        handle[DRAFT] = CurrentTransitCodec.encode(context)
        mutableState.update { old -> old.copy(draft = context, saveSucceeded = false,
            showValidation = if (resetValidation) false else old.showValidation,
            issues = if (resetValidation || !old.showValidation) emptyList() else rules.validate(context),
            error = old.error.takeIf { it == TransitStorageError.READ }) }
    }

    fun selectTransport(type: TransportType) {
        if (state.value.draft.transportType != type) change(rules.selectTransport(type), resetValidation = true)
    }
    fun selectRegion(id: String) { change(rules.selectRegion(state.value.draft, id)) }
    fun selectRoute(id: String) { change(rules.selectRoute(state.value.draft, id)) }
    fun selectDirection(id: String?) { change(rules.selectDirection(state.value.draft, id)) }
    fun selectService(type: VehicleServiceType) { change(rules.selectService(state.value.draft, type)) }
    fun selectStop(choice: StopChoice) { change(rules.selectStop(state.value.draft, choice)) }
    fun selectAnchor(anchor: StopAnchor) {
        if (state.value.draft.transportType?.isTransit == true && state.value.draft.stopAnchor != anchor) {
            change(state.value.draft.copy(stopAnchor = anchor, onBoardConfirmed = false, confirmationTime = 0))
        }
    }
    fun confirmOnBoard(confirmed: Boolean) { change(state.value.draft.copy(onBoardConfirmed = confirmed, confirmationTime = 0)) }

    fun save() {
        if (state.value.isLoading || state.value.isSaving || state.value.error == TransitStorageError.READ) return
        val issues = rules.validate(state.value.draft)
        mutableState.update { it.copy(issues = issues, showValidation = true, error = null) }
        if (issues.isNotEmpty()) return
        val confirmed = state.value.draft.copy(confirmationTime = now())
        mutableState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.save(confirmed)
                mutableState.update { it.copy(saved = confirmed, draft = confirmed, saveSucceeded = true) }
                handle.remove<String>(DRAFT)
            } catch (_: IOException) {
                mutableState.update { it.copy(error = TransitStorageError.WRITE) }
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun consumeSaveResult() { mutableState.update { it.copy(saveSucceeded = false) } }

    companion object {
        private const val DRAFT = "current-transit-draft-v1"
        fun factory(repository: CurrentTransitRepository, provider: TransitSelectionProvider) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    require(modelClass.isAssignableFrom(CurrentTransitViewModel::class.java))
                    @Suppress("UNCHECKED_CAST")
                    return CurrentTransitViewModel(repository, provider, extras.createSavedStateHandle()) as T
                }
            }
    }
}
