# Changelog

## v0.3.5 - backup, navigation, and durability

### Added
- Dedicated `Settings` tab for personalization, accessibility, optional features, privacy, and data tools
- New `Insights` tab that groups Trends & Labs with Doctor Report instead of using a separate report tab
- Password-protected encrypted backup files using PBKDF2-HMAC-SHA256 key derivation and AES-256-GCM authenticated encryption
- Versioned backup payload covering profile, reminders, optional-feature preferences, daily journal entries, medication changes, and lab history
- Encrypted backup restore flow with file validation, password authentication, content summary, and explicit confirmation
- Restore uses merge-safe behavior: matching dates/record IDs are updated and unrelated local records are retained
- Data persistence policy and release checklist in `docs/DATA_PERSISTENCE.md`

### Changed
- Bottom navigation is now `Today · Journal · Medication · Insights · Settings`
- Medication is focused on medication profile, reminders, clinician-directed dose changes, and dose history only
- Personalization, accessibility, optional modules, and privacy information have moved out of Medication into Settings
- Shared screen headers received a restrained branded visual refresh
- Version bumped to 0.3.5 / versionCode 8

### Data safety
- Existing DataStore name and storage keys remain unchanged from v0.3.4 so normal Play updates preserve local journal data
- No update path clears or recreates local storage
- Backup passwords are not stored by Thyroid Echo
- Backup files are encrypted before being written outside app storage
- Android cloud-backup exclusions remain in place; encrypted export is the user-controlled portability mechanism

## v0.3.4 - internal test candidate

### Added
- Optional `Expanded thyroid labs` module for TPOAb, TgAb, and TRAb values and reference ranges
- Expanded thyroid lab values are stored locally and included in lab history and doctor reports when entered
- In-app `Privacy & data` section describing local storage, report sharing, reminder behavior, and data deletion
- Google Play internal-test checklist, store listing draft, privacy-policy draft, declaration notes, and release notes in `docs/`
- Host-ready `docs/privacy-policy.html` page for later public privacy-policy hosting

### Changed
- Lab date entry now uses the same native calendar picker as medication dates instead of manual `YYYY-MM-DD` typing
- History now shows the strongest reported symptoms and severities instead of only a generic symptom average
- History shows weight only on entries where a weight value was actually recorded
- Optional feature settings now include expanded thyroid labs while remaining off by default
- Version bumped to 0.3.4 / versionCode 7

### Internal test posture
- Core app remains local-first with no account, ads, analytics, or remote journal database
- Current Android manifest does not request Internet permission
- Encrypted device transfer remains intentionally deferred until its restore path has dedicated automated coverage and a real device-to-device validation
- Candidate is routed through the protected permanently signed release workflow before Play upload

## v0.3.3 - optional feature foundation

### Added
- New local-only optional feature settings model and persistence layer
- `Optional features` card in the Medication screen
- Toggle for quick context tags in the daily check-in
- Toggle for daily weight tracking
- Optional feature switches are stored locally and are covered by the existing Android backup exclusions

### Changed
- Daily `Anything else?` section now only shows context tags and weight when those features are enabled
- Notes remain available regardless of optional feature choices
- Turning an optional feature off only hides its input; previously saved journal data is not deleted
- Weight tracking defaults off to keep the daily form lighter for users who do not need it
- Quick context tags remain enabled by default because they are compact and were already part of v0.3.2
- Version bumped to 0.3.3 / versionCode 6

### Direction
- This settings structure is the foundation for future opt-in additions such as expanded thyroid labs, extra measurements, custom symptoms, and additional thyroid-specific tracking without cluttering the default experience
- Support / donation UI remains deferred until a real payment destination is selected; no medical or tracking feature will be paywalled

## v0.3.2 - device test candidate

### Added
- Optional first name during onboarding for local personalized greetings
- Time-aware Today greeting: Good morning, Good afternoon, Good evening, or Good night
- Compact `Today at a glance` card showing check-in status, medication-log status, and reminder time when enabled
- Optional quick context tags: Poor sleep, Stress, Sick / illness, Exercise, Travel, and Diet change
- Context tags are retained with individual check-ins and surfaced in History and doctor summaries
- Optional `Larger text & controls` accessibility preference
- Personalization and display preferences can be changed later from the Medication screen

### Changed
- Today now asks `Are you feeling symptoms today?` before showing the symptom list
- A Yes or No symptom response is required before saving the daily check-in
- Choosing No keeps the symptom list collapsed and records symptom scores as zero for that check-in
- Choosing Yes expands the condition-specific symptom list with short descriptions and severity controls
- Daily check-in forms now start fresh instead of preloading today's previously saved answers
- Saving or replacing a check-in clears the form back to its default state
- When a check-in already exists for today, the Today screen clearly warns that submitting the fresh form will replace it
- History distinguishes days with symptoms from days where no symptoms were reported
- Doctor summaries include the optional local first name, days with symptoms, and common context-tag counts
- Version bumped to 0.3.2 / versionCode 5

### Privacy
- First name, accessibility preference, context tags, and all check-in data remain local device data
- No account, analytics identity, remote profile, or cloud journal storage is introduced
- Existing Android cloud-backup exclusions remain in place

### Build / signing
- Permanent Android signing secrets are configured in GitHub Actions
- Same-repository app PRs automatically request the protected signed-release workflow
- v0.3.2 is intended to be the first hands-off permanently signed device-test candidate

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
- Automatic reminder restoration after reboot, time changes, timezone changes, app replacement, or exact-alarm permission change
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
