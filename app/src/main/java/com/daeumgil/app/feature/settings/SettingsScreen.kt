package com.daeumgil.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.daeumgil.app.BuildConfig
import com.daeumgil.app.R
import com.daeumgil.app.core.ui.ScreenContent
import com.daeumgil.app.domain.settings.ThemeMode

@Composable
fun SettingsScreen(state: SettingsUiState, onThemeModeChange: (ThemeMode) -> Unit) {
    ScreenContent {
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleLarge)
        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                val selected = state.settings.themeMode == mode
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .selectable(
                            selected = selected,
                            enabled = !state.isLoading && !state.isSaving && state.error != SettingsError.READ,
                            role = Role.RadioButton,
                            onClick = { onThemeModeChange(mode) },
                        ).testTag("theme-${mode.storageValue}").padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Text(
                        stringResource(when (mode) {
                            ThemeMode.SYSTEM -> R.string.theme_system
                            ThemeMode.LIGHT -> R.string.theme_light
                            ThemeMode.DARK -> R.string.theme_dark
                        }),
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                }
            }
        }
        Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.permission_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.data_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.data_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodySmall)
    }
}
