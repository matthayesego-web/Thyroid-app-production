# Changelog

## v0.2.0 - development

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

### Changed
- Trends screen expanded to Trends & Labs
- Doctor report now remains useful even when the user has labs or medication milestones but few symptom entries
- Migrated the Android build to AGP 9 built-in Kotlin support

### Safety / privacy
- Lab values are stored and displayed as entered; no automatic interpretation is performed
- PDF files are generated in app cache and are not uploaded by the app
- Android backup exclusion remains in place for journal data

## v0.1.0

Initial local prototype with onboarding, daily symptom tracking, medication adherence, history, basic trends and text doctor summaries.
