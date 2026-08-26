package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.DailyEntry
import com.thyroidtracker.app.data.MedicationStatus
import com.thyroidtracker.app.data.SymptomCatalog
import com.thyroidtracker.app.data.UserProfile
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
internal fun TodayScreen(profile: UserProfile, entries: List<DailyEntry>, onSave: (DailyEntry) -> Unit) {
    val today = LocalDate.now().toString()
    val existing = entries.firstOrNull { it.date == today }
    var medStatus by remember(existing) { mutableStateOf(existing?.medicationStatus ?: MedicationStatus.NOT_LOGGED) }
    var overall by remember(existing) { mutableIntStateOf(existing?.overall ?: 5) }
    var energy by remember(existing) { mutableIntStateOf(existing?.energy ?: 5) }
    var mood by remember(existing) { mutableIntStateOf(existing?.mood ?: 5) }
    var sleep by remember(existing) { mutableIntStateOf(existing?.sleep ?: 5) }
    var weight by remember(existing) { mutableStateOf(existing?.weightKg?.toString().orEmpty()) }
    var notes by remember(existing) { mutableStateOf(existing?.notes.orEmpty()) }
    val symptomScores = remember(existing, profile.condition) {
        mutableStateMapOf<String, Int>().apply {
            SymptomCatalog.forCondition(profile.condition).forEach { symptom ->
                put(symptom.id, existing?.symptoms?.get(symptom.id) ?: 0)
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Today", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(formatDate(today), color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (profile.medicationName.isNotBlank()) {
            SectionTitle("Medication")
            Text(
                buildString {
                    append(profile.medicationName)
                    if (profile.medicationDose.isNotBlank()) append(" · ${profile.medicationDose}")
                    if (profile.medicationTime.isNotBlank()) append(" · ${profile.medicationTime}")
                },
                fontWeight = FontWeight.SemiBold
            )
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

        SectionTitle("How are you feeling?")
        ScoreSlider("Overall", overall) { overall = it }
        ScoreSlider("Energy", energy) { energy = it }
        ScoreSlider("Mood", mood) { mood = it }
        ScoreSlider("Sleep quality", sleep) { sleep = it }

        SectionTitle("Symptoms")
        SymptomCatalog.forCondition(profile.condition).forEach { symptom ->
            SymptomSlider(
                label = symptom.label,
                helper = symptom.helper,
                value = symptomScores[symptom.id] ?: 0,
                onChange = { symptomScores[symptom.id] = it }
            )
        }

        SectionTitle("Optional details")
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
            placeholder = { Text("Anything different today? Timing, stress, illness, exercise, meals, etc.") }
        )

        Button(
            onClick = {
                onSave(
                    DailyEntry(
                        date = today,
                        medicationStatus = medStatus,
                        overall = overall,
                        energy = energy,
                        mood = mood,
                        sleep = sleep,
                        weightKg = weight.toDoubleOrNull(),
                        symptoms = symptomScores.toMap(),
                        notes = notes.trim()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (existing == null) "Save today's check-in" else "Update today's check-in")
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ScoreSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
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
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, fontWeight = FontWeight.SemiBold)
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
