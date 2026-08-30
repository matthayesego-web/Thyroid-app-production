package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PrivacyInfoCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Privacy & data")
            Text(
                "Your journal belongs to you. Thyroid Echo's developers do not receive, collect, or store the personal or health information you enter, and they have no remote access to your journal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Core tracking works offline at any time — no account or internet connection is required.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide privacy details" else "Read privacy details")
            }

            if (expanded) {
                Text(
                    "Information you enter — including your optional name, thyroid condition, medication logs, symptoms, notes, weight, labs, reminders, and preferences — is stored locally on your device. Thyroid Echo has no remote journal database, advertising profile, or analytics profile. Daily check-ins, medication reminders, history, labs, trends, doctor reports, and local encrypted backup/restore remain usable without internet access. Thyroid Echo may send a local daily check-in reminder when no entry has been saved for the day; that reminder is generated entirely on your device and contains no health details. Reports and encrypted backup files leave the app only when you explicitly choose to save, export, or share them through Android. Android cloud backup is excluded for journal data. Uninstalling the app removes its local journal data from that device unless you created your own encrypted backup first. Medication reminder notifications intentionally avoid showing medication name or dose. Thyroid Echo does not sell health information.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
