package com.daeumgil.app.domain.transit

enum class TransportType { NOT_BOARDED, SUBWAY, BUS, WALKING, OTHER;
    val isTransit: Boolean get() = this == SUBWAY || this == BUS
}
enum class VehicleServiceType { STANDARD, EXPRESS }
enum class StopAnchor { CURRENT, LAST_PASSED }

data class CurrentTransitContext(
    val transportType: TransportType? = null,
    val providerId: String? = null,
    val regionId: String? = null,
    val routeId: String? = null,
    val directionId: String? = null,
    val directionUnknown: Boolean = false,
    val serviceType: VehicleServiceType? = null,
    val stopId: String? = null,
    val stopSequence: Int? = null,
    val stopAnchor: StopAnchor = StopAnchor.CURRENT,
    val onBoardConfirmed: Boolean = false,
    val confirmationTime: Long = 0,
)

data class TransitRegion(val id: String, val name: String)
data class TransitStop(val id: String, val name: String)
data class StopChoice(val stop: TransitStop, val sequence: Int?)
data class TransitDirection(
    val id: String,
    val label: String,
    val stopsByService: Map<VehicleServiceType, List<TransitStop>>,
)
data class TransitLine(
    val id: String,
    val name: String,
    val detail: String,
    val type: TransportType,
    val regionId: String,
    val directions: List<TransitDirection>,
) {
    val services: List<VehicleServiceType> get() = directions.flatMap { it.stopsByService.keys }.distinct()
}

/** A normalized local snapshot. A future API adapter can supply another implementation. */
interface TransitSelectionProvider {
    val id: String
    val isSample: Boolean
    val regions: List<TransitRegion>
    val lines: List<TransitLine>
    val places: List<TransitStop>
}

fun TransitSelectionProvider.line(id: String?): TransitLine? = lines.firstOrNull { it.id == id }

fun TransitSelectionProvider.stopChoices(context: CurrentTransitContext): List<StopChoice> {
    if (context.transportType?.isTransit != true) return places.map { StopChoice(it, null) }
    val line = line(context.routeId) ?: return emptyList()
    val service = context.serviceType ?: return emptyList()
    if (context.directionUnknown) {
        return line.directions.flatMap { it.stopsByService[service].orEmpty() }
            .distinctBy { it.id }.map { StopChoice(it, null) }
    }
    val direction = line.directions.firstOrNull { it.id == context.directionId } ?: return emptyList()
    return direction.stopsByService[service].orEmpty().mapIndexed { index, stop -> StopChoice(stop, index + 1) }
}
