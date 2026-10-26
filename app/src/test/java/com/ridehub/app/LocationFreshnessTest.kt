package com.ridehub.app

import org.junit.Assert.*
import org.junit.Test

class LocationFreshnessTest {
    @Test fun acceptsNewFix() {
        assertTrue(isLocationFresh(5_000_000_000L, 5_000_000_000L))
    }
    @Test fun acceptsJustUnderTwoMinutes() {
        assertTrue(isLocationFresh(1L, 120_000_000_000L))
    }
    @Test fun rejectsExactlyTwoMinutes() {
        assertFalse(isLocationFresh(1L, 120_000_000_001L))
    }
    @Test fun rejectsOlderFix() {
        assertFalse(isLocationFresh(1L, 180_000_000_001L))
    }
    @Test fun rejectsFutureFix() {
        assertFalse(isLocationFresh(6_000_000_000L, 5_000_000_000L))
    }
    @Test fun rejectsInvalidTimestampsWithoutOverflow() {
        assertFalse(isLocationFresh(-1L, Long.MAX_VALUE))
        assertFalse(isLocationFresh(Long.MIN_VALUE, Long.MAX_VALUE))
        assertFalse(isLocationFresh(0L, -1L))
        assertFalse(isLocationFresh(0L, Long.MAX_VALUE))
    }
    @Test fun acceptsValidBootZeroAndLargeUptime() {
        assertTrue(isLocationFresh(0L, 1L))
        assertTrue(isLocationFresh(Long.MAX_VALUE - 1L, Long.MAX_VALUE))
    }
}
