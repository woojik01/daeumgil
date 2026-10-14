package com.daeumgil.app.domain.transit

import kotlinx.coroutines.flow.Flow

interface CurrentTransitRepository {
    val currentTransit: Flow<CurrentTransitContext?>
    suspend fun save(context: CurrentTransitContext)
    suspend fun clear()
}
