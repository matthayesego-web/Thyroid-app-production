package com.thyroidtracker.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

internal object BackupPayloadCodec {
    const val CURRENT_SCHEMA_VERSION = 1

    fun encode(state: AppState): String = JSONObject().apply {
        put("format", "thyroid-echo-backup")
        put("schemaVersion", CURRENT_SCHEMA_VERSION)
        put("exportedAt", Instant.now().toString())
        put("profile", state.profile?.let(::encodeProfile) ?: JSONObject.NULL)
        put("reminderSettings", encodeReminderSettings(state.reminderSettings))
        put("featureSettings", encodeFeatureSettings(state.featureSettings))
        put("entries", encodeEntries(state.entries))
        put("medicationChanges", encodeMedicationChanges(state.medicationChanges))
        put("labResults", encodeLabResults(state.labResults))
    }.toString()

    fun decode(raw: String): AppState {
        val root = JSONObject(raw)
        require(root.optString("format") == "thyroid-echo-backup") { "Not a Thyroid Echo backup" }
        val schemaVersion = root.optInt("schemaVersion", -1)
        require(schemaVersion in 1..CURRENT_SCHEMA_VERSION) { "Unsupported backup version" }

        val profile = root.optJSONObject("profile")?.let(::decodeProfile)
            ?: throw IllegalArgumentException("Backup is missing its profile")

        return AppState(
            isLoaded = true,
            profile = profile,
            reminderSettings = root.optJSONObject("reminderSettings")?.let(::decodeReminderSettings)
                ?: ReminderSettings(),
            featureSettings = root.optJSONObject("featureSettings")?.let(::decodeFeatureSettings)
                ?: FeatureSettings(),
            entries = decodeEntries(root.optJSONArray("entries") ?: JSONArray()),
            medicationChanges = decodeMedicationChanges(root.optJSONArray("medicationChanges") ?: JSONArray()),
            labResults = decodeLabResults(root.optJSONArray("labResults") ?: JSONArray())
        )
    }

    private fun encodeProfile(profile: UserProfile) = JSONObject().apply {
        put("condition", profile.condition.name)
        put("firstName", profile.firstName)
        put("medicationName", profile.medicationName)
        put("medicationDose", profile.medicationDose)
        put("medicationTime", profile.medicationTime)
        put("doseStartedOn", profile.doseStartedOn)
        put("largeText", profile.largeText)
    }

    private fun decodeProfile(obj: JSONObject) = UserProfile(
        condition = ThyroidCondition.valueOf(obj.getString("condition")),
        firstName = obj.optString("firstName"),
        medicationName = obj.optString("medicationName"),
        medicationDose = obj.optString("medicationDose"),
        medicationTime = obj.optString("medicationTime"),
        doseStartedOn = obj.optString("doseStartedOn"),
        largeText = obj.optBoolean("largeText", false)
    )

    private fun encodeReminderSettings(settings: ReminderSettings) = JSONObject().apply {
        put("enabled", settings.enabled)
        put("reminderTime", settings.reminderTime)
        put("followUpEnabled", settings.followUpEnabled)
        put("followUpDelayMinutes", settings.followUpDelayMinutes)
    }

    private fun decodeReminderSettings(obj: JSONObject) = ReminderSettings(
        enabled = obj.optBoolean("enabled", false),
        reminderTime = obj.optString("reminderTime"),
        followUpEnabled = obj.optBoolean("followUpEnabled", true),
        followUpDelayMinutes = obj.optInt("followUpDelayMinutes", 60).coerceIn(15, 360)
    )

    private fun encodeFeatureSettings(settings: FeatureSettings) = JSONObject().apply {
        put("contextTagsEnabled", settings.contextTagsEnabled)
        put("weightTrackingEnabled", settings.weightTrackingEnabled)
        put("expandedLabsEnabled", settings.expandedLabsEnabled)
    }

    private fun decodeFeatureSettings(obj: JSONObject) = FeatureSettings(
        contextTagsEnabled = obj.optBoolean("contextTagsEnabled", true),
        weightTrackingEnabled = obj.optBoolean("weightTrackingEnabled", false),
        expandedLabsEnabled = obj.optBoolean("expandedLabsEnabled", false)
    )

    private fun encodeEntries(entries: List<DailyEntry>) = JSONArray().apply {
        entries.forEach { entry ->
            put(JSONObject().apply {
                put("date", entry.date)
                put("medicationStatus", entry.medicationStatus.name)
                put("overall", entry.overall)
                put("energy", entry.energy)
                put("mood", entry.mood)
                put("sleep", entry.sleep)
                if (entry.weightKg == null) put("weightKg", JSONObject.NULL) else put("weightKg", entry.weightKg)
                put("hadSymptoms", entry.hadSymptoms)
                put("symptoms", JSONObject().apply { entry.symptoms.forEach { (key, value) -> put(key, value) } })
                put("contextTags", JSONArray().apply { entry.contextTags.sorted().forEach(::put) })
                put("notes", entry.notes)
            })
        }
    }

    private fun decodeEntries(array: JSONArray): List<DailyEntry> = buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val symptomObj = obj.optJSONObject("symptoms") ?: JSONObject()
            val symptoms = buildMap {
                val keys = symptomObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, symptomObj.optInt(key, 0))
                }
            }
            val tags = obj.optJSONArray("contextTags") ?: JSONArray()
            val contextTags = buildSet {
                for (j in 0 until tags.length()) tags.optString(j).takeIf(String::isNotBlank)?.let(::add)
            }
            add(
                DailyEntry(
                    date = obj.getString("date"),
                    medicationStatus = runCatching { MedicationStatus.valueOf(obj.optString("medicationStatus")) }
                        .getOrDefault(MedicationStatus.NOT_LOGGED),
                    overall = obj.optInt("overall", 5),
                    energy = obj.optInt("energy", 5),
                    mood = obj.optInt("mood", 5),
                    sleep = obj.optInt("sleep", 5),
                    weightKg = if (obj.isNull("weightKg")) null else obj.optDouble("weightKg"),
                    hadSymptoms = if (obj.has("hadSymptoms")) obj.optBoolean("hadSymptoms") else symptoms.values.any { it > 0 },
                    symptoms = symptoms,
                    contextTags = contextTags,
                    notes = obj.optString("notes")
                )
            )
        }
    }.sortedByDescending { it.date }

    private fun encodeMedicationChanges(changes: List<MedicationChange>) = JSONArray().apply {
        changes.forEach { change ->
            put(JSONObject().apply {
                put("id", change.id)
                put("date", change.date)
                put("medicationName", change.medicationName)
                put("dose", change.dose)
                put("notes", change.notes)
            })
        }
    }

    private fun decodeMedicationChanges(array: JSONArray): List<MedicationChange> = buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            add(
                MedicationChange(
                    id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                    date = obj.optString("date"),
                    medicationName = obj.optString("medicationName"),
                    dose = obj.optString("dose"),
                    notes = obj.optString("notes")
                )
            )
        }
    }.sortedByDescending { it.date }

    private fun encodeLabResults(results: List<LabResult>) = JSONArray().apply {
        results.forEach { lab ->
            put(JSONObject().apply {
                put("id", lab.id)
                put("date", lab.date)
                put("tsh", lab.tsh)
                put("tshRange", lab.tshRange)
                put("freeT4", lab.freeT4)
                put("freeT4Range", lab.freeT4Range)
                put("t3", lab.t3)
                put("t3Range", lab.t3Range)
                put("tpoAb", lab.tpoAb)
                put("tpoAbRange", lab.tpoAbRange)
                put("tgAb", lab.tgAb)
                put("tgAbRange", lab.tgAbRange)
                put("trab", lab.trab)
                put("trabRange", lab.trabRange)
                put("notes", lab.notes)
            })
        }
    }

    private fun decodeLabResults(array: JSONArray): List<LabResult> = buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            add(
                LabResult(
                    id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                    date = obj.optString("date"),
                    tsh = obj.optString("tsh"),
                    tshRange = obj.optString("tshRange"),
                    freeT4 = obj.optString("freeT4"),
                    freeT4Range = obj.optString("freeT4Range"),
                    t3 = obj.optString("t3"),
                    t3Range = obj.optString("t3Range"),
                    tpoAb = obj.optString("tpoAb"),
                    tpoAbRange = obj.optString("tpoAbRange"),
                    tgAb = obj.optString("tgAb"),
                    tgAbRange = obj.optString("tgAbRange"),
                    trab = obj.optString("trab"),
                    trabRange = obj.optString("trabRange"),
                    notes = obj.optString("notes")
                )
            )
        }
    }.sortedByDescending { it.date }
}
