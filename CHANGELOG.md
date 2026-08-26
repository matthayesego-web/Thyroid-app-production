# Changelog

## v0.3.2 - work in progress

### Changed
- Today now asks `Are you feeling symptoms today?` before showing the symptom list
- Choosing No keeps the symptom list collapsed and records symptom scores as zero for that check-in
- Choosing Yes expands the condition-specific symptom list with the existing short descriptions and severity controls
- Daily check-in forms now start fresh instead of preloading today's previously saved answers
- Saving or replacing a check-in clears the form back to its default state
- When a check-in already exists for today, the Today screen clearly warns that submitting the fresh form will replace it
- Version bumped to 0.3.2 / versionCode 5

### Build status
- Intentionally not compiled or packaged yet; this work remains on `work/v0.3.2-symptom-flow` until permanent Android signing is completed

## v0.3.1 - tested patch

### Fixed
- Onboarding no longer silently traps the user on the first screen
- Hypothyroidism / Hyperthyroidism is the only required onboarding choice
- Medication details remain optional
- Missing condition selection now produces a clear validation message

### Changed
- Current dose start date is explicitly optional
- Dose start date now uses a native Android calendar picker and can be cleared when the user does not remember the date
- Medication dose-change date uses the same calendar picker
- Version bumped to 0.3.1 / versionCode 4

### Verification
- GitHub Actions debug build #85 compiled successfully
- v0.3.1 device testing confirmed the onboarding/date-picker patch works as intended

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
