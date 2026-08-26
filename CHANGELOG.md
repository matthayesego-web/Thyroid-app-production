# Changelog

## v0.3.0 - development

### Added
- Daily thyroid-medication reminder at a user-selected time
- Optional follow-up reminder when no medication status has been logged
- Follow-up delay choices for 30 minutes, 1 hour, 90 minutes, or 2 hours
- Android notification-permission handling in the Medication screen
- In-context access to Android's precise alarm permission when available
- Automatic reminder restoration after reboot, time changes, timezone changes, app replacement, or exact-alarm permission changes
- Privacy-safe medication reminder notification channel

### Changed
- Reminder alarms now skip themselves when today's medication has already been logged as Taken, Late, or Missed
- Reminder notifications are cleared when the user saves today's medication/check-in
- Thyroid Echo now uses a consistent branded indigo/teal/lavender Material 3 palette instead of device dynamic colors
- Updated typography, shapes, card surfaces, onboarding, navigation icons, Today screen, History screen, and Medication screen for a calmer premium presentation
- Android debug artifact naming now reads the app version automatically
- Version bumped to 0.3.0 / versionCode 3

### Safety / privacy
- Reminder notifications deliberately avoid medication name/dose on the lock screen
- Follow-up wording says a medication log is missing; it does not claim the medication was not taken
- All reminder settings and medication-log checks remain local on the device
- Android cloud-backup exclusions remain unchanged

## v0.2.0 - tested milestone

### Added
- Dose-start date on the current medication profile
- Medication/dose change timeline records
- Thyroid lab history for TSH, Free T4 and optional T3
- Per-result laboratory reference-range fields
- Lab notes
- Lab results included in doctor summaries
- Medication milestones included in doctor summaries
- On-device PDF doctor-report generation
- PDF sharing through Android FileProvider
- 30 / 90 / all-check-in report scopes
- Explicit initial data-loading state to avoid onboarding flicker
- GitHub Actions debug-APK build and artifact upload
- Pull-request build verification before promotion to `main`
- Thyroid Echo adaptive launcher icon with stylized thyroid and pulse-line artwork

### Changed
- App working title and visible branding changed to Thyroid Echo
- Trends screen expanded to Trends & Labs
- Doctor report now remains useful even when the user has labs or medication milestones but few symptom entries
- Migrated the Android build to AGP 9 built-in Kotlin support
- APK workflow explicitly provisions stable Android SDK 36 before compiling

### Verification
- GitHub Actions run #43 compiled and uploaded the v0.2.0 debug APK successfully
- v0.2.0 installed and launched successfully on a real Android device

### Safety / privacy
- Lab values are stored and displayed as entered; no automatic interpretation is performed
- PDF files are generated in app cache and are not uploaded by the app
- Android backup exclusion remains in place for journal data

## v0.1.0

Initial local prototype with onboarding, daily symptom tracking, medication adherence, history, basic trends and text doctor summaries.
