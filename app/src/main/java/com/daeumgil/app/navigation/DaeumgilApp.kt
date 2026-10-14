package com.daeumgil.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daeumgil.app.R
import com.daeumgil.app.domain.settings.ThemeMode
import com.daeumgil.app.feature.home.HomeScreen
import com.daeumgil.app.feature.settings.SettingsError
import com.daeumgil.app.feature.settings.SettingsScreen
import com.daeumgil.app.feature.settings.SettingsUiState
import com.daeumgil.app.core.ui.EmptyScreen
import com.daeumgil.app.feature.currenttransit.CurrentTransitScreen
import com.daeumgil.app.feature.currenttransit.CurrentTransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaeumgilApp(
    settingsState: SettingsUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onRetrySettings: () -> Unit,
    currentTransitViewModel: CurrentTransitViewModel,
    navController: NavHostController = rememberNavController(),
) {
    val transitState = currentTransitViewModel.state.collectAsStateWithLifecycle().value
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = AppDestination.entries.firstOrNull { it.route == backStackEntry?.destination?.route }
        ?: AppDestination.HOME
    val navigate: (AppDestination) -> Unit = { target ->
        if (target != AppDestination.CURRENT_SITUATION || (!transitState.isLoading && !transitState.isSaving)) {
            if (target == AppDestination.CURRENT_SITUATION) currentTransitViewModel.beginEditing()
            navController.navigate(target.route) { launchSingleTop = true }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(destination.titleRes)) },
                navigationIcon = {
                    if (destination != AppDestination.HOME) {
                        TextButton(onClick = {
                            if (destination == AppDestination.CURRENT_SITUATION) currentTransitViewModel.cancelEditing()
                            navController.popBackStack()
                        }, enabled = destination != AppDestination.CURRENT_SITUATION || !transitState.isSaving,
                            modifier = Modifier.testTag("back")) {
                            Text(stringResource(R.string.go_back))
                        }
                    }
                },
                actions = {
                    if (destination == AppDestination.HOME) {
                        TextButton(onClick = { navigate(AppDestination.SETTINGS) }, modifier = Modifier.testTag("open-settings")) {
                            Text(stringResource(R.string.settings))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            settingsState.error?.let { error ->
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text(
                            stringResource(if (error == SettingsError.READ) R.string.settings_read_error else R.string.settings_write_error),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        if (error == SettingsError.READ) {
                            TextButton(onClick = onRetrySettings) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                // Retain the graph when loading settings again, so retry never resets the back stack.
                NavHost(
                    navController = navController,
                    startDestination = AppDestination.HOME.route,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                ) {
                    composable(AppDestination.HOME.route) { HomeScreen(navigate, transitState, currentTransitViewModel.provider) }
                    composable(AppDestination.CURRENT_SITUATION.route) {
                        CurrentTransitScreen(transitState, currentTransitViewModel,
                            onBack = { navController.popBackStack() }, onSaved = { navController.popBackStack() })
                    }
                    composable(AppDestination.DESTINATION_SEARCH.route) {
                        EmptyScreen(R.string.destination_preparing, R.string.destination_preparing_body)
                    }
                    composable(AppDestination.ROUTE_RESULTS.route) {
                        EmptyScreen(R.string.routes_empty, R.string.routes_empty_body)
                    }
                    composable(AppDestination.NAVIGATION.route) {
                        EmptyScreen(R.string.navigation_empty, R.string.navigation_empty_body)
                    }
                    composable(AppDestination.ROUTE_RECOVERY.route) {
                        EmptyScreen(R.string.recovery_empty, R.string.recovery_empty_body)
                    }
                    composable(AppDestination.FAVORITES.route) { EmptyScreen(R.string.no_favorites) }
                    composable(AppDestination.SETTINGS.route) { SettingsScreen(settingsState, onThemeModeChange) }
                }
                if (settingsState.isLoading) {
                    Surface(Modifier.fillMaxSize()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.loading), Modifier.padding(24.dp))
                        }
                    }
                }
            }
        }
    }
}
