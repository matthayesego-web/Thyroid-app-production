# Thyroid Tracker

Private Android-only thyroid symptom and medication journal built with Kotlin + Jetpack Compose.

## Stable baseline

Current stable baseline: **v0.1.0**

### v0.1.0 features
- First-launch choice: Hypothyroidism or Hyperthyroidism
- Optional medication name, dose and usual time
- Daily Taken / Late / Missed medication tracking
- Daily Overall, Energy, Mood and Sleep scores
- Condition-specific symptom severity tracking
- Optional weight and free-text notes
- On-device history
- Basic recent trends
- Shareable doctor-summary text
- Local persistence with AndroidX DataStore
- Cloud/transfer backup excluded for health-journal data
- Material 3 + dynamic light/dark colors

## Medical boundary

This app is a journal and trend summarizer. It does not diagnose thyroid disease, determine whether medication is medically effective, interpret lab values, or recommend dose changes.

## Privacy

The initial versions are local-first. No account, analytics, advertising, remote database, or health-data upload is required.

## Development workflow

- `main` = stable/testable baseline
- development branches = upcoming milestones
- signed APK releases will be versioned once device testing begins
