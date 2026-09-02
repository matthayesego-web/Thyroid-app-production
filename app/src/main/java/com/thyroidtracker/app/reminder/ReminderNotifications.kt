package com.thyroidtracker.app.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.thyroidtracker.app.MainActivity
import com.thyroidtracker.app.R
import com.thyroidtracker.app.data.MedicationStatus

object ReminderNotifications {
    private const val MEDICATION_CHANNEL_ID = "medication_reminders"
    private const val DAILY_CHECK_IN_CHANNEL_ID = "daily_checkin_reminders"
    private const val PRIMARY_NOTIFICATION_ID = 5101
    private const val FOLLOW_UP_NOTIFICATION_ID = 5102
    private const val DAILY_CHECK_IN_NOTIFICATION_ID = 5103

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                MEDICATION_CHANNEL_ID,
                "Medication reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thyroid medication reminders with private quick-log actions"
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                DAILY_CHECK_IN_CHANNEL_ID,
                "Daily check-in reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminder when no Thyroid Echo daily check-in has been saved"
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }
        )
    }

    fun showPrimary(context: Context) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)
        val builder = NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Did you take your medication?")
            .setContentText("Log it from this notification, or open Thyroid Echo when you're ready.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Did you take your medication? Mark it Taken, Late, or Missed without opening the app."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
        addMedicationActions(context, builder, ReminderScheduler.SOURCE_MEDICATION_REMINDER)
        NotificationManagerCompat.from(context).notify(PRIMARY_NOTIFICATION_ID, builder.build())
    }

    fun showFollowUp(context: Context) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)
        val builder = NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("No medication log yet")
            .setContentText("Did you take your medication? You can log it here now.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Today's medication is still not logged. Mark it Taken, Late, or Missed from this notification."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
        addMedicationActions(context, builder, ReminderScheduler.SOURCE_MEDICATION_REMINDER)
        NotificationManagerCompat.from(context).notify(FOLLOW_UP_NOTIFICATION_ID, builder.build())
    }

    fun showDailyCheckIn(context: Context, medicationUnlogged: Boolean) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)
        val text = if (medicationUnlogged) {
            "Today's check-in is waiting. Have you taken your medication?"
        } else {
            "No Thyroid Echo check-in has been saved today. Open the app when you're ready."
        }
        val bigText = if (medicationUnlogged) {
            "Today's check-in is still waiting. If you've taken your medication, you can log it from this notification now."
        } else {
            "No Thyroid Echo check-in has been saved today. Open the app when you're ready to complete today's check-in."
        }
        val builder = NotificationCompat.Builder(context, DAILY_CHECK_IN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's check-in is still waiting")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
        if (medicationUnlogged) {
            addMedicationActions(context, builder, ReminderScheduler.SOURCE_DAILY_CHECK_IN)
        }
        NotificationManagerCompat.from(context).notify(DAILY_CHECK_IN_NOTIFICATION_ID, builder.build())
    }

    fun clearMedicationNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancel(PRIMARY_NOTIFICATION_ID)
        NotificationManagerCompat.from(context).cancel(FOLLOW_UP_NOTIFICATION_ID)
    }

    fun clearDailyCheckInNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(DAILY_CHECK_IN_NOTIFICATION_ID)
    }

    fun clearAllReminderNotifications(context: Context) {
        clearMedicationNotifications(context)
        clearDailyCheckInNotification(context)
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun addMedicationActions(
        context: Context,
        builder: NotificationCompat.Builder,
        source: String
    ) {
        listOf(
            MedicationStatus.TAKEN to "Taken",
            MedicationStatus.LATE to "Late",
            MedicationStatus.MISSED to "Missed"
        ).forEach { (status, label) ->
            builder.addAction(
                R.drawable.ic_notification,
                label,
                medicationActionIntent(context, status, source)
            )
        }
    }

    private fun medicationActionIntent(
        context: Context,
        status: MedicationStatus,
        source: String
    ): PendingIntent {
        val sourceOffset = if (source == ReminderScheduler.SOURCE_DAILY_CHECK_IN) 100 else 0
        val requestCode = 5300 + sourceOffset + status.ordinal
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_LOG_MEDICATION
            putExtra(ReminderScheduler.EXTRA_MEDICATION_STATUS, status.name)
            putExtra(ReminderScheduler.EXTRA_ACTION_SOURCE, source)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            5201,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
