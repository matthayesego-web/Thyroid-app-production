package com.thyroidtracker.app.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.thyroidtracker.app.data.ReminderSettings
import com.thyroidtracker.app.data.UserProfile
import com.thyroidtracker.app.reminder.ReminderNotifications
import com.thyroidtracker.app.reminder.ReminderScheduler
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ReminderSettingsCard(
    profile: UserProfile,
    savedSettings: ReminderSettings,
    onSave: (ReminderSettings) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val startingTime = remember(savedSettings.reminderTime, profile.medicationTime) {
        savedSettings.reminderTime.ifBlank { normalizeMedicationTime(profile.medicationTime) ?: "07:00" }
    }

    var enabled by remember(savedSettings) { mutableStateOf(savedSettings.enabled) }
    var reminderTime by remember(startingTime) { mutableStateOf(startingTime) }
    var followUpEnabled by remember(savedSettings) { mutableStateOf(savedSettings.followUpEnabled) }
    var followUpDelay by remember(savedSettings) { mutableStateOf(savedSettings.followUpDelayMinutes) }
    var notificationsAllowed by remember { mutableStateOf(ReminderNotifications.canPostNotifications(context)) }
    var exactAllowed by remember { mutableStateOf(ReminderScheduler.canScheduleExact(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationsAllowed = granted }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = ReminderNotifications.canPostNotifications(context)
                exactAllowed = ReminderScheduler.canScheduleExact(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(Modifier.weight(1f)) {
                    Text("Medication reminders", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "A daily reminder, plus an optional follow-up only when today's medication is still not logged.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        enabled = checked
                        if (
                            checked &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !notificationsAllowed
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reminder time", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(
                    enabled = enabled,
                    onClick = {
                        val current = runCatching { LocalTime.parse(reminderTime) }.getOrDefault(LocalTime.of(7, 0))
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> reminderTime = String.format(Locale.US, "%02d:%02d", hour, minute) },
                            current.hour,
                            current.minute,
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null)
                    Spacer(Modifier.padding(horizontal = 5.dp))
                    Text(formatReminderTime(reminderTime))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Follow-up reminder", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Only alerts if Taken, Late, or Missed has not been logged.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                }
                Switch(
                    checked = enabled && followUpEnabled,
                    onCheckedChange = { followUpEnabled = it },
                    enabled = enabled
                )
            }

            if (enabled && followUpEnabled) {
                Text("Follow up after", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf(30, 60, 90, 120).forEach { minutes ->
                        FilterChip(
                            selected = followUpDelay == minutes,
                            onClick = { followUpDelay = minutes },
                            label = { Text(delayLabel(minutes)) }
                        )
                    }
                }
            }

            if (enabled) {
                PermissionStatusRow(
                    label = "Notifications",
                    value = if (notificationsAllowed) "Allowed" else "Permission needed",
                    good = notificationsAllowed
                )
                PermissionStatusRow(
                    label = "Timing",
                    value = if (exactAllowed) "Precise" else "Battery-friendly",
                    good = exactAllowed
                )

                if (!notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    OutlinedButton(
                        onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 5.dp))
                        Text("Allow notifications")
                    }
                }

                if (!exactAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Alarm, contentDescription = null)
                        Spacer(Modifier.padding(horizontal = 5.dp))
                        Text("Allow precise reminder timing")
                    }
                    Text(
                        "Without precise timing access, Android may shift the reminder slightly to save battery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                }
            }

            Button(
                onClick = {
                    onSave(
                        ReminderSettings(
                            enabled = enabled,
                            reminderTime = reminderTime,
                            followUpEnabled = followUpEnabled,
                            followUpDelayMinutes = followUpDelay
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (enabled) "Save reminder settings" else "Keep reminders off")
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(label: String, value: String, good: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = if (good) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun delayLabel(minutes: Int): String = when (minutes) {
    30 -> "30m"
    60 -> "1h"
    90 -> "90m"
    120 -> "2h"
    else -> "${minutes}m"
}

private fun formatReminderTime(raw: String): String = runCatching {
    LocalTime.parse(raw).format(DateTimeFormatter.ofPattern("h:mm a"))
}.getOrDefault(raw)

private fun normalizeMedicationTime(raw: String): String? {
    if (raw.isBlank()) return null
    val cleaned = raw.trim().uppercase(Locale.US).replace(" ", "")
    val patterns = listOf("h:mma", "ha", "H:mm", "HH:mm")
    patterns.forEach { pattern ->
        val parsed = runCatching {
            LocalTime.parse(cleaned, DateTimeFormatter.ofPattern(pattern, Locale.US))
        }.getOrNull()
        if (parsed != null) return parsed.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    return null
}
