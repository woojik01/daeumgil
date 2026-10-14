package com.daeumgil.app.feature.currenttransit

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.daeumgil.app.R
import com.daeumgil.app.domain.transit.CurrentTransitContext
import com.daeumgil.app.domain.transit.StopAnchor
import com.daeumgil.app.domain.transit.TransitInputIssue
import com.daeumgil.app.domain.transit.TransitSelectionProvider
import com.daeumgil.app.domain.transit.TransportType
import com.daeumgil.app.domain.transit.VehicleServiceType
import com.daeumgil.app.domain.transit.line

fun TransportType.labelRes(): Int = when (this) {
    TransportType.NOT_BOARDED -> R.string.transport_not_boarded
    TransportType.SUBWAY -> R.string.transport_subway
    TransportType.BUS -> R.string.transport_bus
    TransportType.WALKING -> R.string.transport_walking
    TransportType.OTHER -> R.string.transport_other
}
fun VehicleServiceType.labelRes(): Int = if (this == VehicleServiceType.EXPRESS) R.string.service_express else R.string.service_standard
fun StopAnchor.labelRes(): Int = if (this == StopAnchor.CURRENT) R.string.anchor_current else R.string.anchor_last_passed

fun TransitInputIssue.labelRes(): Int = when (this) {
    TransitInputIssue.TRANSPORT_REQUIRED -> R.string.error_transport
    TransitInputIssue.PROVIDER_MISMATCH -> R.string.error_source
    TransitInputIssue.REGION_REQUIRED -> R.string.error_region
    TransitInputIssue.REGION_MISMATCH -> R.string.error_region_mismatch
    TransitInputIssue.ROUTE_REQUIRED -> R.string.error_route
    TransitInputIssue.ROUTE_MISMATCH -> R.string.error_route_mismatch
    TransitInputIssue.DIRECTION_REQUIRED -> R.string.error_direction
    TransitInputIssue.DIRECTION_MISMATCH -> R.string.error_direction_mismatch
    TransitInputIssue.SERVICE_REQUIRED -> R.string.error_service
    TransitInputIssue.SERVICE_MISMATCH -> R.string.error_service_mismatch
    TransitInputIssue.STOP_REQUIRED -> R.string.error_stop
    TransitInputIssue.STOP_MISMATCH -> R.string.error_stop_mismatch
    TransitInputIssue.BOARDING_CONFIRMATION_REQUIRED -> R.string.error_boarding
    TransitInputIssue.UNEXPECTED_TRANSIT_FIELDS -> R.string.error_unexpected_fields
}

@Composable
fun transitSummary(context: CurrentTransitContext?, provider: TransitSelectionProvider): String {
    if (context?.transportType == null) return stringResource(R.string.current_not_selected)
    val line = provider.line(context.routeId)
    val parts = mutableListOf(stringResource(context.transportType.labelRes()))
    line?.let { parts += if (it.type == TransportType.BUS) "${it.name} · ${it.detail}" else it.name }
    context.serviceType?.let { parts += stringResource(it.labelRes()) }
    if (context.directionUnknown) parts += stringResource(R.string.direction_unknown)
    else line?.directions?.firstOrNull { it.id == context.directionId }?.let { parts += it.label }
    provider.places.firstOrNull { it.id == context.stopId }?.let {
        parts += if (context.transportType.isTransit) "${stringResource(context.stopAnchor.labelRes())}: ${it.name}" else it.name
    }
    return parts.joinToString("\n")
}
