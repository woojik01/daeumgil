package com.daeumgil.app.data.transit

import com.daeumgil.app.domain.transit.TransitDirection
import com.daeumgil.app.domain.transit.TransitLine
import com.daeumgil.app.domain.transit.TransitRegion
import com.daeumgil.app.domain.transit.TransitSelectionProvider
import com.daeumgil.app.domain.transit.TransitStop
import com.daeumgil.app.domain.transit.TransportType
import com.daeumgil.app.domain.transit.VehicleServiceType

/** Entirely fictional fixtures. No live transit names, coordinates, or travel-time claims. */
class SampleTransitSelectionProvider : TransitSelectionProvider {
    override val id = "sample-v1"
    override val isSample = true
    override val regions = listOf(TransitRegion("sample-east", "연습 동쪽 지역"), TransitRegion("sample-west", "연습 서쪽 지역"))
    override val places = listOf(
        TransitStop("sample-start", "연습 출발역"), TransitStop("sample-middle", "연습 중간역"),
        TransitStop("sample-transfer", "연습 환승역"), TransitStop("sample-end", "연습 종점역"),
        TransitStop("sample-park", "연습 공원 정류장"),
    )

    private fun directions(prefix: String, stops: List<TransitStop>, express: List<TransitStop>? = null): List<TransitDirection> {
        val services = buildMap {
            put(VehicleServiceType.STANDARD, stops)
            express?.let { put(VehicleServiceType.EXPRESS, it) }
        }
        return listOf(
            TransitDirection("$prefix-out", "${stops.last().name} 방향", services),
            TransitDirection("$prefix-in", "${stops.first().name} 방향", services.mapValues { (_, value) -> value.reversed() }),
        )
    }

    override val lines = listOf(
        TransitLine("sample-subway-a", "연습 A선", "출발역 ↔ 종점역", TransportType.SUBWAY, regions[0].id,
            directions("subway-a", places.take(4), listOf(places[0], places[2], places[3]))),
        TransitLine("sample-subway-b", "연습 B선", "환승역 ↔ 공원 정류장", TransportType.SUBWAY, regions[0].id,
            directions("subway-b", listOf(places[2], places[4]))),
        TransitLine("sample-bus-east-101-a", "101", "연습 동쪽 · 출발역 ↔ 공원", TransportType.BUS, regions[0].id,
            directions("bus-east-a", listOf(places[0], places[1], places[4]))),
        TransitLine("sample-bus-east-101-b", "101", "연습 동쪽 · 환승역 ↔ 종점역", TransportType.BUS, regions[0].id,
            directions("bus-east-b", listOf(places[2], places[3]))),
        TransitLine("sample-bus-west-101", "101", "연습 서쪽 · 종점역 ↔ 공원", TransportType.BUS, regions[1].id,
            directions("bus-west", listOf(places[3], places[4]))),
    )
}
