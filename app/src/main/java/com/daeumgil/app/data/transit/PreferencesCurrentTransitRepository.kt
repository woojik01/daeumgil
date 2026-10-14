package com.daeumgil.app.data.transit

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.daeumgil.app.domain.transit.CurrentTransitCodec
import com.daeumgil.app.domain.transit.CurrentTransitContext
import com.daeumgil.app.domain.transit.CurrentTransitRepository
import java.io.IOException
import kotlinx.coroutines.flow.map

class PreferencesCurrentTransitRepository(private val store: DataStore<Preferences>) : CurrentTransitRepository {
    override val currentTransit = store.data.map { preferences ->
        preferences[CURRENT_TRANSIT]?.let { encoded ->
            try {
                CurrentTransitCodec.decode(encoded).also {
                    require(it.transportType != null && it.confirmationTime > 0 && !it.providerId.isNullOrBlank())
                }
            } catch (exception: IllegalArgumentException) {
                throw IOException("Unreadable transit selection", exception)
            }
        }
    }

    override suspend fun save(context: CurrentTransitContext) {
        require(context.transportType != null && context.confirmationTime > 0 && !context.providerId.isNullOrBlank())
        store.edit { it[CURRENT_TRANSIT] = CurrentTransitCodec.encode(context) }
    }

    override suspend fun clear() { store.edit { it.remove(CURRENT_TRANSIT) } }

    companion object { private val CURRENT_TRANSIT = stringPreferencesKey("current_transit_v1") }
}
