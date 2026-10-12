# Validation — 10 October 2026

- Kotlin compilation and debug APK assembly succeeded with Java 17, AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9 and SDK 35.
- Four JVM tests passed, zero failures/errors: official share-link acceptance, unsafe-link rejection, independent provider cancellation/type changes, and retry superseding prior generation.
- Final lint and APK rebuild succeeded after explicit backup exclusions and icon were added.
- Device/emulator execution and visual inspection were not performed. The city graphic is code-rendered illustration, not licensed map data. Read DEVICE-CHECKS.md before relying on device behavior.
- Hosted live tracking, real maps/geocoding, provider accounts/quotes/booking/driver feeds are not connected. See README.md for integration contracts and blockers.
- Initial sandbox builds could not connect to the Gradle daemon. An approved build outside the sandbox using cached dependencies succeeded. No uploaded location or provider credentials were involved.
