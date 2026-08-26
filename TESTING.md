# Thyroid Echo — v0.3.0 device test

v0.2.0 successfully compiled, installed, and launched on a real Android device. v0.3.0 adds medication reminders and a premium visual pass while keeping the same local-first journal behavior.

## Build status

The Android source is on `develop/v0.3.0`. CI uses JDK 17, Android SDK 36, Build Tools 36.0.0, and Gradle 9.5 to build `:app:assembleDebug`.

## Install / upgrade

- [ ] Install the v0.3.0 debug APK over the existing v0.2.0 test build.
- [ ] Confirm existing profile, check-ins, medication history, and labs remain present.
- [ ] Confirm the app still opens without crashing.
- [ ] Confirm the launcher name/icon remain **Thyroid Echo**.

## Premium UI pass

- [ ] Confirm the app uses the Thyroid Echo indigo/teal/lavender palette rather than the phone wallpaper colors.
- [ ] Confirm light mode looks calm and readable.
- [ ] Confirm dark mode looks calm and readable.
- [ ] Confirm the bottom navigation uses clear icons and does not feel crowded.
- [ ] Confirm Today, History, and Medication cards/spacing look polished without feeling over-designed.
- [ ] Confirm text and controls remain readable at the phone's normal font/display size.

## Medication reminders

### Permission/setup
- [ ] Open **Medication** and find **Medication reminders**.
- [ ] Turn reminders on.
- [ ] Allow notification permission when Android asks.
- [ ] Pick a reminder time a few minutes in the future.
- [ ] Leave follow-up enabled and choose a short test delay where practical.
- [ ] Save reminder settings.
- [ ] If **Allow precise reminder timing** is shown, grant Android's **Alarms & reminders** access and return to Thyroid Echo.

### Primary reminder
- [ ] Do not log medication before the selected time.
- [ ] Confirm an audible/vibrating **Time for your thyroid medication** notification appears.
- [ ] Tap it and confirm Thyroid Echo opens to the normal app experience.
- [ ] Confirm medication name/dose are not exposed in the notification text.

### Follow-up reminder
- [ ] Leave today's medication status unlogged after the primary reminder.
- [ ] Confirm the follow-up says **No medication log yet**.
- [ ] Confirm the wording says the log is missing rather than claiming the medication was not taken.
- [ ] Open Thyroid Echo and save Taken, Late, or Missed.
- [ ] Confirm existing medication reminder notifications disappear after saving.

### Smart suppression
- [ ] On another test day/time, log Taken/Late/Missed before the scheduled primary reminder.
- [ ] Confirm the primary reminder is skipped because a medication log already exists.
- [ ] Confirm the follow-up is also skipped.

### Persistence/recovery
- [ ] Close Thyroid Echo completely and confirm the next scheduled reminder still arrives.
- [ ] Reopen the app and confirm reminder time/follow-up settings are preserved.
- [ ] If practical, reboot the phone and confirm reminder settings remain enabled and the next reminder is restored.

## Existing v0.2.0 regression checks

### Daily check-in
- [ ] Mark medication Taken, Late, or Missed.
- [ ] Adjust Overall, Energy, Mood, and Sleep.
- [ ] Adjust several condition-specific symptom sliders.
- [ ] Enter optional weight and a note.
- [ ] Save, close, reopen, and confirm values persist.
- [ ] Update today's entry and confirm it does not duplicate.

### Medication / labs / report
- [ ] Save a medication profile edit.
- [ ] Save a dose-change milestone and confirm it persists.
- [ ] Add a TSH/Free T4/T3 result as applicable, including reference ranges exactly as shown by the lab.
- [ ] Confirm the app does not label lab values normal/abnormal.
- [ ] Confirm Trends & Labs still renders existing data.
- [ ] Confirm Doctor Report still includes medication, check-ins, milestones, and labs.
- [ ] Confirm **Create & share PDF** works.
- [ ] Confirm **Share as text** works.

## What to report after testing

For any issue, capture the screen/feature, what was tapped, what happened, what was expected, and a screenshot when useful.

## Safety boundary

Thyroid Echo is a journal and trend-summary tool. It does not diagnose thyroid disease, interpret lab values, determine whether a medication dose is correct, or recommend medication changes.
