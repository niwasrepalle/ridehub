# Required device validation

Not executed in this environment. Install the debug APK on Android 9+ after allowing installs from the file manager.

Record each APK/device run using [DEVICE-TEST-RESULTS.md](DEVICE-TEST-RESULTS.md), including actual observations and evidence. Its initial NOT RUN entries do not establish device validation.

1. Deny location: demo comparison works and GPS must remain off. Grant approximate only: tracking must not require precise access. Revoke permissions mid-trip: no crash and no new location access.
2. Start tracking with the app visible; notification appears and Stop clears GPS. Check notification denial on Android 13+, GPS disabled, indoor no-fix, screen-off and power-saving interruptions. Confirm stale location is omitted from sharing after two minutes.
3. Search all providers, cancel one, change another ride type, retry. Other searches remain independent. Disable provider filter; Compare excludes it.
4. Share from trip and RideSafe: inspect preview, verify demo labels, no invented live link, no coordinates unless actual fresh GPS. Cancel sharing; no recipient should receive anything.
5. Save/delete contacts, restart app. Contacts persist locally; GPS and Rapido link must not persist. No automatic contact notification.
6. Test SOS cancellation and moving app into background during countdown. To test final dialer transition, stop before placing a call unless there is an actual emergency. Never make test emergency calls.
7. Open an official share link you own. Malformed, HTTP, foreign-host and credential-bearing URLs must be rejected. Verify no RideHub scraping or network traffic.
8. Rotate/recreate activity and test process death: current in-memory demo/search state resets; tracking is non-sticky, so Android may stop it. This prototype does not promise uninterrupted safety monitoring.
