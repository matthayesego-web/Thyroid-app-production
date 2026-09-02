package com.thyroidtracker.app.data

import java.time.LocalDate
import java.util.UUID

enum class ThyroidCondition(val displayName: String) {
    HYPOTHYROIDISM("Hypothyroidism"),
    HYPERTHYROIDISM("Hyperthyroidism")
}

enum class MedicationStatus(val displayName: String) {
    TAKEN("Taken"),
    LATE("Late"),
    MISSED("Missed"),
    NOT_LOGGED("Not logged")
}

data class UserProfile(
    val condition: ThyroidCondition,
    val firstName: String = "",
    val medicationName: String = "",
    val medicationDose: String = "",
    val medicationTime: String = "",
    val doseStartedOn: String = "",
    val largeText: Boolean = false
)

data class ReminderSettings(
    val enabled: Boolean = false,
    val reminderTime: String = "",
    val followUpEnabled: Boolean = true,
    val followUpDelayMinutes: Int = 60
)

data class FeatureSettings(
    val contextTagsEnabled: Boolean = true,
    val weightTrackingEnabled: Boolean = false,
    val expandedLabsEnabled: Boolean = false
)

data class DailyEntry(
    val date: String = LocalDate.now().toString(),
    // Kept only so pre-v0.3.8 data can be read safely. New medication logs are stored separately.
    val medicationStatus: MedicationStatus = MedicationStatus.NOT_LOGGED,
    val overall: Int = 5,
    val energy: Int = 5,
    val mood: Int = 5,
    val sleep: Int = 5,
    val weightKg: Double? = null,
    val hadSymptoms: Boolean = false,
    val symptoms: Map<String, Int> = emptyMap(),
    val contextTags: Set<String> = emptySet(),
    val notes: String = ""
)

data class MedicationLog(
    val date: String = LocalDate.now().toString(),
    val status: MedicationStatus,
    val recordedAtEpochMillis: Long = System.currentTimeMillis()
)

data class MedicationChange(
    val id: String = UUID.randomUUID().toString(),
    val date: String = LocalDate.now().toString(),
    val medicationName: String = "",
    val dose: String = "",
    val notes: String = ""
)

data class LabResult(
    val id: String = UUID.randomUUID().toString(),
    val date: String = LocalDate.now().toString(),
    val tsh: String = "",
    val tshRange: String = "",
    val freeT4: String = "",
    val freeT4Range: String = "",
    val t3: String = "",
    val t3Range: String = "",
    val tpoAb: String = "",
    val tpoAbRange: String = "",
    val tgAb: String = "",
    val tgAbRange: String = "",
    val trab: String = "",
    val trabRange: String = "",
    val notes: String = ""
)

data class AppState(
    val isLoaded: Boolean = false,
    val profile: UserProfile? = null,
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val featureSettings: FeatureSettings = FeatureSettings(),
    val entries: List<DailyEntry> = emptyList(),
    val medicationLogs: List<MedicationLog> = emptyList(),
    val medicationChanges: List<MedicationChange> = emptyList(),
    val labResults: List<LabResult> = emptyList()
)

data class SymptomDefinition(
    val id: String,
    val label: String,
    val helper: String
)

data class ContextTagDefinition(
    val id: String,
    val label: String
)

object ContextTagCatalog {
    val all = listOf(
        ContextTagDefinition("poor_sleep", "Poor sleep"),
        ContextTagDefinition("stress", "Stress"),
        ContextTagDefinition("illness", "Sick / illness"),
        ContextTagDefinition("exercise", "Exercise"),
        ContextTagDefinition("travel", "Travel"),
        ContextTagDefinition("diet_change", "Diet change")
    )

    fun labelFor(id: String): String = all.firstOrNull { it.id == id }?.label ?: id
}

object SymptomCatalog {
    val hypo = listOf(
        SymptomDefinition("fatigue", "Fatigue", "Unusual tiredness or low stamina"),
        SymptomDefinition("cold", "Feeling cold", "Cold intolerance compared with others"),
        SymptomDefinition("brain_fog", "Brain fog", "Forgetfulness or difficulty concentrating"),
        SymptomDefinition("constipation", "Constipation", "Slower or difficult bowel movements"),
        SymptomDefinition("dry_skin", "Dry skin", "Dry, rough or unusually flaky skin"),
        SymptomDefinition("hair", "Hair changes", "Dryness, thinning or increased shedding"),
        SymptomDefinition("low_mood", "Low mood", "Feeling down or less interested than usual"),
        SymptomDefinition("muscle_joint", "Muscle / joint discomfort", "Aches, stiffness or weakness")
    )

    val hyper = listOf(
        SymptomDefinition("heat", "Feeling hot / sweating", "Heat intolerance or increased sweating"),
        SymptomDefinition("palpitations", "Racing heart / palpitations", "Awareness of fast or pounding heartbeat"),
        SymptomDefinition("tremor", "Tremor", "Shakiness, especially in the hands"),
        SymptomDefinition("anxiety", "Nervousness / anxiety", "Feeling unusually keyed up or worried"),
        SymptomDefinition("irritability", "Irritability", "Feeling more easily frustrated or restless"),
        SymptomDefinition("sleep_difficulty", "Sleep difficulty", "Trouble falling or staying asleep"),
        SymptomDefinition("bowel", "Frequent bowel movements", "More frequent or looser bowel movements"),
        SymptomDefinition("hair", "Hair changes", "Thinning or increased shedding")
    )

    fun forCondition(condition: ThyroidCondition): List<SymptomDefinition> =
        if (condition == ThyroidCondition.HYPOTHYROIDISM) hypo else hyper
}
