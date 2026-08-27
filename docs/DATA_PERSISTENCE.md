# Thyroid Echo data persistence policy

Thyroid Echo's journal is local-first. Normal app updates must never delete or reset the user's saved journal.

## Update safety rules

- Keep the Android application ID `com.thyroidtracker.app` unchanged.
- Keep the DataStore name `thyroid_tracker` unchanged.
- Existing preference keys (`profile`, `reminder_settings`, `feature_settings`, `entries`, `medication_changes`, `lab_results`) are stable storage contracts and must not be renamed or cleared during an app update.
- Model changes must be additive/backward-compatible wherever possible. New JSON fields require safe defaults when older records do not contain them.
- Version upgrades must not call `clear()`, delete app storage, recreate the DataStore, or replace saved collections with empty defaults.
- A failed decoder must never be used as a reason to write empty data back over the stored value.
- Feature toggles hide inputs only; turning a feature off must not erase previously recorded values.
- Restore from encrypted backup is merge-safe: matching daily dates or record IDs may be updated, while unrelated existing records remain.

## What Android preserves

Installing a newer Play release over an existing Thyroid Echo installation preserves the app's private DataStore as long as the package/signing lineage remains valid. Uninstalling the app is different: Android removes private local app data.

Thyroid Echo intentionally excludes its health journal from Android cloud backup. Users should use Thyroid Echo's encrypted backup feature before uninstalling, resetting, or replacing a device.

## Release checklist

Before every release:

1. Increase `versionCode` only; do not change `applicationId`.
2. Verify existing storage keys and DataStore name remain intact.
3. Build from the permanent Play upload-signing pipeline.
4. Test an upgrade over the previous Play build with existing journal data before wider rollout when schema/storage code changes.
5. Test encrypted backup export and restore whenever the backup schema changes.
