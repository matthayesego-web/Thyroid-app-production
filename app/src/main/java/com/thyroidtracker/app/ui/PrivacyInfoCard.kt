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
                "Your thyroid journal stays on this device during normal use. Thyroid Echo does not require an account and does not include ads or analytics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide privacy details" else "Read privacy details")
            }

            if (expanded) {
                Text(
                    "Information you enter — including your optional name, thyroid condition, medication logs, symptoms, notes, weight, labs, and reminder preferences — is stored locally by Thyroid Echo. The app does not upload your journal to a Thyroid Echo server in this release. Android cloud backup is excluded for journal data. Reports are created on your device and leave the app only when you choose to share them through Android. Uninstalling the app removes its local journal data from that device. Medication reminder notifications intentionally avoid showing medication name or dose. Thyroid Echo does not sell health information.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
