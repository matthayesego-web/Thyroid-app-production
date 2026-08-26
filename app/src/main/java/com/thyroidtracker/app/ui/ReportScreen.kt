package com.thyroidtracker.app.ui

import android.content.Intent
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.MedicationStatus
import com.thyroidtracker.app.data.SymptomCatalog
import com.thyroidtracker.app.report.createDoctorReportPdf

@Composable
internal fun ReportScreen(appState: AppState) {
    val context = LocalContext.current
    var reportCount by remember { mutableIntStateOf(30) }
    val summary = remember(appState, reportCount) { buildDoctorSummary(appState, reportCount) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Doctor Report", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("A concise patient-entered summary for an appointment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 90, 9999).forEach { count ->
                FilterChip(
                    selected = reportCount == count,
                    onClick = { reportCount = count },
                    label = { Text(if (count == 9999) "All" else "$count entries") }
                )
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Text(summary, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            enabled = appState.entries.isNotEmpty() || appState.labResults.isNotEmpty() || appState.medicationChanges.isNotEmpty(),
            onClick = {
                val file = createDoctorReportPdf(context, summary)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_SUBJECT, "Thyroid symptom summary")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share thyroid PDF"))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create & share PDF") }
        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Thyroid symptom summary")
                    putExtra(Intent.EXTRA_TEXT, summary)
                }
                context.startActivity(Intent.createChooser(intent, "Share thyroid summary"))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Share as text") }
        SafetyCard()
        Spacer(Modifier.height(10.dp))
    }
}

private fun buildDoctorSummary(appState: AppState, maxEntries: Int = 30): String {
    val profile = appState.profile ?: return "No profile available."
    val entries = if (maxEntries >= 9999) appState.entries else appState.entries.take(maxEntries)
    val adherenceCount = entries.count { it.medicationStatus == MedicationStatus.TAKEN || it.medicationStatus == MedicationStatus.LATE }
    val medLoggedCount = entries.count { it.medicationStatus != MedicationStatus.NOT_LOGGED }
    val adherence = if (medLoggedCount == 0) null else adherenceCount * 100.0 / medLoggedCount
    val catalog = SymptomCatalog.forCondition(profile.condition).associateBy { it.id }
    val symptomAverages = catalog.mapNotNull { (id, def) ->
        val values = entries.mapNotNull { it.symptoms[id]?.toDouble() }
        if (values.isEmpty()) null else def.label to values.average()
    }.sortedByDescending { it.second }

    return buildString {
        appendLine("THYROID ECHO — PATIENT SUMMARY")
        appendLine()
        appendLine("Condition: ${profile.condition.displayName}")
        if (profile.medicationName.isNotBlank()) {
            appendLine("Current medication: ${profile.medicationName}${if (profile.medicationDose.isNotBlank()) " ${profile.medicationDose}" else ""}")
            if (profile.doseStartedOn.isNotBlank()) appendLine("Current dose since: ${profile.doseStartedOn}")
        }
        appendLine("Check-ins included: ${entries.size}")

        if (entries.isNotEmpty()) {
            appendLine()
            appendLine("SYMPTOM / WELLNESS SUMMARY")
            appendLine("Average overall: ${"%.1f".format(entries.map { it.overall }.average())}/10")
            appendLine("Average energy: ${"%.1f".format(entries.map { it.energy }.average())}/10")
            appendLine("Average mood: ${"%.1f".format(entries.map { it.mood }.average())}/10")
            appendLine("Average sleep: ${"%.1f".format(entries.map { it.sleep }.average())}/10")
            adherence?.let { appendLine("Medication adherence when logged: ${"%.0f".format(it)}%") }
            if (symptomAverages.isNotEmpty()) {
                appendLine("Reported symptom averages (0 none — 4 severe):")
                symptomAverages.forEach { (name, avg) -> appendLine("• $name: ${"%.1f".format(avg)}/4") }
            }
        }

        if (appState.medicationChanges.isNotEmpty()) {
            appendLine()
            appendLine("MEDICATION / DOSE MILESTONES")
            appState.medicationChanges.take(10).forEach { change ->
                append("• ${change.date}: ${change.medicationName.ifBlank { "Medication" }} ${change.dose}")
                if (change.notes.isNotBlank()) append(" — ${change.notes}")
                appendLine()
            }
        }

        if (appState.labResults.isNotEmpty()) {
            appendLine()
            appendLine("LAB RESULTS (AS ENTERED)")
            appState.labResults.take(10).forEach { lab ->
                appendLine("• ${lab.date}")
                if (lab.tsh.isNotBlank()) appendLine("  TSH: ${lab.tsh}${rangeSuffix(lab.tshRange)}")
                if (lab.freeT4.isNotBlank()) appendLine("  Free T4: ${lab.freeT4}${rangeSuffix(lab.freeT4Range)}")
                if (lab.t3.isNotBlank()) appendLine("  T3: ${lab.t3}${rangeSuffix(lab.t3Range)}")
                if (lab.notes.isNotBlank()) appendLine("  Note: ${lab.notes}")
            }
        }

        val latestNotes = entries.filter { it.notes.isNotBlank() }.take(3)
        if (latestNotes.isNotEmpty()) {
            appendLine()
            appendLine("RECENT PATIENT NOTES")
            latestNotes.forEach { appendLine("• ${it.date}: ${it.notes}") }
        }

        appendLine()
        append("This is a patient-entered tracking summary. It does not diagnose thyroid disease, interpret lab values, or recommend medication changes.")
    }
}
