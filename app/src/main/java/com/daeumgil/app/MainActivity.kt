package com.daeumgil.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daeumgil.app.core.ui.DaeumgilTheme
import com.daeumgil.app.feature.settings.SettingsViewModel
import com.daeumgil.app.navigation.DaeumgilApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as DaeumgilApplication).container.settingsRepository
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(repository))
            val state = settingsViewModel.state.collectAsStateWithLifecycle().value
            val isDark = state.settings.themeMode.isDark(isSystemInDarkTheme())
            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(0xE6FFFFFF.toInt(), 0x801B1B1B.toInt()) { isDark },
                )
            }
            DaeumgilTheme(state.settings.themeMode) {
                DaeumgilApp(state, settingsViewModel::setThemeMode, settingsViewModel::retryRead)
            }
        }
    }
}

