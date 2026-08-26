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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.LabResult
import java.time.LocalDate

@Composable
internal fun TrendsAndLabsScreen(appState: AppState, onSaveLab: (LabResult) -> Unit, onSaved: () -> Unit) {
    val recent = appState.entries.take(7)
    val previous = appState.entries.drop(7).take(7)
    var showLabForm by remember { mutableStateOf(false) }
    var labDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var tsh by remember { mutableStateOf("") }
    var tshRange by remember { mutableStateOf("") }
    var freeT4 by remember { mutableStateOf("") }
    var freeT4Range by remember { mutableStateOf("") }
    var t3 by remember { mutableStateOf("") }
    var t3Range by remember { mutableStateOf("") }
    var labNotes by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Trends & Labs", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Compare recent check-ins and keep lab results beside medication changes.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (recent.isEmpty()) {
            EmptyCard("Not enough symptom data yet", "Check in for a few days and your averages will start appearing here.")
        } else {
            MetricSummaryCard("Recent average", "Overall", recent.map { it.overall.toDouble() }.average(), 10)
            MetricSummaryCard("Recent average", "Energy", recent.map { it.energy.toDouble() }.average(), 10)
            MetricSummaryCard("Recent average", "Sleep", recent.map { it.sleep.toDouble() }.average(), 10)
            if (previous.isNotEmpty()) {
                val current = recent.map { it.energy.toDouble() }.average()
                val old = previous.map { it.energy.toDouble() }.average()
                val direction = when {
                    current > old + 0.25 -> "Higher than the previous period"
                    current < old - 0.25 -> "Lower than the previous period"
                    else -> "Similar to the previous period"
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Energy comparison", fontWeight = FontWeight.Bold)
                        Text(direction, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Recent ${"%.1f".format(current)}/10 · Previous ${"%.1f".format(old)}/10")
                    }
                }
            }
        }

        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Lab results")
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showLabForm = !showLabForm }) {
                Text(if (showLabForm) "Hide form" else "Add result")
            }
        }
        Text(
            "Enter the result and the reference range exactly as shown by the laboratory. The app stores and reports the numbers without deciding whether they are normal or abnormal.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (showLabForm) {
            OutlinedTextField(labDate, { labDate = it }, Modifier.fillMaxWidth(), label = { Text("Lab date") }, placeholder = { Text("YYYY-MM-DD") }, singleLine = true)
            LabField("TSH", tsh, { tsh = it }, tshRange, { tshRange = it })
            LabField("Free T4", freeT4, { freeT4 = it }, freeT4Range, { freeT4Range = it })
            LabField("T3 (optional)", t3, { t3 = it }, t3Range, { t3Range = it })
            OutlinedTextField(labNotes, { labNotes = it }, Modifier.fillMaxWidth(), label = { Text("Lab notes (optional)") }, minLines = 2)
            Button(
                enabled = tsh.isNotBlank() || freeT4.isNotBlank() || t3.isNotBlank(),
                onClick = {
                    onSaveLab(
                        LabResult(
                            date = labDate.trim().ifBlank { LocalDate.now().toString() },
                            tsh = tsh.trim(),
                            tshRange = tshRange.trim(),
                            freeT4 = freeT4.trim(),
                            freeT4Range = freeT4Range.trim(),
                            t3 = t3.trim(),
                            t3Range = t3Range.trim(),
                            notes = labNotes.trim()
                        )
                    )
                    tsh = ""
                    tshRange = ""
                    freeT4 = ""
                    freeT4Range = ""
                    t3 = ""
                    t3Range = ""
                    labNotes = ""
                    showLabForm = false
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save lab result") }
        }

        if (appState.labResults.isEmpty()) {
            EmptyCard("No lab results saved", "Add TSH, Free T4 or other thyroid results when you have them.")
        } else {
            appState.labResults.take(10).forEach { lab -> LabResultCard(lab) }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun LabField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    range: String,
    onRangeChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text("Result + unit, exactly as shown") },
            singleLine = true
        )
        OutlinedTextField(
            value = range,
            onValueChange = onRangeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("$label reference range") },
            singleLine = true
        )
    }
}

@Composable
private fun LabResultCard(lab: LabResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formatDate(lab.date), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (lab.tsh.isNotBlank()) Text("TSH: ${lab.tsh}${rangeSuffix(lab.tshRange)}")
            if (lab.freeT4.isNotBlank()) Text("Free T4: ${lab.freeT4}${rangeSuffix(lab.freeT4Range)}")
            if (lab.t3.isNotBlank()) Text("T3: ${lab.t3}${rangeSuffix(lab.t3Range)}")
            if (lab.notes.isNotBlank()) Text(lab.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricSummaryCard(kicker: String, label: String, value: Double, max: Int) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(kicker, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("${"%.1f".format(value)} / $max", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
