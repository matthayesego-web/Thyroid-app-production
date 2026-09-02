package com.thyroidtracker.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thyroidtracker.app.data.AppState
import com.thyroidtracker.app.data.MedicationLog
import com.thyroidtracker.app.data.MedicationStatus
import com.thyroidtracker.app.data.ThyroidRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ThyroidRepository(appContext)
                val state = repository.snapshot()
                val settings = state.reminderSettings
                when (intent.action) {
                    ReminderScheduler.ACTION_PRIMARY -> {
                        if (settings.enabled) {
                            if (!state.hasMedicationLogToday()) {
                                ReminderNotifications.showPrimary(appContext)
                            }
                            ReminderScheduler.schedulePrimary(appContext, settings)
                        }
                    }

                    ReminderScheduler.ACTION_FOLLOW_UP -> {
                        if (settings.enabled && settings.followUpEnabled) {
                            if (!state.hasMedicationLogToday()) {
                                ReminderNotifications.showFollowUp(appContext)
                            }
                            ReminderScheduler.scheduleFollowUp(appContext, settings)
                        }
                    }

                    ReminderScheduler.ACTION_DAILY_CHECK_IN -> {
                        if (state.profile != null && !state.hasDailyEntryToday()) {
                            val medicationConfigured = state.profile.medicationName.isNotBlank()
                            ReminderNotifications.showDailyCheckIn(
                                appContext,
                                medicationUnlogged = medicationConfigured && !state.hasMedicationLogToday()
                            )
                        }
                        ReminderScheduler.scheduleDailyCheckIn(appContext, settings)
                    }

                    ReminderScheduler.ACTION_LOG_MEDICATION -> {
                        val status = runCatching {
                            MedicationStatus.valueOf(
                                intent.getStringExtra(ReminderScheduler.EXTRA_MEDICATION_STATUS).orEmpty()
                            )
                        }.getOrNull()
                        val logDate = intent.getStringExtra(ReminderScheduler.EXTRA_LOG_DATE)
                            ?.takeIf { runCatching { LocalDate.parse(it) }.isSuccess }
                            ?: LocalDate.now().toString()
                        if (status != null && status != MedicationStatus.NOT_LOGGED && state.profile != null) {
                            repository.saveMedicationLog(
                                MedicationLog(
                                    date = logDate,
                                    status = status
                                )
                            )
                            ReminderNotifications.clearMedicationNotifications(appContext)

                            val source = intent.getStringExtra(ReminderScheduler.EXTRA_ACTION_SOURCE)
                            if (
                                source == ReminderScheduler.SOURCE_DAILY_CHECK_IN &&
                                logDate == LocalDate.now().toString() &&
                                !state.hasDailyEntryToday()
                            ) {
                                // Keep the daily check-in reminder accurate after medication is logged.
                                ReminderNotifications.showDailyCheckIn(
                                    appContext,
                                    medicationUnlogged = false
                                )
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = ThyroidRepository(appContext).snapshot()
                if (state.profile != null) {
                    ReminderScheduler.scheduleAll(appContext, state.reminderSettings)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

private fun AppState.hasMedicationLogToday(): Boolean {
    val today = LocalDate.now().toString()
    return medicationLogs.any { it.date == today && it.status != MedicationStatus.NOT_LOGGED }
}

private fun AppState.hasDailyEntryToday(): Boolean {
    val today = LocalDate.now().toString()
    return entries.any { it.date == today }
}
