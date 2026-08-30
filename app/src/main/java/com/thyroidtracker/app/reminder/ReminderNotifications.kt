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
                description = "Daily thyroid medication reminders and follow-up medication-log reminders"
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
        NotificationManagerCompat.from(context).notify(
            PRIMARY_NOTIFICATION_ID,
            NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time for your thyroid medication")
                .setContentText("Open Thyroid Echo when you're ready to update today's medication log.")
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "Open Thyroid Echo when you're ready to update today's medication log."
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(openAppIntent(context))
                .setAutoCancel(true)
                .build()
        )
    }

    fun showFollowUp(context: Context) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)
        NotificationManagerCompat.from(context).notify(
            FOLLOW_UP_NOTIFICATION_ID,
            NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("No medication log yet")
                .setContentText("You haven't logged today's medication yet. Open Thyroid Echo to update it.")
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "You haven't logged today's medication yet. Open Thyroid Echo to mark it Taken, Late, or Missed."
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(openAppIntent(context))
                .setAutoCancel(true)
                .build()
        )
    }

    fun showDailyCheckIn(context: Context) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)
        NotificationManagerCompat.from(context).notify(
            DAILY_CHECK_IN_NOTIFICATION_ID,
            NotificationCompat.Builder(context, DAILY_CHECK_IN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Today's check-in is still waiting")
                .setContentText("No Thyroid Echo entry has been saved today. Open the app when you're ready to check in.")
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "No Thyroid Echo entry has been saved today. Open the app when you're ready to complete today's check-in."
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(openAppIntent(context))
                .setAutoCancel(true)
                .build()
        )
    }

    fun clearMedicationNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancel(PRIMARY_NOTIFICATION_ID)
        NotificationManagerCompat.from(context).cancel(FOLLOW_UP_NOTIFICATION_ID)
    }

    fun clearDailyCheckInNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(DAILY_CHECK_IN_NOTIFICATION_ID)
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
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
