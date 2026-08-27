package com.thyroidtracker.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.LabResult

private enum class InsightView { TRENDS_LABS, DOCTOR_REPORT }

@Composable
internal fun InsightsScreen(
    appState: AppState,
    onSaveLab: (LabResult) -> Unit,
    onSaved: () -> Unit
) {
    var view by remember { mutableStateOf(InsightView.TRENDS_LABS) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = view == InsightView.TRENDS_LABS,
                onClick = { view = InsightView.TRENDS_LABS },
                label = { Text("Trends & labs") }
            )
            FilterChip(
                selected = view == InsightView.DOCTOR_REPORT,
                onClick = { view = InsightView.DOCTOR_REPORT },
                label = { Text("Doctor report") }
            )
        }

        Box(Modifier.weight(1f)) {
            when (view) {
                InsightView.TRENDS_LABS -> TrendsAndLabsScreen(appState, onSaveLab, onSaved)
                InsightView.DOCTOR_REPORT -> ReportScreen(appState)
            }
        }
    }
}
