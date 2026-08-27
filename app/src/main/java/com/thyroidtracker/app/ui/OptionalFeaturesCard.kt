package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.thyroidtracker.app.data.FeatureSettings

@Composable
internal fun OptionalFeaturesCard(
    savedSettings: FeatureSettings,
    onSave: (FeatureSettings) -> Unit
) {
    var contextTagsEnabled by remember(savedSettings) { mutableStateOf(savedSettings.contextTagsEnabled) }
    var weightTrackingEnabled by remember(savedSettings) { mutableStateOf(savedSettings.weightTrackingEnabled) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle("Optional features")
            Text(
                "Keep Thyroid Echo simple by showing only the extras that are useful to you. Turning a feature off hides its input; it does not erase information you already saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FeatureToggleRow(
                title = "Quick context tags",
                description = "Show one-tap tags such as stress, poor sleep, exercise, illness, travel, and diet change.",
                checked = contextTagsEnabled,
                onCheckedChange = { contextTagsEnabled = it }
            )

            FeatureToggleRow(
                title = "Weight tracking",
                description = "Show an optional weight field in the daily check-in.",
                checked = weightTrackingEnabled,
                onCheckedChange = { weightTrackingEnabled = it }
            )

            Text(
                "More optional thyroid-specific tools can be added here without making the daily screen busier for everyone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = {
                    onSave(
                        FeatureSettings(
                            contextTagsEnabled = contextTagsEnabled,
                            weightTrackingEnabled = weightTrackingEnabled
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save optional features")
            }
        }
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
