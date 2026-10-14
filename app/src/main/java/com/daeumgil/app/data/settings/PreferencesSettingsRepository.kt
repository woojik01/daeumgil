package com.daeumgil.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.daeumgil.app.domain.settings.AppSettings
import com.daeumgil.app.domain.settings.SettingsRepository
import com.daeumgil.app.domain.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesSettingsRepository(private val dataStore: DataStore<Preferences>) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(themeMode = ThemeMode.fromStorage(preferences[THEME_MODE]))
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = themeMode.storageValue }
    }

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
