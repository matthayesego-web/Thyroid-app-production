package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.DailyEntry
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
    HISTORY("History", Icons.Rounded.History),
    MEDICATION("Medication", Icons.Rounded.Medication),
    TRENDS("Trends", Icons.Rounded.TrendingUp),
    REPORT("Report", Icons.Rounded.Description)
}

@Composable
fun ThyroidTrackerApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = remember { ThyroidRepository(appContext) }
    val appState by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        ReminderNotifications.ensureChannel(appContext)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            !appState.isLoaded -> LoadingScreen()
            appState.profile == null -> OnboardingScreen(
                onFinish = { scope.launch { repository.saveProfile(it) } }
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
                    }
                },
                onSaveMedicationChange = { scope.launch { repository.saveMedicationChange(it) } },
                onSaveLabResult = { scope.launch { repository.saveLabResult(it) } }
            )
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
    var medication by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var doseStartedOn by remember { mutableStateOf("") }

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

        SectionTitle("What are you tracking?")
        ConditionCard(
            title = "Hypothyroidism",
            subtitle = "Track fatigue, cold intolerance, brain fog, bowel changes and other common symptoms.",
            selected = condition == ThyroidCondition.HYPOTHYROIDISM,
            onClick = { condition = ThyroidCondition.HYPOTHYROIDISM }
        )
        ConditionCard(
            title = "Hyperthyroidism",
            subtitle = "Track heat intolerance, palpitations, tremor, sleep changes and other common symptoms.",
            selected = condition == ThyroidCondition.HYPERTHYROIDISM,
            onClick = { condition = ThyroidCondition.HYPERTHYROIDISM }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Medication · optional")
                OutlinedTextField(
                    value = medication,
                    onValueChange = { medication = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Medication name") },
                    placeholder = { Text("e.g. Levothyroxine") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Current dose") },
                    placeholder = { Text("e.g. 100 mcg") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Usual time") },
                    placeholder = { Text("e.g. 7:00 AM") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = doseStartedOn,
                    onValueChange = { doseStartedOn = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Current dose started") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true
                )
            }
        }

        SafetyCard()

        Button(
            enabled = condition != null,
            onClick = {
                condition?.let {
                    onFinish(
                        UserProfile(
                            condition = it,
                            medicationName = medication.trim(),
                            medicationDose = dose.trim(),
                            medicationTime = time.trim(),
                            doseStartedOn = doseStartedOn.trim()
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
    onSaveMedicationChange: (MedicationChange) -> Unit,
    onSaveLabResult: (LabResult) -> Unit
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
                    entries = appState.entries,
                    onSave = { entry ->
                        onSaveEntry(entry) {
                            scope.launch { snackbar.showSnackbar("Today's check-in saved") }
                        }
                    }
                )
                MainTab.HISTORY -> HistoryScreen(appState)
                MainTab.MEDICATION -> MedicationScreen(
                    appState = appState,
                    onSaveProfile = onSaveProfile,
                    onSaveReminderSettings = onSaveReminderSettings,
                    onSaveChange = onSaveMedicationChange,
                    onSaved = { message -> scope.launch { snackbar.showSnackbar(message) } }
                )
                MainTab.TRENDS -> TrendsAndLabsScreen(
                    appState = appState,
                    onSaveLab = onSaveLabResult,
                    onSaved = { scope.launch { snackbar.showSnackbar("Lab result saved") } }
                )
                MainTab.REPORT -> ReportScreen(appState)
            }
        }
    }
}
