package com.thyroidtracker.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.json.JSONArray
import org.json.JSONObject

private val Context.thyroidDataStore by preferencesDataStore(name = "thyroid_tracker")

class ThyroidRepository(private val context: Context) {
    private val profileKey = stringPreferencesKey("profile")
    private val reminderSettingsKey = stringPreferencesKey("reminder_settings")
    private val entriesKey = stringPreferencesKey("entries")
    private val medicationChangesKey = stringPreferencesKey("medication_changes")
    private val labResultsKey = stringPreferencesKey("lab_results")
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val state = context.thyroidDataStore.data
        .map(::decodeState)
        .catch { emit(AppState(isLoaded = true)) }
        .stateIn(repositoryScope, SharingStarted.Eagerly, AppState())

    suspend fun snapshot(): AppState = context.thyroidDataStore.data
        .map(::decodeState)
        .first()

    suspend fun saveProfile(profile: UserProfile) {
        context.thyroidDataStore.edit { prefs -> prefs[profileKey] = encodeProfile(profile) }
    }

    suspend fun saveReminderSettings(settings: ReminderSettings) {
        context.thyroidDataStore.edit { prefs ->
            prefs[reminderSettingsKey] = encodeReminderSettings(settings)
        }
    }

    suspend fun saveEntry(entry: DailyEntry) {
        context.thyroidDataStore.edit { prefs ->
            val current = prefs[entriesKey]?.let(::decodeEntries).orEmpty().toMutableList()
            val existingIndex = current.indexOfFirst { it.date == entry.date }
            if (existingIndex >= 0) current[existingIndex] = entry else current += entry
            prefs[entriesKey] = encodeEntries(current.sortedByDescending { it.date })
        }
    }

    suspend fun saveMedicationChange(change: MedicationChange) {
        context.thyroidDataStore.edit { prefs ->
            val current = prefs[medicationChangesKey]?.let(::decodeMedicationChanges).orEmpty().toMutableList()
            val existingIndex = current.indexOfFirst { it.id == change.id }
            if (existingIndex >= 0) current[existingIndex] = change else current += change
            prefs[medicationChangesKey] = encodeMedicationChanges(current.sortedByDescending { it.date })
        }
    }

    suspend fun saveLabResult(result: LabResult) {
        context.thyroidDataStore.edit { prefs ->
            val current = prefs[labResultsKey]?.let(::decodeLabResults).orEmpty().toMutableList()
            val existingIndex = current.indexOfFirst { it.id == result.id }
            if (existingIndex >= 0) current[existingIndex] = result else current += result
            prefs[labResultsKey] = encodeLabResults(current.sortedByDescending { it.date })
        }
    }

    private fun decodeState(prefs: Preferences): AppState = AppState(
        isLoaded = true,
        profile = prefs[profileKey]?.let(::decodeProfile),
        reminderSettings = prefs[reminderSettingsKey]?.let(::decodeReminderSettings) ?: ReminderSettings(),
        entries = prefs[entriesKey]?.let(::decodeEntries).orEmpty(),
        medicationChanges = prefs[medicationChangesKey]?.let(::decodeMedicationChanges).orEmpty(),
        labResults = prefs[labResultsKey]?.let(::decodeLabResults).orEmpty()
    )

    private fun encodeProfile(profile: UserProfile): String = JSONObject().apply {
        put("condition", profile.condition.name)
        put("medicationName", profile.medicationName)
        put("medicationDose", profile.medicationDose)
        put("medicationTime", profile.medicationTime)
        put("doseStartedOn", profile.doseStartedOn)
    }.toString()

    private fun decodeProfile(raw: String): UserProfile? = runCatching {
        val obj = JSONObject(raw)
        UserProfile(
            condition = ThyroidCondition.valueOf(obj.getString("condition")),
            medicationName = obj.optString("medicationName"),
            medicationDose = obj.optString("medicationDose"),
            medicationTime = obj.optString("medicationTime"),
            doseStartedOn = obj.optString("doseStartedOn")
        )
    }.getOrNull()

    private fun encodeReminderSettings(settings: ReminderSettings): String = JSONObject().apply {
        put("enabled", settings.enabled)
        put("reminderTime", settings.reminderTime)
        put("followUpEnabled", settings.followUpEnabled)
        put("followUpDelayMinutes", settings.followUpDelayMinutes)
    }.toString()

    private fun decodeReminderSettings(raw: String): ReminderSettings = runCatching {
        val obj = JSONObject(raw)
        ReminderSettings(
            enabled = obj.optBoolean("enabled", false),
            reminderTime = obj.optString("reminderTime"),
            followUpEnabled = obj.optBoolean("followUpEnabled", true),
            followUpDelayMinutes = obj.optInt("followUpDelayMinutes", 60).coerceIn(15, 360)
        )
    }.getOrDefault(ReminderSettings())

    private fun encodeEntries(entries: List<DailyEntry>): String = JSONArray().apply {
        entries.forEach { entry ->
            put(JSONObject().apply {
                put("date", entry.date)
                put("medicationStatus", entry.medicationStatus.name)
                put("overall", entry.overall)
                put("energy", entry.energy)
                put("mood", entry.mood)
                put("sleep", entry.sleep)
                if (entry.weightKg != null) put("weightKg", entry.weightKg) else put("weightKg", JSONObject.NULL)
                put("notes", entry.notes)
                put("symptoms", JSONObject().apply {
                    entry.symptoms.forEach { (key, value) -> put(key, value) }
                })
            })
        }
    }.toString()

    private fun decodeEntries(raw: String): List<DailyEntry> = runCatching {
        val array = JSONArray(raw)
        buildList {
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
                add(
                    DailyEntry(
                        date = obj.getString("date"),
                        medicationStatus = runCatching {
                            MedicationStatus.valueOf(obj.optString("medicationStatus", MedicationStatus.NOT_LOGGED.name))
                        }.getOrDefault(MedicationStatus.NOT_LOGGED),
                        overall = obj.optInt("overall", 5),
                        energy = obj.optInt("energy", 5),
                        mood = obj.optInt("mood", 5),
                        sleep = obj.optInt("sleep", 5),
                        weightKg = if (obj.isNull("weightKg")) null else obj.optDouble("weightKg"),
                        symptoms = symptoms,
                        notes = obj.optString("notes")
                    )
                )
            }
        }.sortedByDescending { it.date }
    }.getOrDefault(emptyList())

    private fun encodeMedicationChanges(changes: List<MedicationChange>): String = JSONArray().apply {
        changes.forEach { change ->
            put(JSONObject().apply {
                put("id", change.id)
                put("date", change.date)
                put("medicationName", change.medicationName)
                put("dose", change.dose)
                put("notes", change.notes)
            })
        }
    }.toString()

    private fun decodeMedicationChanges(raw: String): List<MedicationChange> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    MedicationChange(
                        id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                        date = obj.optString("date"),
                        medicationName = obj.optString("medicationName"),
                        dose = obj.optString("dose"),
                        notes = obj.optString("notes")
                    )
                )
            }
        }.sortedByDescending { it.date }
    }.getOrDefault(emptyList())

    private fun encodeLabResults(results: List<LabResult>): String = JSONArray().apply {
        results.forEach { result ->
            put(JSONObject().apply {
                put("id", result.id)
                put("date", result.date)
                put("tsh", result.tsh)
                put("tshRange", result.tshRange)
                put("freeT4", result.freeT4)
                put("freeT4Range", result.freeT4Range)
                put("t3", result.t3)
                put("t3Range", result.t3Range)
                put("notes", result.notes)
            })
        }
    }.toString()

    private fun decodeLabResults(raw: String): List<LabResult> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    LabResult(
                        id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                        date = obj.optString("date"),
                        tsh = obj.optString("tsh"),
                        tshRange = obj.optString("tshRange"),
                        freeT4 = obj.optString("freeT4"),
                        freeT4Range = obj.optString("freeT4Range"),
                        t3 = obj.optString("t3"),
                        t3Range = obj.optString("t3Range"),
                        notes = obj.optString("notes")
                    )
                )
            }
        }.sortedByDescending { it.date }
    }.getOrDefault(emptyList())
}
