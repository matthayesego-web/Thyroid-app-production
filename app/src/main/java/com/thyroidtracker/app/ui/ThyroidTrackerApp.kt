package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.DailyEntry
import com.thyroidtracker.app.data.FeatureSettings
import com.thyroidtracker.app.data.LabResult
import com.thyroidtracker.app.data.MedicationChange
import com.thyroidtracker.app.data.ReminderSettings
import com.thyroidtracker.app.data.ThyroidCondition
import com.thyroidtracker.app.data.ThyroidRepository
import com.thyroidtracker.app.data.UserProfile
import com.thyroidtracker.app.reminder.ReminderNotifications
import com.thyroidtracker.app.reminder.ReminderScheduler
import kotlinx.coroutines.launch

private enum class MainTab(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Rounded.Home),
    JOURNAL("Journal", Icons.Rounded.History),
    MEDICATION("Medication", Icons.Rounded.Medication),
    INSIGHTS("Insights", Icons.Rounded.Insights),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun ThyroidTrackerApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = remember { ThyroidRepository(appContext) }
    val appState by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val baseDensity = LocalDensity.current
    val displayDensity = if (appState.profile?.largeText == true) {
        Density(baseDensity.density, maxOf(baseDensity.fontScale, 1.18f))
    } else {
        baseDensity
    }

    LaunchedEffect(Unit) {
        ReminderNotifications.ensureChannel(appContext)
    }

    LaunchedEffect(appState.isLoaded, appState.reminderSettings) {
        if (appState.isLoaded) {
            ReminderScheduler.scheduleAll(appContext, appState.reminderSettings)
        }
    }

    CompositionLocalProvider(LocalDensity provides displayDensity) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                !appState.isLoaded -> LoadingScreen()
                appState.profile == null -> OnboardingScreen(
                    onFinish = { profile -> scope.launch { repository.saveProfile(profile) } }
                )
                else -> MainShell(
                    appState = appState,
                    onSaveEntry = { entry, onComplete ->
                        scope.launch {
                            repository.saveEntry(entry)
                            ReminderNotifications.clearMedicationNotifications(appContext)
                            onComplete()
                        }
                    },
                    onSaveProfile = { scope.launch { repository.saveProfile(it) } },
                    onSaveReminderSettings = { settings ->
                        scope.launch {
                            repository.saveReminderSettings(settings)
                            ReminderScheduler.scheduleAll(appContext, settings)
                            if (!settings.enabled) {
                                ReminderNotifications.clearMedicationNotifications(appContext)
                            }
                        }
                    },
                    onSaveFeatureSettings = { settings ->
                        scope.launch { repository.saveFeatureSettings(settings) }
                    },
                    onSaveMedicationChange = { scope.launch { repository.saveMedicationChange(it) } },
                    onSaveLabResult = { scope.launch { repository.saveLabResult(it) } },
                    onRestoreBackup = { restored, onComplete ->
                        scope.launch {
                            restored.profile?.let { repository.saveProfile(it) }
                            repository.saveReminderSettings(restored.reminderSettings)
                            repository.saveFeatureSettings(restored.featureSettings)
                            restored.entries.forEach { repository.saveEntry(it) }
                            restored.medicationChanges.forEach { repository.saveMedicationChange(it) }
                            restored.labResults.forEach { repository.saveLabResult(it) }
                            ReminderNotifications.clearMedicationNotifications(appContext)
                            ReminderScheduler.scheduleAll(appContext, restored.reminderSettings)
                            onComplete()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CircularProgressIndicator()
            Text("Thyroid Echo", style = MaterialTheme.typography.headlineMedium)
            Text("Loading your private journal", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OnboardingScreen(onFinish: (UserProfile) -> Unit) {
    var condition by remember { mutableStateOf<ThyroidCondition?>(null) }
    var firstName by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var doseStartedOn by remember { mutableStateOf("") }
    var largeText by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 38.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ScreenHeader(
            title = "Your thyroid journal",
            subtitle = "Private, calm tracking for symptoms, medication, labs, and the details you want to bring to appointments."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Make it yours · optional")
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("First name") },
                    placeholder = { Text("Used only for greetings") },
                    singleLine = true
                )
                Text(
                    "Your name stays on this device and is never used to create an account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Larger text & controls", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Makes Thyroid Echo easier to read and tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = largeText, onCheckedChange = { largeText = it })
                }
            }
        }

        SectionTitle("What are you tracking?")
        Text(
            "Choose one condition to continue. Everything in the medication section is optional.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ConditionCard(
            title = "Hypothyroidism",
            subtitle = "Track fatigue, cold intolerance, brain fog, bowel changes and other common symptoms.",
            selected = condition == ThyroidCondition.HYPOTHYROIDISM,
            onClick = {
                condition = ThyroidCondition.HYPOTHYROIDISM
                validationMessage = null
            }
        )
        ConditionCard(
            title = "Hyperthyroidism",
            subtitle = "Track heat intolerance, palpitations, tremor, sleep changes and other common symptoms.",
            selected = condition == ThyroidCondition.HYPERTHYROIDISM,
            onClick = {
                condition = ThyroidCondition.HYPERTHYROIDISM
                validationMessage = null
            }
        )
        validationMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Medication · optional")
                Text(
                    "Add what you know now, or skip any field and fill it in later from the Medication tab.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = medication,
                    onValueChange = { medication = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Medication name (optional)") },
                    placeholder = { Text("e.g. Levothyroxine") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Current dose (optional)") },
                    placeholder = { Text("e.g. 100 mcg") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Usual time (optional)") },
                    placeholder = { Text("e.g. 7:00 AM") },
                    singleLine = true
                )
                DatePickerField(
                    label = "Current dose started",
                    date = doseStartedOn,
                    onDateChange = { doseStartedOn = it },
                    optional = true
                )
            }
        }

        SafetyCard()

        Button(
            onClick = {
                val selectedCondition = condition
                if (selectedCondition == null) {
                    validationMessage = "Choose Hypothyroidism or Hyperthyroidism to continue."
                } else {
                    validationMessage = null
                    onFinish(
                        UserProfile(
                            condition = selectedCondition,
                            firstName = firstName.trim(),
                            medicationName = medication.trim(),
                            medicationDose = dose.trim(),
                            medicationTime = time.trim(),
                            doseStartedOn = doseStartedOn.trim(),
                            largeText = largeText
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start tracking")
        }
    }
}

@Composable
private fun ConditionCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (selected) {
                Text(
                    "Selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MainShell(
    appState: AppState,
    onSaveEntry: (DailyEntry, () -> Unit) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveReminderSettings: (ReminderSettings) -> Unit,
    onSaveFeatureSettings: (FeatureSettings) -> Unit,
    onSaveMedicationChange: (MedicationChange) -> Unit,
    onSaveLabResult: (LabResult) -> Unit,
    onRestoreBackup: (AppState, () -> Unit) -> Unit
) {
    var tab by remember { mutableStateOf(MainTab.TODAY) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            when (tab) {
                MainTab.TODAY -> TodayScreen(
                    profile = appState.profile!!,
                    reminderSettings = appState.reminderSettings,
                    featureSettings = appState.featureSettings,
                    entries = appState.entries,
                    onSave = { entry ->
                        onSaveEntry(entry) {
                            scope.launch { snackbar.showSnackbar("Today's check-in saved") }
                        }
                    }
                )
                MainTab.JOURNAL -> HistoryScreen(appState)
                MainTab.MEDICATION -> MedicationScreen(
                    appState = appState,
                    onSaveProfile = onSaveProfile,
                    onSaveReminderSettings = onSaveReminderSettings,
                    onSaveChange = onSaveMedicationChange,
                    onSaved = { message -> scope.launch { snackbar.showSnackbar(message) } }
                )
                MainTab.INSIGHTS -> InsightsScreen(
                    appState = appState,
                    onSaveLab = onSaveLabResult,
                    onSaved = { scope.launch { snackbar.showSnackbar("Lab result saved") } }
                )
                MainTab.SETTINGS -> SettingsScreen(
                    appState = appState,
                    onSaveProfile = onSaveProfile,
                    onSaveFeatureSettings = onSaveFeatureSettings,
                    onRestoreBackup = { restored ->
                        onRestoreBackup(restored) {
                            scope.launch { snackbar.showSnackbar("Encrypted backup restored") }
                        }
                    },
                    onSaved = { message -> scope.launch { snackbar.showSnackbar(message) } }
                )
            }
        }
    }
}
