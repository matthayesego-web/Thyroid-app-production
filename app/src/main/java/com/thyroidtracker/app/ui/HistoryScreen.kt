package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.ContextTagCatalog
import com.thyroidtracker.app.data.MedicationStatus
import com.thyroidtracker.app.data.SymptomCatalog

@Composable
internal fun HistoryScreen(appState: AppState) {
    val symptomLabels = appState.profile
        ?.let { SymptomCatalog.forCondition(it.condition).associate { symptom -> symptom.id to symptom.label } }
        .orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "Journal",
                subtitle = "Your daily check-ins, newest first."
            )
        }
        if (appState.entries.isEmpty()) {
            item { EmptyCard("No check-ins yet", "Save the first daily check-in and it will appear here.") }
        } else {
            items(appState.entries, key = { it.date }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(formatDate(entry.date), style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Overall ${entry.overall}/10 · Energy ${entry.energy}/10 · Sleep ${entry.sleep}/10",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.medicationStatus != MedicationStatus.NOT_LOGGED) {
                            Text(
                                "Medication · ${entry.medicationStatus.displayName}",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        if (entry.hadSymptoms) {
                            val reported = entry.symptoms
                                .filterValues { it > 0 }
                                .entries
                                .sortedByDescending { it.value }
                                .take(3)
                            if (reported.isEmpty()) {
                                Text("Symptoms · Reported", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text(
                                    "Symptoms · ${reported.joinToString(" · ") { (id, severity) ->
                                        "${symptomLabels[id] ?: id} ${symptomSeverityText(severity)}"
                                    }}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            Text("Symptoms · None reported", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (entry.contextTags.isNotEmpty()) {
                            Text(
                                "Context · ${entry.contextTags.joinToString(" · ") { ContextTagCatalog.labelFor(it) }}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        entry.weightKg?.let { weight ->
                            Text(
                                "Weight · ${"%.1f".format(weight)} kg",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (entry.notes.isNotBlank()) {
                            Text(entry.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun symptomSeverityText(value: Int): String = when (value) {
    1 -> "Mild"
    2 -> "Moderate"
    3 -> "Strong"
    4 -> "Severe"
    else -> value.toString()
}
