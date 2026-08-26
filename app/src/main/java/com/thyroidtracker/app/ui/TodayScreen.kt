package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.ContextTagCatalog
import com.thyroidtracker.app.data.DailyEntry
import com.thyroidtracker.app.data.MedicationStatus
import com.thyroidtracker.app.data.ReminderSettings
import com.thyroidtracker.app.data.SymptomCatalog
import com.thyroidtracker.app.data.UserProfile
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

@Composable
internal fun TodayScreen(
    profile: UserProfile,
    reminderSettings: ReminderSettings,
    entries: List<DailyEntry>,
    onSave: (DailyEntry) -> Unit
) {
    val today = LocalDate.now().toString()
    val existing = entries.firstOrNull { it.date == today }
    val symptomCatalog = SymptomCatalog.forCondition(profile.condition)
    val contextCatalog = ContextTagCatalog.all

    var medStatus by remember(today) { mutableStateOf(MedicationStatus.NOT_LOGGED) }
    var overall by remember(today) { mutableIntStateOf(5) }
    var energy by remember(today) { mutableIntStateOf(5) }
    var mood by remember(today) { mutableIntStateOf(5) }
    var sleep by remember(today) { mutableIntStateOf(5) }
    var symptomsToday by remember(today) { mutableStateOf<Boolean?>(null) }
    var weight by remember(today) { mutableStateOf("") }
    var notes by remember(today) { mutableStateOf("") }
    val symptomScores = remember(today, profile.condition) {
        mutableStateMapOf<String, Int>().apply {
            symptomCatalog.forEach { symptom -> put(symptom.id, 0) }
        }
    }
    val selectedContextTags = remember(today) { mutableStateMapOf<String, Boolean>() }

    fun clearForm() {
        medStatus = MedicationStatus.NOT_LOGGED
        overall = 5
        energy = 5
        mood = 5
        sleep = 5
        symptomsToday = null
        symptomCatalog.forEach { symptom -> symptomScores[symptom.id] = 0 }
        selectedContextTags.clear()
        weight = ""
        notes = ""
    }

    val greeting = remember(profile.firstName) {
        val prefix = when (LocalTime.now().hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
        if (profile.firstName.isBlank()) prefix else "$prefix, ${profile.firstName.trim()}"
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(title = greeting, subtitle = formatDate(today))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Today at a glance")
                StatusRow("Check-in", if (existing == null) "Not done" else "Saved")
                StatusRow(
                    "Medication",
                    when {
                        profile.medicationName.isBlank() -> "Not configured"
                        existing?.medicationStatus != null && existing.medicationStatus != MedicationStatus.NOT_LOGGED -> existing.medicationStatus.displayName
                        else -> "Not logged"
                    }
                )
                if (reminderSettings.enabled && reminderSettings.reminderTime.isNotBlank()) {
                    StatusRow("Reminder", reminderSettings.reminderTime)
                }
            }
        }

        if (existing != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    "A check-in is already saved for today. Submitting this fresh form will replace it.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (profile.medicationName.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Medication", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(
                                buildString {
                                    append(profile.medicationName)
                                    if (profile.medicationDose.isNotBlank()) append(" · ${profile.medicationDose}")
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (profile.medicationTime.isNotBlank()) {
                                Text(
                                    "Usual time · ${profile.medicationTime}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f)
                                )
                            }
                        }
                        Text(
                            medStatus.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(MedicationStatus.TAKEN, MedicationStatus.LATE, MedicationStatus.MISSED).forEach { status ->
                            FilterChip(
                                selected = medStatus == status,
                                onClick = { medStatus = status },
                                label = { Text(status.displayName) }
                            )
                        }
                    }
                }
            }
        }

        SectionTitle("How are you feeling?")
        ScoreSlider("Overall", overall) { overall = it }
        ScoreSlider("Energy", energy) { energy = it }
        ScoreSlider("Mood", mood) { mood = it }
        ScoreSlider("Sleep quality", sleep) { sleep = it }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Are you feeling symptoms today?")
                Text(
                    "Choose Yes to open the symptom list. Each item includes a short description of what it covers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = symptomsToday == false,
                        onClick = {
                            symptomsToday = false
                            symptomCatalog.forEach { symptom -> symptomScores[symptom.id] = 0 }
                        },
                        label = { Text("No") }
                    )
                    FilterChip(
                        selected = symptomsToday == true,
                        onClick = { symptomsToday = true },
                        label = { Text("Yes") }
                    )
                }
            }
        }

        if (symptomsToday == true) {
            SectionTitle("What are you feeling?")
            Text(
                "Set the intensity for anything you are experiencing today. Leave symptoms you are not feeling at None.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            symptomCatalog.forEach { symptom ->
                SymptomSlider(
                    label = symptom.label,
                    helper = symptom.helper,
                    value = symptomScores[symptom.id] ?: 0,
                    onChange = { symptomScores[symptom.id] = it }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Optional context")
                Text(
                    "Add quick context without writing a note.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                contextCatalog.chunked(2).forEach { tagRow ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        tagRow.forEach { tag ->
                            FilterChip(
                                selected = selectedContextTags[tag.id] == true,
                                onClick = { selectedContextTags[tag.id] = selectedContextTags[tag.id] != true },
                                label = { Text(tag.label) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Weight (kg)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") },
                    minLines = 3,
                    placeholder = { Text("Anything else worth remembering.") }
                )
            }
        }

        if (symptomsToday == null) {
            Text(
                "Choose Yes or No for today's symptoms before saving.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            enabled = symptomsToday != null,
            onClick = {
                val savedSymptoms = if (symptomsToday == true) {
                    symptomScores.toMap()
                } else {
                    symptomCatalog.associate { it.id to 0 }
                }
                onSave(
                    DailyEntry(
                        date = today,
                        medicationStatus = medStatus,
                        overall = overall,
                        energy = energy,
                        mood = mood,
                        sleep = sleep,
                        weightKg = weight.toDoubleOrNull(),
                        hadSymptoms = symptomsToday == true,
                        symptoms = savedSymptoms,
                        contextTags = selectedContextTags.filterValues { it }.keys,
                        notes = notes.trim()
                    )
                )
                clearForm()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (existing == null) "Save today's check-in" else "Replace today's check-in")
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ScoreSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("$value / 10", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.roundToInt().coerceIn(1, 10)) },
                valueRange = 1f..10f,
                steps = 8
            )
        }
    }
}

@Composable
private fun SymptomSlider(label: String, helper: String, value: Int, onChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                    Text(helper, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(symptomSeverityLabel(value), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.roundToInt().coerceIn(0, 4)) },
                valueRange = 0f..4f,
                steps = 3
            )
        }
    }
}

private fun symptomSeverityLabel(value: Int): String = when (value) {
    0 -> "None"
    1 -> "Mild"
    2 -> "Moderate"
    3 -> "Strong"
    else -> "Severe"
}
