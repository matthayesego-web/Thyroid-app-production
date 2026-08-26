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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.DailyEntry
import com.thyroidtracker.app.data.LabResult
import com.thyroidtracker.app.data.MedicationChange
import com.thyroidtracker.app.data.ThyroidCondition
import com.thyroidtracker.app.data.ThyroidRepository
import com.thyroidtracker.app.data.UserProfile
import kotlinx.coroutines.launch

private enum class MainTab(val label: String, val symbol: String) {
    TODAY("Today", "●"),
    HISTORY("History", "◷"),
    MEDICATION("Medication", "+"),
    TRENDS("Trends", "↗"),
    REPORT("Report", "▤")
}

@Composable
fun ThyroidTrackerApp() {
    val context = LocalContext.current
    val repository = remember { ThyroidRepository(context.applicationContext) }
    val appState by repository.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            !appState.isLoaded -> LoadingScreen()
            appState.profile == null -> OnboardingScreen(
                onFinish = { scope.launch { repository.saveProfile(it) } }
            )
            else -> MainShell(
                appState = appState,
                onSaveEntry = { scope.launch { repository.saveEntry(it) } },
                onSaveProfile = { scope.launch { repository.saveProfile(it) } },
                onSaveMedicationChange = { scope.launch { repository.saveMedicationChange(it) } },
                onSaveLabResult = { scope.launch { repository.saveLabResult(it) } }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Thyroid Echo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
            .padding(horizontal = 24.dp, vertical = 42.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Thyroid Echo", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "A private symptom, medication and lab journal designed to make patterns easier to explain at appointments.",
            style = MaterialTheme.typography.bodyLarge
        )

        SectionTitle("1. What are you tracking?")
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

        SectionTitle("2. Medication (optional)")
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
            label = { Text("Current dose started (optional)") },
            placeholder = { Text("YYYY-MM-DD") },
            singleLine = true
        )

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
    val colors = if (selected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else CardDefaults.cardColors()
    Card(onClick = onClick, colors = colors, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MainShell(
    appState: AppState,
    onSaveEntry: (DailyEntry) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveMedicationChange: (MedicationChange) -> Unit,
    onSaveLabResult: (LabResult) -> Unit
) {
    var tab by remember { mutableStateOf(MainTab.TODAY) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Text(item.symbol, fontWeight = FontWeight.Bold) },
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
                    onSave = {
                        onSaveEntry(it)
                        scope.launch { snackbar.showSnackbar("Today's check-in saved") }
                    }
                )
                MainTab.HISTORY -> HistoryScreen(appState)
                MainTab.MEDICATION -> MedicationScreen(
                    appState = appState,
                    onSaveProfile = onSaveProfile,
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
