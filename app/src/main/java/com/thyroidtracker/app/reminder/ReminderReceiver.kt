package com.thyroidtracker.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
                val state = ThyroidRepository(appContext).snapshot()
                val settings = state.reminderSettings
                when (intent.action) {
                    ReminderScheduler.ACTION_PRIMARY -> {
                        if (settings.enabled) {
                            ReminderNotifications.showPrimary(appContext)
                            ReminderScheduler.schedulePrimary(appContext, settings)
                        }
                    }

                    ReminderScheduler.ACTION_FOLLOW_UP -> {
                        if (settings.enabled && settings.followUpEnabled) {
                            val today = LocalDate.now().toString()
                            val medicationStatus = state.entries
                                .firstOrNull { it.date == today }
                                ?.medicationStatus
                            val hasMedicationLog = medicationStatus != null &&
                                medicationStatus != MedicationStatus.NOT_LOGGED

                            if (!hasMedicationLog) {
                                ReminderNotifications.showFollowUp(appContext)
                            }
                            ReminderScheduler.scheduleFollowUp(appContext, settings)
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
                ReminderScheduler.scheduleAll(appContext, state.reminderSettings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
