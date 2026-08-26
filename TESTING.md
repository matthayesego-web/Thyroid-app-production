# Thyroid Tracker — v0.2.0 first-device test

This is the first hands-on Android test cycle. The goal is to validate the daily experience and local persistence before adding reminders, richer charts, or cloud features.

## Build status

The Android source is on `develop/v0.2.0`. GitHub-hosted runners currently are not being allocated for this private repository, so CI cannot yet produce the debug APK automatically.

## Build with Android Studio

Recommended environment:
- Android Studio Quail 3 | 2026.1.3 Patch 1 or newer stable
- JDK 17
- Android SDK Platform 37
- Android SDK Build Tools 36.0.0 or newer compatible version

Steps:
1. Check out or download the `develop/v0.2.0` branch.
2. Open the repository root in Android Studio.
3. Let Gradle sync and install any requested SDK components.
4. Choose **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK should be created at `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer the APK to the Android test phone and install it. Android may require permission for the app used to open the APK to install unknown apps.

## First test checklist

### Onboarding
- [ ] App opens without crashing.
- [ ] Select **Hypothyroidism**.
- [ ] Enter medication name, current dose, usual time, and optional dose-start date.
- [ ] Complete onboarding.

### Daily check-in
- [ ] Mark medication Taken, Late, or Missed.
- [ ] Adjust Overall, Energy, Mood, and Sleep.
- [ ] Adjust several hypothyroidism symptom sliders.
- [ ] Enter optional weight and a note.
- [ ] Save the check-in.
- [ ] Close the app completely and reopen it.
- [ ] Confirm today's values are still present.
- [ ] Change one value and save again; confirm today's entry updates rather than duplicating.

### History
- [ ] Confirm today's check-in appears in History.
- [ ] Confirm medication status, wellness values, symptom average, and note are readable.

### Medication
- [ ] Confirm current medication information is present.
- [ ] Save a dose-change milestone.
- [ ] Confirm the new dose becomes the current dose.
- [ ] Confirm the milestone remains in dose history after reopening the app.

### Labs
- [ ] Add a TSH result and its laboratory reference range.
- [ ] Add Free T4 if available.
- [ ] Add T3 only if it is part of the actual laboratory results.
- [ ] Confirm the app displays values exactly as entered and does not label them normal/abnormal.

### Trends and report
- [ ] Confirm recent averages appear after check-ins exist.
- [ ] Open Doctor Report.
- [ ] Confirm the summary includes medication, check-ins, symptom averages, milestones, and labs where available.
- [ ] Tap **Create & share PDF**.
- [ ] Confirm Android opens the share chooser and the PDF can be opened/shared.
- [ ] Confirm **Share as text** also opens the share chooser.

## What to report after testing

For any issue, capture:
- Screen/feature
- What was tapped
- What happened
- What was expected
- Screenshot if useful
- Whether the app was reopened before the problem occurred

## Safety boundary

The test build is a journal and trend-summary tool. It does not diagnose thyroid disease, interpret lab values, determine whether a medication dose is correct, or recommend medication changes.
