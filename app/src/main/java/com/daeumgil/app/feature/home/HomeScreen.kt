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

@Composable
fun HomeScreen(onNavigate: (AppDestination) -> Unit) {
    ScreenContent {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.slogan), color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.home_prompt), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.home_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SelectionCard(
            R.string.select_current_situation, R.string.current_not_selected, "select-current",
        ) { onNavigate(AppDestination.CURRENT_SITUATION) }
        SelectionCard(
            R.string.select_destination, R.string.destination_not_selected, "select-destination",
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
private fun SelectionCard(@StringRes title: Int, @StringRes subtitle: Int, tag: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag(tag)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

