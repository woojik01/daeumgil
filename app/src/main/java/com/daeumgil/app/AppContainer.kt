package com.daeumgil.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.daeumgil.app.data.settings.PreferencesSettingsRepository
import com.daeumgil.app.domain.settings.SettingsRepository
import com.daeumgil.app.data.transit.PreferencesCurrentTransitRepository
import com.daeumgil.app.data.transit.SampleTransitSelectionProvider
import com.daeumgil.app.domain.transit.CurrentTransitRepository
import com.daeumgil.app.domain.transit.TransitSelectionProvider

private val Context.settingsDataStore by preferencesDataStore(name = "settings")
private val Context.currentTransitDataStore by preferencesDataStore(name = "current_transit")

class AppContainer(context: Context) {
    val settingsRepository: SettingsRepository =
        PreferencesSettingsRepository(context.applicationContext.settingsDataStore)
    val currentTransitRepository: CurrentTransitRepository =
        PreferencesCurrentTransitRepository(context.applicationContext.currentTransitDataStore)
    val transitSelectionProvider: TransitSelectionProvider = SampleTransitSelectionProvider()
}
