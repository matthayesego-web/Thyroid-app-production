# Thyroid Echo

Private Android-only thyroid symptom, medication and lab journal built with Kotlin + Jetpack Compose.

## Current development build

**v0.2.0** (`develop/v0.2.0`)

### Core features
- First-launch choice: Hypothyroidism or Hyperthyroidism
- Condition-specific daily symptom tracking
- Daily Taken / Late / Missed medication tracking
- Daily Overall, Energy, Mood and Sleep scores
- Optional weight and free-text notes
- Local on-device history using AndroidX DataStore
- Current medication and dose-start date
- Medication/dose change milestones
- TSH, Free T4 and optional T3 lab-result history
- Laboratory reference ranges stored exactly as entered
- Recent trend summaries
- Doctor summary with medication milestones and labs
- On-device PDF generation and Android share sheet
- Text summary sharing
- Adaptive Thyroid Echo launcher icon
- Cloud/transfer backup excluded for health-journal data
- Material 3 + dynamic light/dark colors

## Medical boundary

Thyroid Echo is a journal and trend summarizer. It does **not** diagnose thyroid disease, determine whether medication is medically effective, interpret lab values, or recommend dose changes.

Lab results are deliberately stored with the reference range shown by the user's own laboratory instead of applying a universal range.

## Privacy

The app is local-first. v0.2.0 requires:
- no account
- no remote database
- no analytics
- no ads
- no health-data upload

PDF reports are generated locally in the app cache and shared only when the user chooses Android's Share action.

## Build requirements

- Android SDK Platform 37
- JDK 17+
- Android Gradle Plugin 9.3.0
- Kotlin 2.3.21
- Compose BOM 2026.08.00
- Gradle 9.5 when generating/using a Gradle wrapper

The GitHub workflow explicitly provisions Android SDK 37 before compiling. A local Android Studio build can also be used while GitHub-hosted runner allocation is unavailable.

## Development workflow

- `main` = stable/tested baseline
- `develop/v0.2.0` = current feature development
- release APKs should use the same securely retained signing key so future versions can install over earlier versions
