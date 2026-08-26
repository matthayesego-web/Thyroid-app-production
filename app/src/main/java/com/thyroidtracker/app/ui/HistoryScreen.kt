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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.MedicationStatus

@Composable
internal fun HistoryScreen(appState: AppState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("History", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Daily check-ins, newest first.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (appState.entries.isEmpty()) {
            item { EmptyCard("No check-ins yet", "Save the first daily check-in and it will appear here.") }
        } else {
            items(appState.entries, key = { it.date }) { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(formatDate(entry.date), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Overall ${entry.overall}/10 · Energy ${entry.energy}/10 · Sleep ${entry.sleep}/10")
                        if (entry.medicationStatus != MedicationStatus.NOT_LOGGED) {
                            Text("Medication: ${entry.medicationStatus.displayName}")
                        }
                        val average = if (entry.symptoms.isEmpty()) null else entry.symptoms.values.average()
                        average?.let { Text("Average symptom severity: ${"%.1f".format(it)}/4") }
                        if (entry.notes.isNotBlank()) Text(entry.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
