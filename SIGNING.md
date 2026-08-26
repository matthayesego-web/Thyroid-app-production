# Thyroid Echo Android signing

Thyroid Echo uses one permanent Android signing identity for installable release APKs and future store bundles.

## Permanent certificate identity

- Key alias: `thyroid-echo`
- Algorithm: RSA 4096-bit
- Certificate subject: `CN=Thyroid Echo, OU=Mobile Apps, O=Thyroid Echo, L=Peterborough, ST=Ontario, C=CA`
- SHA-256 certificate fingerprint: `87:A6:C6:BF:40:A6:9E:DA:D2:EA:24:6E:60:2E:F9:E5:FD:AE:E6:FC:44:54:C8:12:1D:23:3D:7E:AB:28:00:13`

The private keystore and its passwords must never be committed to this repository. The existing `.gitignore` excludes `*.jks` and `*.keystore` files.

## GitHub Actions secrets required for signed releases

The `Signed Android release` workflow reads four protected repository secrets:

- `THYROID_KEYSTORE_BASE64`
- `THYROID_KEYSTORE_PASSWORD`
- `THYROID_KEY_ALIAS`
- `THYROID_KEY_PASSWORD`

`THYROID_KEYSTORE_BASE64` is the base64-encoded permanent keystore. The alias is `thyroid-echo`. Password values are stored only in the private signing backup supplied to the project owner.

## Release outputs

The signed release workflow produces both:

- `ThyroidEcho-vX.Y.Z-release.apk` for direct Android installation/testing
- `ThyroidEcho-vX.Y.Z-release.aab` for future store distribution

Before distributing a release, verify the signing certificate fingerprint matches the SHA-256 fingerprint above.

## Key custody

Keep at least two encrypted backups of the permanent keystore and credentials in separate locations. Do not send the private keystore through issue comments, pull requests, source control, or public file shares.
