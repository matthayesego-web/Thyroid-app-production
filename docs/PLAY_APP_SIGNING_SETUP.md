# Thyroid Echo — Play App Signing setup

Thyroid Echo already has a permanent app-signing identity. The first Play setup should preserve that identity rather than accidentally creating an unrelated app-signing key.

## Existing Thyroid Echo app-signing certificate

- Alias: `thyroid-echo`
- Algorithm: RSA 4096
- SHA-256 certificate fingerprint:
  `87:A6:C6:BF:40:A6:9E:DA:D2:EA:24:6E:60:2E:F9:E5:FD:AE:E6:FC:44:54:C8:12:1D:23:3D:7E:AB:28:00:13`
- Package/application ID: `com.thyroidtracker.app`

## Preferred Play App Signing configuration

When Google Play asks how to configure the app-signing key for the first release, choose the option that lets you **provide/use your own existing app-signing key** rather than automatically choosing a different Google-generated signing identity.

Google Play App Signing can hold a protected copy of an app-signing key that the developer generated. This is the preferred Thyroid Echo setup because it keeps the same signing identity available for legitimate distribution outside Google Play as well.

Play may guide you through the PEPK/key-transfer process. Follow the current Play Console instructions exactly; do not upload the raw `.jks` or paste its password into source code, issues, pull requests, or chat logs.

The private keystore should remain in the creator's secure offline backups and GitHub Actions secret storage. The repository contains only the public fingerprint and signing instructions.

## Upload key after enrollment

Google recommends a separate upload key after Play App Signing is configured. The upload key authorizes future bundle uploads; Google then signs delivered APKs with the app-signing key.

Do not rotate or replace the permanent app-signing key casually. If Play asks about changing/rotating the app-signing key, stop and verify the proposed fingerprints before accepting.

## Verification after enrollment

In Play Console, open the Play App Signing page and compare the **App signing key certificate SHA-256** with the permanent Thyroid Echo fingerprint above.

If the fingerprint matches, the intended identity is preserved.

If it does not match, do not move the release beyond internal testing until the signing configuration is understood.
