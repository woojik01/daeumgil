package com.daeumgil.app.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daeumgil.app.R
import com.daeumgil.app.core.ui.ScreenContent
import com.daeumgil.app.navigation.AppDestination
import com.daeumgil.app.domain.transit.TransitSelectionProvider
import com.daeumgil.app.feature.currenttransit.CurrentTransitUiState
import com.daeumgil.app.feature.currenttransit.SampleTransitNotice
import com.daeumgil.app.feature.currenttransit.TransitStorageError
import com.daeumgil.app.feature.currenttransit.transitSummary
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(onNavigate: (AppDestination) -> Unit, transitState: CurrentTransitUiState, provider: TransitSelectionProvider) {
    ScreenContent {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.slogan), color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.home_prompt), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.home_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (provider.isSample) SampleTransitNotice()
        val currentLabel = when {
            transitState.isLoading -> stringResource(R.string.transit_loading)
            transitState.error == TransitStorageError.READ -> stringResource(R.string.transit_read_error)
            transitState.error == TransitStorageError.INVALID_SAVED_SELECTION -> stringResource(R.string.transit_invalid_saved)
            else -> transitSummary(transitState.saved, provider)
        }
        SelectionCard(
            R.string.select_current_situation, currentLabel, "select-current", !transitState.isLoading,
        ) { onNavigate(AppDestination.CURRENT_SITUATION) }
        transitState.saved?.let { saved ->
            Text(stringResource(R.string.saved_current_notice), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.saved_current_time, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(saved.confirmationTime))),
                style = MaterialTheme.typography.bodySmall)
        }
        SelectionCard(
            R.string.select_destination, stringResource(R.string.destination_not_selected), "select-destination",
        ) { onNavigate(AppDestination.DESTINATION_SEARCH) }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("find-route")) {
                Text(stringResource(R.string.find_route))
            }
            Text(stringResource(R.string.route_requirements), style = MaterialTheme.typography.bodySmall)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.recent_destinations), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.no_recent_destinations), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = { onNavigate(AppDestination.FAVORITES) }, modifier = Modifier.testTag("open-favorites")) {
            Text(stringResource(R.string.favorites))
        }
    }
}

@Composable
private fun SelectionCard(@StringRes title: Int, subtitle: String, tag: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().testTag(tag)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
