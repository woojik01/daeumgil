package com.daeumgil.app.feature.currenttransit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.daeumgil.app.R
import com.daeumgil.app.core.ui.ScreenContent
import com.daeumgil.app.domain.transit.StopAnchor
import com.daeumgil.app.domain.transit.TransportType
import com.daeumgil.app.domain.transit.line
import com.daeumgil.app.domain.transit.stopChoices

@Composable
fun SampleTransitNotice() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.sample_data), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.sample_data_notice), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CurrentTransitScreen(
    state: CurrentTransitUiState,
    viewModel: CurrentTransitViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val provider = viewModel.provider
    val draft = state.draft
    val line = provider.line(draft.routeId)
    val enabled = !state.isLoading && !state.isSaving && state.error != TransitStorageError.READ
    var picker by rememberSaveable { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) { viewModel.consumeSaveResult(); onSaved() }
    }
    BackHandler { if (!state.isSaving) { viewModel.cancelEditing(); onBack() } }

    Column(Modifier.fillMaxSize().imePadding()) {
        Box(Modifier.weight(1f)) {
            ScreenContent {
                if (provider.isSample) SampleTransitNotice()
                state.error?.let { error ->
                    Text(stringResource(when (error) {
                        TransitStorageError.READ -> R.string.transit_read_error
                        TransitStorageError.WRITE -> R.string.transit_write_error
                        TransitStorageError.INVALID_SAVED_SELECTION -> R.string.transit_invalid_saved
                    }), color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    if (error == TransitStorageError.READ) TextButton(onClick = viewModel::retryRead) { Text(stringResource(R.string.retry)) }
                }
                Text(stringResource(R.string.transport_prompt), style = MaterialTheme.typography.titleLarge)
                Column(Modifier.selectableGroup()) {
                    TransportType.entries.forEach { type ->
                        ChoiceRow(stringResource(type.labelRes()), draft.transportType == type, "transport-${type.name}", enabled) {
                            viewModel.selectTransport(type)
                        }
                    }
                }
                if (draft.transportType == TransportType.BUS) {
                    PickerButton(stringResource(R.string.region), provider.regions.firstOrNull { it.id == draft.regionId }?.name,
                        "pick-region", enabled) { picker = "region" }
                }
                if (draft.transportType?.isTransit == true) {
                    PickerButton(stringResource(R.string.line), line?.let { "${it.name} · ${it.detail}" }, "pick-route",
                        enabled && (draft.transportType != TransportType.BUS || draft.regionId != null)) { picker = "route" }
                    if (line != null) {
                        if (line.services.size > 1) {
                            Text(stringResource(R.string.service_type), style = MaterialTheme.typography.titleMedium)
                            Column(Modifier.selectableGroup()) {
                                line.services.forEach { service ->
                                    ChoiceRow(stringResource(service.labelRes()), draft.serviceType == service, "service-${service.name}", enabled) {
                                        viewModel.selectService(service)
                                    }
                                }
                            }
                        }
                        PickerButton(stringResource(R.string.direction),
                            if (draft.directionUnknown) stringResource(R.string.direction_unknown) else line.directions.firstOrNull { it.id == draft.directionId }?.label,
                            "pick-direction", enabled) { picker = "direction" }
                        if (draft.directionUnknown) Text(stringResource(R.string.direction_unknown_notice), color = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.position_anchor), style = MaterialTheme.typography.titleMedium)
                        Column(Modifier.selectableGroup()) {
                            StopAnchor.entries.forEach { anchor ->
                                ChoiceRow(stringResource(anchor.labelRes()), draft.stopAnchor == anchor, "anchor-${anchor.name}", enabled) {
                                    viewModel.selectAnchor(anchor)
                                }
                            }
                        }
                    }
                }
                if (draft.transportType != null) {
                    val selectedStop = provider.places.firstOrNull { it.id == draft.stopId }
                    PickerButton(stringResource(R.string.current_stop), selectedStop?.name, "pick-stop",
                        enabled && (draft.transportType.isTransit != true || draft.directionId != null || draft.directionUnknown)) { picker = "stop" }
                }
                if (draft.transportType?.isTransit == true && line != null) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(
                            value = draft.onBoardConfirmed, role = Role.Checkbox, enabled = enabled,
                            onValueChange = viewModel::confirmOnBoard,
                        ).testTag("confirm-onboard").padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(draft.onBoardConfirmed, onCheckedChange = null)
                        Text(stringResource(R.string.confirm_onboard), Modifier.padding(start = 12.dp))
                    }
                }
                if (state.showValidation && state.issues.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.issues.forEach { issue -> Text(stringResource(issue.labelRes()), color = MaterialTheme.colorScheme.error) }
                    }
                }
                Text(stringResource(R.string.selection_summary), style = MaterialTheme.typography.titleMedium)
                Text(transitSummary(draft, provider), Modifier.testTag("draft-summary"))
            }
        }
        Surface(shadowElevation = 2.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.issues.firstOrNull()?.takeIf { state.showValidation }?.let { issue ->
                    Text(stringResource(issue.labelRes()), color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("validation-error").semantics { liveRegion = LiveRegionMode.Polite })
                }
                Button(onClick = { focusManager.clearFocus(); viewModel.save() }, enabled = enabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("save-current")) {
                    Text(stringResource(if (state.isSaving) R.string.saving_current else R.string.save_current))
                }
            }
        }
    }

    picker?.let { kind ->
        var query by rememberSaveable(kind, draft.transportType, draft.regionId, draft.routeId, draft.directionId, draft.serviceType) { mutableStateOf("") }
        val title = stringResource(when (kind) {
            "region" -> R.string.region
            "route" -> R.string.line
            "direction" -> R.string.direction
            else -> R.string.current_stop
        })
        val select: (() -> Unit) -> Unit = { action -> action(); picker = null; focusManager.clearFocus() }
        AlertDialog(
            onDismissRequest = { picker = null },
            title = { Text(title) },
            confirmButton = { TextButton(onClick = { picker = null }) { Text(stringResource(R.string.close_picker)) } },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (kind == "route" || kind == "stop") {
                        OutlinedTextField(query, onValueChange = { query = it }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("picker-query"), label = { Text(stringResource(R.string.search_in_list)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                    }
                    Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()).selectableGroup()) {
                        when (kind) {
                            "region" -> provider.regions.forEach { region ->
                                ChoiceRow(region.name, draft.regionId == region.id, "region-${region.id}") { select { viewModel.selectRegion(region.id) } }
                            }
                            "route" -> {
                                val lines = provider.lines.filter { it.type == draft.transportType &&
                                    (it.type != TransportType.BUS || it.regionId == draft.regionId) &&
                                    (it.name.contains(query, true) || it.detail.contains(query, true)) }
                                if (lines.isEmpty()) Text(stringResource(R.string.no_selection_results))
                                lines.forEach { option -> ChoiceRow("${option.name}\n${option.detail}", draft.routeId == option.id, "route-${option.id}") {
                                    select { viewModel.selectRoute(option.id) }
                                } }
                            }
                            "direction" -> {
                                line?.directions?.forEach { direction ->
                                    ChoiceRow(direction.label, draft.directionId == direction.id, "direction-${direction.id}") {
                                        select { viewModel.selectDirection(direction.id) }
                                    }
                                }
                                ChoiceRow(stringResource(R.string.direction_unknown), draft.directionUnknown, "direction-unknown") {
                                    select { viewModel.selectDirection(null) }
                                }
                            }
                            "stop" -> {
                                val choices = provider.stopChoices(draft).filter { it.stop.name.contains(query, true) }
                                if (choices.isEmpty()) Text(stringResource(R.string.no_selection_results))
                                choices.forEach { choice ->
                                    ChoiceRow(choice.stop.name, draft.stopId == choice.stop.id && draft.stopSequence == choice.sequence,
                                        "stop-${choice.stop.id}-${choice.sequence ?: 0}") { select { viewModel.selectStop(choice) } }
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun PickerButton(title: String, selected: String?, tag: String, enabled: Boolean, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag)) {
            Text(selected ?: stringResource(R.string.choose_from_list))
        }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, tag: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
        .selectable(selected = selected, role = Role.RadioButton, enabled = enabled, onClick = onClick)
        .testTag(tag).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, onClick = null)
        Text(label, Modifier.padding(start = 12.dp))
    }
}
