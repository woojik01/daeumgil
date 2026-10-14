package com.daeumgil.app.domain.transit

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentTransitCodecTest {
    @Test fun allFieldsAndEscapedUnicodeIdentifiersRoundTrip() {
        val context = CurrentTransitContext(TransportType.SUBWAY, "provider|한글~", "region", "route|~", "direction",
            false, VehicleServiceType.EXPRESS, "stop|한글", 4, StopAnchor.LAST_PASSED, true, 9876)
        assertEquals(context, CurrentTransitCodec.decode(CurrentTransitCodec.encode(context)))
        assertEquals(CurrentTransitContext(), CurrentTransitCodec.decode(CurrentTransitCodec.encode(CurrentTransitContext())))
    }

    @Test(expected = IllegalArgumentException::class) fun malformedRecordsAreNotSilentlyInterpreted() {
        CurrentTransitCodec.decode("unreadable-record!")
    }
}
