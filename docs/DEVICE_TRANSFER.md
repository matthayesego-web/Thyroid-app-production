# Thyroid Echo — encrypted device transfer design

## Goal
Allow a user to move all Thyroid Echo records from one Android device to another without creating an account and without keeping readable health data in cloud storage.

## Privacy model
- Normal Thyroid Echo use remains local-only.
- Device transfer is opt-in and temporary.
- The old phone creates the backup archive locally.
- The archive is encrypted on-device before upload.
- The relay stores only ciphertext and minimal non-health metadata.
- The relay never receives the decryption secret.
- Successful import requests immediate deletion of the temporary object.
- A short automatic expiration is a fallback if the transfer is never completed.
- No medication names, lab values, symptoms, notes, diagnosis/condition, or other journal data appear in object names, metadata, logs, or URLs.

## User experience
### Old phone
1. Open Settings / Data & privacy > Move to a new phone.
2. Tap Create transfer.
3. Thyroid Echo builds a versioned archive containing profile, reminder settings, daily entries, medication-change history, and lab history.
4. The archive is compressed and encrypted locally.
5. Only the encrypted object is uploaded.
6. The app shows:
   - a QR code for easiest transfer;
   - a human-readable recovery code as a manual fallback;
   - an expiration time;
   - a warning to keep the old phone/data until the import succeeds.

### New phone
1. Install Thyroid Echo.
2. Choose Restore from another phone before or during onboarding.
3. Scan the QR code or enter the recovery code.
4. The app derives the lookup identifier and encryption key locally.
5. The encrypted transfer object is downloaded.
6. The archive is authenticated and decrypted locally.
7. The app validates the archive schema and shows a local-only import summary.
8. After successful transactional import, the app requests immediate deletion of the temporary remote object.
9. If deletion cannot be confirmed, the app retries deletion and the server-side expiration remains the fallback.

## Cryptographic design
Use platform-provided cryptography rather than a custom cipher.

- Generate a random transfer secret locally with SecureRandom.
- Target at least 128 bits of entropy for a manual recovery secret.
- Derive a 256-bit encryption key from the transfer secret with HKDF-SHA-256 using a versioned Thyroid Echo transfer context string.
- Encrypt the compressed archive with AES-256-GCM using a unique cryptographically random nonce.
- GCM provides confidentiality plus authentication; corrupted or modified ciphertext must fail closed before import.
- Derive the remote lookup identifier from a one-way hash of the transfer secret so the relay does not need the secret itself.
- Never log the transfer secret, plaintext archive, decryption key, or user-entered recovery code.
- Discard transient key material from normal app state after transfer setup/import has completed.

The cross-device transfer secret must not depend on Android Keystore because Android Keystore keys intentionally do not move between devices. Android Keystore remains appropriate for device-bound keys, but the transfer archive needs a portable user-held secret.

## Recovery code format
Prefer a QR code plus a human-readable fallback with roughly 128 bits of entropy, for example a 12-word randomly generated phrase or equivalent error-resistant Base32 representation.

Do not use a short 4- or 6-digit PIN as the only decryption secret. Short codes are suitable only as rate-limited lookup identifiers, not as the sole cryptographic key for health-data backup.

## Archive format
Use a versioned envelope so future Thyroid Echo releases can import old transfers safely.

Logical plaintext before encryption:

```text
schemaVersion
exportedAt
appVersion
profile
reminderSettings
entries[]
medicationChanges[]
labResults[]
```

The encrypted object should contain a small binary/version header, nonce, and authenticated ciphertext. No readable health fields should exist outside the encrypted payload.

Imports should be transactional: either the complete verified archive is committed to local storage or nothing is changed.

## Temporary relay
Recommended implementation: a small Cloudflare Worker in front of a private R2 bucket.

Why:
- R2 supports private object storage and direct programmatic deletion.
- A Worker can expose only the minimal create/upload/fetch/delete endpoints required by Thyroid Echo.
- Temporary access can be tightly limited and rate-limited.
- The bucket can have an expiration lifecycle as a cleanup safety net.

The app must never receive R2 administrative credentials. The Worker owns storage access.

Suggested endpoints:

```text
POST   /v1/transfers            create upload authorization for a random object id
PUT    /v1/transfers/{id}       upload encrypted payload
GET    /v1/transfers/{id}       retrieve encrypted payload
DELETE /v1/transfers/{id}       delete after verified local import
```

Implementation can combine or replace these with short-lived presigned upload/download URLs if useful, but one-time/redeemed state should be enforced by the Worker rather than relying only on a reusable URL.

## Retention
Recommended default: delete on first confirmed successful import, with automatic expiry after 48 hours if never redeemed.

Immediate deletion should be performed through the storage API after successful import. Bucket lifecycle expiration is a fallback rather than the primary deletion mechanism because lifecycle cleanup can occur after the exact expiration time rather than instantaneously.

Bucket versioning/retention features that would intentionally preserve deleted transfer objects should not be enabled for this use case.

## Abuse / backend protections
- HTTPS only.
- Private bucket; no public listing.
- Strict maximum payload size.
- Rate-limit transfer creation and repeated fetch attempts.
- Do not log request bodies, recovery codes, decrypted content, or health-derived metadata.
- Generic object ids only.
- No analytics SDK on transfer payload paths.
- Reject malformed/oversized payloads before storage.
- Do not expose storage credentials to the Android app.

## Optional no-cloud fallback
Also support Export encrypted backup file / Import encrypted backup file.

This produces the same encrypted Thyroid Echo archive but lets the user move it with Android Quick Share, USB, SD card, or another method. This path uses no relay at all and is useful for users who want zero temporary cloud storage.

## Important deletion semantics
Deleting the relay object removes Thyroid Echo's accessible temporary copy. Cloud storage providers may use redundant physical storage internally, so an absolute statement that every physical bit is immediately erased everywhere cannot be guaranteed by the app. The design minimizes that risk by ensuring the provider only ever holds strongly encrypted ciphertext and never receives the decryption secret.

## Build policy during current development
This design is being developed on `work/v0.3.2-symptom-flow`, which is intentionally outside the automatic `develop/**` APK workflow. No APK should be produced from this work until the permanent Android signing setup has been completed.