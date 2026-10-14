package com.daeumgil.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.daeumgil.app.data.settings.PreferencesSettingsRepository
import com.daeumgil.app.domain.settings.SettingsRepository

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class AppContainer(context: Context) {
    val settingsRepository: SettingsRepository =
        PreferencesSettingsRepository(context.applicationContext.settingsDataStore)
}

