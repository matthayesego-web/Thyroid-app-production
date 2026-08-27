# Thyroid Echo — Play Console declaration notes

These notes are intended to make the Play Console forms faster and more consistent with the app's actual behavior. They are not legal advice; final answers should match the exact app build being submitted.

## Health apps declaration

Thyroid Echo should be declared as offering health features.

Recommended health categories based on the current functionality:

- **Diseases and Conditions Management** — Thyroid Echo is focused on user-entered tracking of hypothyroidism/hyperthyroidism symptoms, labs, and condition history.
- **Medication and Treatment Management** — Thyroid Echo includes medication logging and optional medication reminders.

Do **not** select Clinical Decision Support based on the current app: Thyroid Echo does not calculate doses, assess risk, interpret labs, diagnose, or recommend treatment.

Do **not** identify the current app as a medical-device app unless qualified regulatory advice later determines that the product is regulated as one in a market where it is distributed.

## Health-data behavior summary

Current app behavior:

- user manually enters health information
- journal data remains local on the device during normal use
- no account required
- no third-party analytics SDK
- no advertising SDK
- no health-data sale
- no background upload of journal data
- no Health Connect integration
- no fitness/wearable data access
- reports are generated locally and leave the app only after a user-initiated Android share action

## Permissions currently used

- `POST_NOTIFICATIONS` — optional medication reminders
- `SCHEDULE_EXACT_ALARM` — optional reminder timing where Android permits it
- `RECEIVE_BOOT_COMPLETED` — restore enabled reminders after reboot

The current manifest does not request Internet permission.

## Privacy policy

Google Play health-app policy requires a public privacy-policy URL and privacy-policy link or text inside the app for health/medical apps. The policy text is drafted in `docs/PRIVACY_POLICY.md`.

Before closed beta/review:

1. publish that policy at an active public non-PDF URL;
2. replace the placeholder contact section with the chosen public support/privacy email;
3. add the public policy URL inside Thyroid Echo.

## Data Safety direction

For the current local-only build, the app developer does not collect journal/health data into a developer-controlled server during normal use. User-initiated sharing of a locally generated report should be described consistently with Play's definitions and the final implementation.

If encrypted relay/device transfer is later released, revisit the Data Safety answers before publishing that version even if the relay is zero-knowledge encrypted, because the app's network behavior will have changed.

## Content / medical claims wording

Use these concepts consistently:

- “track”
- “record”
- “organize”
- “summarize patient-entered information”
- “prepare for appointments”

Avoid claims such as:

- “diagnoses”
- “detects thyroid problems”
- “optimizes medication”
- “tells you whether labs are normal”
- “recommends a dose”
- “replaces your doctor”

## Internal testing note

Google Play's internal testing track can be used before the app is fully configured. For the later closed test, complete the policy/declaration work above before requesting review.
