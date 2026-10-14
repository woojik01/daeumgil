package com.daeumgil.app.domain.transit

import com.daeumgil.app.data.transit.SampleTransitSelectionProvider
import org.junit.Assert.*
import org.junit.Test

class TransitSelectionRulesTest {
    private val provider = SampleTransitSelectionProvider()
    private val rules = TransitSelectionRules(provider)

    private fun subway(): CurrentTransitContext {
        var context = rules.selectRoute(rules.selectTransport(TransportType.SUBWAY), "sample-subway-a")
        context = rules.selectDirection(context, "subway-a-out")
        context = rules.selectStop(context, provider.stopChoices(context)[1])
        return context.copy(onBoardConfirmed = true, confirmationTime = 1234)
    }

    @Test fun missingTypeAndTransitFieldsCannotBeSaved() {
        assertEquals(listOf(TransitInputIssue.TRANSPORT_REQUIRED), rules.validate(CurrentTransitContext()))
        val issues = rules.validate(rules.selectTransport(TransportType.BUS))
        assertTrue(issues.containsAll(listOf(TransitInputIssue.REGION_REQUIRED, TransitInputIssue.ROUTE_REQUIRED,
            TransitInputIssue.DIRECTION_REQUIRED, TransitInputIssue.STOP_REQUIRED, TransitInputIssue.BOARDING_CONFIRMATION_REQUIRED)))
    }

    @Test fun validSampleSelectionIsNeverEligibleForRealRouting() {
        assertTrue(rules.validate(subway()).isEmpty())
        assertFalse(rules.canUseForRouting(subway()))
    }

    @Test fun changingDirectionClearsOldStopAndOnboardConfirmation() {
        val changed = rules.selectDirection(subway(), "subway-a-in")
        assertNull(changed.stopId)
        assertNull(changed.stopSequence)
        assertFalse(changed.onBoardConfirmed)
        assertEquals(0L, changed.confirmationTime)
        assertEquals("sample-end", provider.stopChoices(changed).first().stop.id)
    }

    @Test fun changingRouteClearsDependentDirectionAndPosition() {
        val changed = rules.selectRoute(subway(), "sample-subway-b")
        assertEquals(TransportType.SUBWAY, changed.transportType)
        assertNull(changed.directionId)
        assertFalse(changed.directionUnknown)
        assertNull(changed.stopId)
        assertFalse(changed.onBoardConfirmed)
    }

    @Test fun expressSelectionRemovesOrdinaryOnlyStopAndRejectsForgedPosition() {
        val changed = rules.selectService(subway(), VehicleServiceType.EXPRESS)
        assertNull(changed.stopId)
        assertFalse(changed.onBoardConfirmed)
        assertFalse(provider.stopChoices(changed).any { it.stop.id == "sample-middle" })
        val forged = changed.copy(stopId = "sample-middle", stopSequence = 2, onBoardConfirmed = true)
        assertTrue(TransitInputIssue.STOP_MISMATCH in rules.validate(forged))
    }

    @Test fun unknownDirectionCanBeRecordedButCannotEnableRoutingEvenWithNonSampleProvider() {
        var context = rules.selectDirection(subway(), null)
        context = rules.selectStop(context, provider.stopChoices(context).first()).copy(onBoardConfirmed = true, confirmationTime = 1234)
        assertTrue(rules.validate(context).isEmpty())
        assertNull(context.stopSequence)
        val nonSample = object : TransitSelectionProvider by provider { override val isSample = false }
        assertFalse(TransitSelectionRules(nonSample).canUseForRouting(context))
    }

    @Test fun wrongDirectionAndStopSequenceAreRejected() {
        assertTrue(TransitInputIssue.DIRECTION_MISMATCH in rules.validate(subway().copy(directionId = "other-line-direction")))
        assertTrue(TransitInputIssue.STOP_MISMATCH in rules.validate(subway().copy(stopSequence = 999)))
    }

    @Test fun sameBusNumberUsesUniqueIdsAndRegionChangeClearsRoute() {
        val lines = provider.lines.filter { it.type == TransportType.BUS && it.name == "101" }
        assertEquals(3, lines.map { it.id }.distinct().size)
        var context = rules.selectRegion(rules.selectTransport(TransportType.BUS), "sample-east")
        context = rules.selectRoute(context, "sample-bus-east-101-a")
        val changed = rules.selectRegion(context, "sample-west")
        assertNull(changed.routeId)
        assertEquals("sample-west", changed.regionId)
        assertEquals(changed, rules.selectRoute(changed, "sample-bus-east-101-a"))
    }

    @Test fun nonTransitSelectionRejectsResidualTransitFields() {
        val walking = rules.selectTransport(TransportType.WALKING)
        val positioned = rules.selectStop(walking, provider.stopChoices(walking).first())
        assertTrue(rules.validate(positioned).isEmpty())
        assertTrue(TransitInputIssue.UNEXPECTED_TRANSIT_FIELDS in rules.validate(positioned.copy(routeId = "sample-subway-a")))
    }

    @Test fun invalidChoiceDoesNotMutateValidSelection() {
        val context = subway()
        assertEquals(context, rules.selectDirection(context, "missing-direction"))
        assertEquals(context, rules.selectStop(context, StopChoice(TransitStop("missing", "Unknown"), 1)))
        assertEquals(context, rules.selectRoute(context, "missing-route"))
    }
}
