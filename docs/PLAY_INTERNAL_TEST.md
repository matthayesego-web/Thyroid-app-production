# Thyroid Echo — Google Play internal test handoff

This checklist is for the first Google Play internal test of Thyroid Echo.

## Release identity

- App name: Thyroid Echo
- Suggested Play title: `Thyroid Echo: Symptom Tracker`
- Package/application ID: `com.thyroidtracker.app`
- Release format: Android App Bundle (`.aab`)
- Signing: permanent Thyroid Echo signing key is configured in GitHub Actions
- Version codes must increase for every Play upload

Important: once the first bundle is uploaded to the Play app entry, the package name is fixed. Do not create a second Play app with a different package ID unless that is intentional.

## Internal test setup

1. Create/select the Thyroid Echo app in Google Play Console.
2. Go to **Test and release → Testing → Internal testing**.
3. Create an internal tester list and add the tester's Google-account email address.
4. Add a feedback email address or feedback URL for the tester opt-in page.
5. Create a new internal-testing release.
6. Upload the signed Thyroid Echo `.aab` produced by the GitHub `Signed Android release` workflow.
7. Review any Play App Signing/key prompts carefully. Keep the existing Thyroid Echo signing identity in mind before accepting a key change or rotation.
8. Add the release notes from `docs/PLAY_RELEASE_NOTES.md`.
9. Save, review, and roll out the internal-test release.
10. Copy the internal-test opt-in/share link and open it while signed into the tester's approved Google account.
11. Install Thyroid Echo from Google Play rather than sideloading the APK.

Google Play internal testing supports up to 100 testers and is intended for fast distribution before closed testing.

## What to test first

For the first internal tester, prioritize normal use rather than trying every edge case at once:

- clean install from Google Play
- onboarding and optional first name
- hypothyroidism/hyperthyroidism selection
- medication profile and date picker
- medication reminder notification permission and reminder scheduling
- daily check-in with no symptoms
- daily check-in with symptoms and severities
- replacing today's check-in
- History presentation
- optional context tags and weight feature toggles
- standard thyroid lab entry
- expanded thyroid lab toggle and antibody entry
- doctor text and PDF report sharing
- app close/reopen with data retained
- Play-delivered update to a later version without losing local data

## Before closed beta

The closed beta should not begin until these are ready:

- public privacy-policy URL
- privacy policy linked inside the app
- Health apps declaration completed accurately
- store listing text and graphics
- Data safety form when required for the selected track/release state
- tester feedback channel
- release notes and changelog

Thyroid Echo should be declared as a health/medical app that supports **Diseases and Conditions Management** and **Medication and Treatment Management**. It is designed as a patient-entered tracking journal, not as clinical decision support or a medical-device diagnostic/treatment system.

## Deliberate non-features in this release line

- no account
- no analytics
- no ads
- no remote health database
- no diagnosis
- no lab interpretation
- no medication dose recommendation
- no AI medical advice
- no permanent cloud sync

Encrypted export/restore remains on the roadmap and should be added only after its restore path has automated coverage and at least one real device-to-device validation.
