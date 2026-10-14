package com.daeumgil.app.domain.transit

import java.util.Base64

/** Versioned, escaped primitive representation shared by disk and saved-instance state. */
object CurrentTransitCodec {
    fun encode(context: CurrentTransitContext): String {
        val fields = listOf("1", context.transportType?.name, context.providerId, context.regionId,
            context.routeId, context.directionId, context.directionUnknown.toString(), context.serviceType?.name,
            context.stopId, context.stopSequence?.toString(), context.stopAnchor.name,
            context.onBoardConfirmed.toString(), context.confirmationTime.toString())
        return fields.joinToString("|") { value ->
            value?.let { Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray(Charsets.UTF_8)) } ?: "~"
        }
    }

    fun decode(value: String): CurrentTransitContext {
        val fields = value.split('|').map { if (it == "~") null else String(Base64.getUrlDecoder().decode(it), Charsets.UTF_8) }
        require(fields.size == 13 && fields[0] == "1") { "Unsupported transit record" }
        val result = CurrentTransitContext(
            transportType = fields[1]?.let { TransportType.valueOf(it) }, providerId = fields[2], regionId = fields[3],
            routeId = fields[4], directionId = fields[5], directionUnknown = requireNotNull(fields[6]).toBooleanStrict(),
            serviceType = fields[7]?.let { VehicleServiceType.valueOf(it) }, stopId = fields[8], stopSequence = fields[9]?.toInt(),
            stopAnchor = StopAnchor.valueOf(requireNotNull(fields[10])),
            onBoardConfirmed = requireNotNull(fields[11]).toBooleanStrict(), confirmationTime = requireNotNull(fields[12]).toLong(),
        )
        require(result.confirmationTime >= 0 && (result.stopSequence == null || result.stopSequence > 0))
        return result
    }
}
