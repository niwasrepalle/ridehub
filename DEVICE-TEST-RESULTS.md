# Device test record

Copy this template for each APK/device combination. The checks below are **NOT RUN**; this file is a recording template, not evidence of a passing device test. Follow [DEVICE-CHECKS.md](DEVICE-CHECKS.md) for the full procedure.

## Test environment

- Tester and date/time with timezone:
- Source commit and APK SHA-256:
- APK source / Actions run URL:
- Device model and Android version:
- Location permission (denied / approximate / precise):
- Notification permission and battery-saving settings:
- Evidence folder (keep personal coordinates and contacts out of public uploads):

## Results

Use PASS, FAIL, BLOCKED or NOT RUN. Record what actually happened, including interruptions; a build passing is not a device result.

| ID | Procedure | Expected observation | Result | Actual observation / evidence |
| --- | --- | --- | --- | --- |
| GPS-01 | Deny location, then compare demo rides | Comparison works; local tracking stays off | NOT RUN | |
| GPS-02 | Grant approximate only and start tracking while app is visible | Tracking can start without precise permission; notification Stop ends it | NOT RUN | |
| GPS-03 | Revoke location while tracking | No crash or continued location access | NOT RUN | |
| GPS-04 | Disable location providers; then try screen-off and battery saver separately | Record unavailable/interrupted tracking honestly; no uninterrupted-monitoring claim | NOT RUN | |
| SHARE-01 | Open trip and RideSafe share sheets with no GPS fix | Simulated ride label; no invented live link or coordinates | NOT RUN | |
| SHARE-02 | Open share sheet with a fresh fix, then after more than two minutes without a new fix | Fresh snapshot is labelled one-time, not live; stale coordinates omitted | NOT RUN | |
| SHARE-03 | Cancel the share sheet | No message sent to a recipient | NOT RUN | |
| CONTACT-01 | Save/delete a synthetic contact and restart | Local changes persist; no automatic notification | NOT RUN | |
| SOS-01 | Start countdown, then cancel before zero | Countdown closes without opening the dialer | NOT RUN | |
| SOS-02 | Allow countdown to finish, then exit the dialer without calling | Dialer opens for 112; no automatic call or contact alert | NOT RUN | |
| LINK-01 | Try an owned official link and malformed/HTTP/foreign-host links | Only the supported HTTPS host/path is accepted; browser opens externally | NOT RUN | |
| STATE-01 | Rotate/recreate, then separately kill and reopen the app | Record resets; GPS snapshot and pasted link must not survive process death | NOT RUN | |

For SOS-02, never place a test emergency call. Use synthetic contacts and cancel share sheets unless a consenting test recipient is deliberately selected.

## Failure record

- Check ID:
- Reproduction steps and starting permissions:
- Expected versus observed behavior:
- Reproducibility (attempts / failures):
- Redacted screenshot or log reference:
- Issue link / retest commit:

## Completion statement

- Checks passed / failed / blocked / not run:
- Remaining device limitations:
- Retest needed after fixes:

Do not replace the existing device-testing-pending statement until completed records provide evidence for the specific claims being changed.
