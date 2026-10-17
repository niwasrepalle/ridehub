# RideHub

An Android ride-comparison and personal ride-safety prototype by **Niwas Repalle**. Explore simulated provider offers and a user-controlled local GPS workflow in one app.

**Demo boundary:** fares, drivers and booking states are simulated. Local device GPS is real when permission is granted; no provider APIs or hosted tracking service are connected.

Native Kotlin / Jetpack Compose, Android 9+ (API 28), target 35. Dark monochrome UI, blue routes, red SOS. Splash is a procedural aerial street illustration with illuminated routes, no planets or characters. It is not a photographic map.

## Implemented
- Onboarding and password-free provider connection placeholders.
- Illustrated map, editable pickup/destination and local demo-place search.
- Provider filters, independent car/bike/auto/premium selection, parallel simulated searches, retry/cancel with generation guards, simulated fare and driver detail, demo trip start/end.
- Android share sheet from trip and RideSafe. Clearly labeled simulated ride details, optional fresh real GPS snapshot and user-pasted official Rapido link. Sharing is explicit; no automatic messaging.
- Manually entered local trusted contacts and delete action. No contacts permission.
- Real local location foreground service started by user while the activity is visible. Fine/coarse runtime request, approximate location support, persistent notification with Stop, no background-location permission, no boot restart. Location stays in process memory and clears on stop. GPS may be unavailable or interrupted.
- Five-second cancelable SOS countdown opens ACTION_DIAL tel:112. User must place the call. No CALL_PHONE permission or automatic dispatch.
- Strict Rapido HTTPS host/path validation. Link opens externally; no web scraping, token logging or embedded browser. Link retained only in memory.

## Not implemented / blockers
No provider login, real fares, real availability, booking, driver feed, cancellations or payments. All need provider-authorized APIs and commercial/technical permissions. Ride-type availability in the demo does not establish provider support.
No real map tiles, geocoding or routing: integrate a licensed SDK and its credentials later. No hosted live tracking link: `LiveTrackingGateway` specifies create/publish/revoke operations; there is deliberately no fabricated URL or network publisher. Trusted contacts do not receive automatic alerts.
Physical phone/emulator, permission-denial UX, background GPS longevity, external browser/share targets, and 112 dialer must be tested on a device. This is a prototype, not an emergency response service.

## Privacy and security
No INTERNET permission, analytics, passwords, API secrets, SMS or address-book access. Device-private preferences store contacts; Android backup is disabled. Share links are sensitive bearer links; share only with permission. Only fresh location (under two minutes) can be included in an explicit share action; coordinates are not shown in app logs. Stop tracking clears location. Uninstall deletes local app data.

## Rapido investigation (10 October 2026)
The user-supplied link could not be accessed by the research tool; no trip/location content was retrieved or persisted. The live token is not embedded in this project. Rapido's safety material documents sharing trip status in its app. Public terms/privacy do not establish authorization for third-party extraction or polling. Therefore the only integration here is validating a user-provided official URL and handing it to an external browser. Trip validity, expiry and browser behavior remain unverified.
Sources: https://www.rapido.bike/CustomerTerms , https://www.rapido.bike/Privacy , https://rapido.bike/RapidoSafetyGuidelines.pdf
Android location guidance: https://developer.android.com/develop/sensors-and-location/location/permissions and https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start

## Build
Use JDK 17 and Android SDK 35: `./gradlew testDebugUnitTest lintDebug assembleDebug`. Windows: `gradlew.bat`. GitHub Actions workflow included; upload this project as repository root, run workflow and download APK artifact. Local build used the already available portable toolchain. APK is a debug-signed personal preview.

## Live tracking backend contract for future implementation
Explicit sharing consent precedes session creation. Authenticated uploader receives a random >=128-bit bearer viewer token; server stores only a hash, expires session at trip end or at most one hour, supports immediate revoke, TLS only, rate limits, no indexing, no analytics, no cached location responses. Viewer displays last-update time, accuracy, stale/offline/ended states. Retain only latest point, delete at expiry/revoke. Never merge provider driver location with the phone GPS stream. App must display sharing status and a Stop sharing action and stop/revoke on trip end. These controls are design requirements, not deployed behavior.
