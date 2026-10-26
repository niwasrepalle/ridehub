package com.ridehub.app

private const val MAX_LOCATION_AGE_NANOS = 120_000_000_000L

// Both timestamps must use Android elapsed realtime from the same boot.
fun isLocationFresh(fixElapsedNanos: Long, nowElapsedNanos: Long): Boolean {
    if (fixElapsedNanos < 0 || nowElapsedNanos < fixElapsedNanos) return false
    return nowElapsedNanos - fixElapsedNanos < MAX_LOCATION_AGE_NANOS
}
