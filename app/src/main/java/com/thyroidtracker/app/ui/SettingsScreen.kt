package com.thyroidtracker.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.BackupPayloadCodec
import com.thyroidtracker.app.data.EncryptedBackup
import com.thyroidtracker.app.data.FeatureSettings
import com.thyroidtracker.app.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
internal fun SettingsScreen(
    appState: AppState,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveFeatureSettings: (FeatureSettings) -> Unit,
    onRestoreBackup: (AppState) -> Unit,
    onSaved: (String) -> Unit
) {
    val profile = appState.profile ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firstName by remember(profile) { mutableStateOf(profile.firstName) }
    var largeText by remember(profile) { mutableStateOf(profile.largeText) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var exportConfirm by remember { mutableStateOf("") }
    var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restorePassword by remember { mutableStateOf("") }
    var pendingRestore by remember { mutableStateOf<AppState?>(null) }

    val createBackupFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val bytes = pendingExportBytes
        pendingExportBytes = null
        if (uri != null && bytes != null) {
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                            ?: error("Could not open backup destination")
                    }
                }
                if (result.isSuccess) {
                    onSaved("Encrypted backup saved")
                } else {
                    onSaved("Could not save the backup file")
                }
            }
        }
    }

    val openBackupFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val password = restorePassword.toCharArray()
            scope.launch {
                val result = runCatching {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: error("Could not open backup file")
                    }
                    val raw = withContext(Dispatchers.Default) {
                        EncryptedBackup.decrypt(bytes, password)
                    }
                    BackupPayloadCodec.decode(raw)
                }
                result.onSuccess {
                    pendingRestore = it
                    showRestoreDialog = false
                    restorePassword = ""
                }.onFailure {
                    restorePassword = ""
                    onSaved("Backup could not be opened. Check the file and password.")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ScreenHeader(
            title = "Settings",
            subtitle = "Personalize Thyroid Echo, choose optional tracking tools, and manage your private data."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Personalization & accessibility")
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
                            "Increase Thyroid Echo's reading size without changing the rest of your phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = largeText, onCheckedChange = { largeText = it })
                }
                OutlinedButton(
                    onClick = {
                        onSaveProfile(profile.copy(firstName = firstName.trim(), largeText = largeText))
                        onSaved("Preferences saved")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save preferences") }
            }
        }

        OptionalFeaturesCard(
            savedSettings = appState.featureSettings,
            onSave = {
                onSaveFeatureSettings(it)
                onSaved("Optional features saved")
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Encrypted backup & restore")
                Text(
                    "Create a password-protected backup of your profile, journal, medication history, labs, reminders, and preferences. The file is encrypted before it leaves Thyroid Echo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Create encrypted backup") }
                OutlinedButton(
                    onClick = { showRestoreDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Restore encrypted backup") }
                Text(
                    "Restore merges the backup into this device. Matching dates or record IDs are updated; unrelated records already on this phone are not erased.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrivacyInfoCard()
        SafetyCard()
        Spacer(Modifier.height(10.dp))
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
                exportPassword = ""
                exportConfirm = ""
            },
            title = { Text("Protect your backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose a password with at least 8 characters. Thyroid Echo does not store it and cannot recover it for you.")
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Backup password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = exportConfirm,
                        onValueChange = { exportConfirm = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    if (exportConfirm.isNotEmpty() && exportPassword != exportConfirm) {
                        Text("Passwords do not match.", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = exportPassword.length >= 8 && exportPassword == exportConfirm,
                    onClick = {
                        val password = exportPassword.toCharArray()
                        showExportDialog = false
                        exportPassword = ""
                        exportConfirm = ""
                        scope.launch {
                            val bytes = withContext(Dispatchers.Default) {
                                EncryptedBackup.encrypt(BackupPayloadCodec.encode(appState), password)
                            }
                            pendingExportBytes = bytes
                            createBackupFile.launch("ThyroidEcho-${LocalDate.now()}.tebackup")
                        }
                    }
                ) { Text("Choose save location") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    exportPassword = ""
                    exportConfirm = ""
                }) { Text("Cancel") }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestoreDialog = false
                restorePassword = ""
            },
            title = { Text("Restore encrypted backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter the password used when the backup was created, then choose the .tebackup file.")
                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Backup password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = restorePassword.isNotBlank(),
                    onClick = {
                        openBackupFile.launch(arrayOf("application/octet-stream", "application/*"))
                    }
                ) { Text("Choose backup file") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    restorePassword = ""
                }) { Text("Cancel") }
            }
        )
    }

    pendingRestore?.let { restored ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Restore this backup?") },
            text = {
                Text(
                    "Backup contains ${restored.entries.size} check-ins, ${restored.labResults.size} lab records, and ${restored.medicationChanges.size} dose-change records. Existing unrelated records on this phone will remain."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRestoreBackup(restored)
                    pendingRestore = null
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("Cancel") }
            }
        )
    }
}
