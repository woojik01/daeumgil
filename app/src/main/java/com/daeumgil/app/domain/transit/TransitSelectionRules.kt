package com.daeumgil.app.domain.transit

enum class TransitInputIssue {
    TRANSPORT_REQUIRED, PROVIDER_MISMATCH, REGION_REQUIRED, REGION_MISMATCH,
    ROUTE_REQUIRED, ROUTE_MISMATCH, DIRECTION_REQUIRED, DIRECTION_MISMATCH,
    SERVICE_REQUIRED, SERVICE_MISMATCH, STOP_REQUIRED, STOP_MISMATCH,
    BOARDING_CONFIRMATION_REQUIRED, UNEXPECTED_TRANSIT_FIELDS,
}

class TransitSelectionRules(private val provider: TransitSelectionProvider) {
    fun selectTransport(type: TransportType) = CurrentTransitContext(transportType = type, providerId = provider.id)

    fun selectRegion(context: CurrentTransitContext, id: String): CurrentTransitContext {
        if (context.transportType != TransportType.BUS || provider.regions.none { it.id == id }) return context
        if (id == context.regionId) return context
        return selectTransport(TransportType.BUS).copy(regionId = id)
    }

    fun selectRoute(context: CurrentTransitContext, id: String): CurrentTransitContext {
        val line = provider.line(id) ?: return context
        if (line.type != context.transportType || (line.type == TransportType.BUS && line.regionId != context.regionId)) return context
        if (line.id == context.routeId) return context
        return selectTransport(line.type).copy(regionId = line.regionId, routeId = line.id, serviceType = line.services.firstOrNull())
    }

    fun selectDirection(context: CurrentTransitContext, id: String?): CurrentTransitContext {
        val line = provider.line(context.routeId) ?: return context
        if (id != null && line.directions.none { it.id == id }) return context
        if (context.directionId == id && context.directionUnknown == (id == null)) return context
        val services = line.directions.firstOrNull { it.id == id }?.stopsByService?.keys?.toList() ?: line.services
        return context.copy(directionId = id, directionUnknown = id == null,
            serviceType = context.serviceType.takeIf { it in services } ?: services.firstOrNull(), stopId = null, stopSequence = null,
            onBoardConfirmed = false, confirmationTime = 0)
    }

    fun selectService(context: CurrentTransitContext, type: VehicleServiceType): CurrentTransitContext {
        val line = provider.line(context.routeId) ?: return context
        if (type !in line.services || type == context.serviceType) return context
        return context.copy(serviceType = type, stopId = null, stopSequence = null,
            onBoardConfirmed = false, confirmationTime = 0)
    }

    fun selectStop(context: CurrentTransitContext, choice: StopChoice): CurrentTransitContext {
        if (choice !in provider.stopChoices(context)) return context
        return context.copy(stopId = choice.stop.id, stopSequence = choice.sequence,
            onBoardConfirmed = false, confirmationTime = 0)
    }

    fun validate(context: CurrentTransitContext): List<TransitInputIssue> = buildList {
        val type = context.transportType
        if (type == null) { add(TransitInputIssue.TRANSPORT_REQUIRED); return@buildList }
        if (context.providerId != provider.id) add(TransitInputIssue.PROVIDER_MISMATCH)
        if (type.isTransit) {
            if (type == TransportType.BUS && context.regionId == null) add(TransitInputIssue.REGION_REQUIRED)
            else if (type == TransportType.BUS && provider.regions.none { it.id == context.regionId }) add(TransitInputIssue.REGION_MISMATCH)
            val line = provider.line(context.routeId)
            if (context.routeId == null) add(TransitInputIssue.ROUTE_REQUIRED)
            else if (line == null || line.type != type) add(TransitInputIssue.ROUTE_MISMATCH)
            if (line != null && line.regionId != context.regionId) add(TransitInputIssue.REGION_MISMATCH)
            if (context.directionId == null && !context.directionUnknown) add(TransitInputIssue.DIRECTION_REQUIRED)
            else if ((context.directionUnknown && context.directionId != null) ||
                (!context.directionUnknown && line?.directions?.none { it.id == context.directionId } == true)) {
                add(TransitInputIssue.DIRECTION_MISMATCH)
            }
            if (context.serviceType == null) add(TransitInputIssue.SERVICE_REQUIRED)
            else if (line != null && (context.serviceType !in line.services ||
                (!context.directionUnknown && context.directionId != null &&
                    line.directions.firstOrNull { it.id == context.directionId }?.stopsByService?.containsKey(context.serviceType) == false))) {
                add(TransitInputIssue.SERVICE_MISMATCH)
            }
            if (!context.onBoardConfirmed) add(TransitInputIssue.BOARDING_CONFIRMATION_REQUIRED)
        } else if (context.regionId != null || context.routeId != null || context.directionId != null ||
            context.directionUnknown || context.serviceType != null || context.stopSequence != null ||
            context.onBoardConfirmed || context.stopAnchor != StopAnchor.CURRENT) {
            add(TransitInputIssue.UNEXPECTED_TRANSIT_FIELDS)
        }
        if (context.stopId == null) add(TransitInputIssue.STOP_REQUIRED)
        else if (provider.stopChoices(context).none { it.stop.id == context.stopId && it.sequence == context.stopSequence }) {
            add(TransitInputIssue.STOP_MISMATCH)
        }
    }

    fun canUseForRouting(context: CurrentTransitContext): Boolean =
        !provider.isSample && context.confirmationTime > 0 && !context.directionUnknown && validate(context).isEmpty()
}
