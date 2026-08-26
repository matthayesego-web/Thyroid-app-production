package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.MedicationChange
import com.thyroidtracker.app.data.UserProfile
import java.time.LocalDate

@Composable
internal fun MedicationScreen(
    appState: AppState,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveChange: (MedicationChange) -> Unit,
    onSaved: (String) -> Unit
) {
    val profile = appState.profile ?: return
    var name by remember(profile) { mutableStateOf(profile.medicationName) }
    var dose by remember(profile) { mutableStateOf(profile.medicationDose) }
    var time by remember(profile) { mutableStateOf(profile.medicationTime) }
    var startedOn by remember(profile) { mutableStateOf(profile.doseStartedOn) }

    var changeDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var changeDose by remember { mutableStateOf("") }
    var changeNotes by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Medication", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(profile.condition.displayName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

        SectionTitle("Current medication")
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Medication name") }, singleLine = true)
        OutlinedTextField(dose, { dose = it }, Modifier.fillMaxWidth(), label = { Text("Current dose") }, singleLine = true)
        OutlinedTextField(time, { time = it }, Modifier.fillMaxWidth(), label = { Text("Usual time") }, singleLine = true)
        OutlinedTextField(startedOn, { startedOn = it }, Modifier.fillMaxWidth(), label = { Text("Current dose started") }, placeholder = { Text("YYYY-MM-DD") }, singleLine = true)
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

        HorizontalDivider()
        SectionTitle("Log a dose change")
        Text(
            "Use this whenever the prescribing clinician changes the medication or dose. The app records the milestone; it does not recommend one.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(changeDate, { changeDate = it }, Modifier.fillMaxWidth(), label = { Text("Change date") }, placeholder = { Text("YYYY-MM-DD") }, singleLine = true)
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

        if (appState.medicationChanges.isNotEmpty()) {
            SectionTitle("Dose history")
            appState.medicationChanges.take(10).forEach { change ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(formatDate(change.date), fontWeight = FontWeight.Bold)
                        Text("${change.medicationName.ifBlank { "Medication" }} · ${change.dose}")
                        if (change.notes.isNotBlank()) Text(change.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        SafetyCard()
        Spacer(Modifier.height(10.dp))
    }
}
