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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.MedicationChange
import com.thyroidtracker.app.data.ReminderSettings
import com.thyroidtracker.app.data.UserProfile
import java.time.LocalDate

@Composable
internal fun MedicationScreen(
    appState: AppState,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveReminderSettings: (ReminderSettings) -> Unit,
    onSaveChange: (MedicationChange) -> Unit,
    onSaved: (String) -> Unit
) {
    val profile = appState.profile ?: return
    var name by remember(profile) { mutableStateOf(profile.medicationName) }
    var dose by remember(profile) { mutableStateOf(profile.medicationDose) }
    var time by remember(profile) { mutableStateOf(profile.medicationTime) }
    var startedOn by remember(profile) { mutableStateOf(profile.doseStartedOn) }
    var firstName by remember(profile) { mutableStateOf(profile.firstName) }
    var largeText by remember(profile) { mutableStateOf(profile.largeText) }

    var changeDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var changeDose by remember { mutableStateOf("") }
    var changeNotes by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(
            title = "Medication",
            subtitle = "Keep your current dose, reminders, and clinician-directed dose changes together."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Current medication")
                Text(
                    profile.condition.displayName,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Medication name") }, singleLine = true)
                OutlinedTextField(dose, { dose = it }, Modifier.fillMaxWidth(), label = { Text("Current dose") }, singleLine = true)
                OutlinedTextField(time, { time = it }, Modifier.fillMaxWidth(), label = { Text("Usual time") }, placeholder = { Text("e.g. 7:00 AM") }, singleLine = true)
                DatePickerField(
                    label = "Current dose started",
                    date = startedOn,
                    onDateChange = { startedOn = it },
                    optional = true
                )
                Button(
                    onClick = {
                        onSaveProfile(
                            profile.copy(
                                medicationName = name.trim(),
                                medicationDose = dose.trim(),
                                medicationTime = time.trim(),
                                doseStartedOn = startedOn.trim()
                            )
                        )
                        onSaved("Medication profile saved")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save current medication") }
            }
        }

        ReminderSettingsCard(
            profile = profile.copy(
                medicationName = name.trim(),
                medicationDose = dose.trim(),
                medicationTime = time.trim(),
                doseStartedOn = startedOn.trim()
            ),
            savedSettings = appState.reminderSettings,
            onSave = {
                onSaveReminderSettings(it)
                onSaved(if (it.enabled) "Medication reminders saved" else "Medication reminders turned off")
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Personalization & display")
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("First name (optional)") },
                    placeholder = { Text("Used for your greeting") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Larger text & controls", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Increase the app's reading size without changing your phone settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = largeText, onCheckedChange = { largeText = it })
                }
                Text(
                    "These preferences stay on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        onSaveProfile(
                            profile.copy(
                                firstName = firstName.trim(),
                                largeText = largeText
                            )
                        )
                        onSaved("Preferences saved")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save preferences") }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Log a dose change")
                Text(
                    "Use this whenever the prescribing clinician changes the medication or dose. Thyroid Echo records the milestone; it does not recommend one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                DatePickerField(
                    label = "Change date",
                    date = changeDate,
                    onDateChange = { changeDate = it },
                    optional = false
                )
                OutlinedTextField(changeDose, { changeDose = it }, Modifier.fillMaxWidth(), label = { Text("New dose") }, placeholder = { Text("e.g. 112 mcg") }, singleLine = true)
                OutlinedTextField(changeNotes, { changeNotes = it }, Modifier.fillMaxWidth(), label = { Text("Notes (optional)") }, minLines = 2)
                OutlinedButton(
                    enabled = changeDose.isNotBlank(),
                    onClick = {
                        val effectiveDate = changeDate.trim().ifBlank { LocalDate.now().toString() }
                        onSaveChange(
                            MedicationChange(
                                date = effectiveDate,
                                medicationName = name.trim(),
                                dose = changeDose.trim(),
                                notes = changeNotes.trim()
                            )
                        )
                        onSaveProfile(
                            profile.copy(
                                medicationName = name.trim(),
                                medicationDose = changeDose.trim(),
                                medicationTime = time.trim(),
                                doseStartedOn = effectiveDate
                            )
                        )
                        dose = changeDose.trim()
                        startedOn = effectiveDate
                        changeDose = ""
                        changeNotes = ""
                        onSaved("Dose-change milestone saved")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save dose-change milestone") }
            }
        }

        if (appState.medicationChanges.isNotEmpty()) {
            SectionTitle("Dose history")
            appState.medicationChanges.take(10).forEach { change ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(formatDate(change.date), style = MaterialTheme.typography.titleMedium)
                        Text("${change.medicationName.ifBlank { "Medication" }} · ${change.dose}")
                        if (change.notes.isNotBlank()) {
                            Text(change.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        SafetyCard()
        Spacer(Modifier.height(10.dp))
    }
}
