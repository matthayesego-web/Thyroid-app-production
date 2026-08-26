package com.thyroidtracker.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.thyroidtracker.app.data.ReminderSettings
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ReminderScheduler {
    const val ACTION_PRIMARY = "com.thyroidtracker.app.reminder.PRIMARY"
    const val ACTION_FOLLOW_UP = "com.thyroidtracker.app.reminder.FOLLOW_UP"

    private const val PRIMARY_REQUEST_CODE = 4101
    private const val FOLLOW_UP_REQUEST_CODE = 4102
    private val storageTimeFormat = DateTimeFormatter.ofPattern("HH:mm")

    fun scheduleAll(context: Context, settings: ReminderSettings) {
        cancelAll(context)
        if (!settings.enabled) return
        val time = parseStorageTime(settings.reminderTime) ?: return
        schedulePrimary(context, settings, time)
        if (settings.followUpEnabled) scheduleFollowUp(context, settings, time)
    }

    fun schedulePrimary(context: Context, settings: ReminderSettings) {
        val time = parseStorageTime(settings.reminderTime) ?: return
        schedulePrimary(context, settings, time)
    }

    fun scheduleFollowUp(context: Context, settings: ReminderSettings) {
        val time = parseStorageTime(settings.reminderTime) ?: return
        scheduleFollowUp(context, settings, time)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, ACTION_PRIMARY, PRIMARY_REQUEST_CODE))
        alarmManager.cancel(pendingIntent(context, ACTION_FOLLOW_UP, FOLLOW_UP_REQUEST_CODE))
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    private fun schedulePrimary(context: Context, settings: ReminderSettings, time: LocalTime) {
        if (!settings.enabled) return
        val triggerAt = nextTriggerMillis(time, 0)
        setAlarm(
            context = context,
            triggerAtMillis = triggerAt,
            operation = pendingIntent(context, ACTION_PRIMARY, PRIMARY_REQUEST_CODE)
        )
    }

    private fun scheduleFollowUp(context: Context, settings: ReminderSettings, time: LocalTime) {
        if (!settings.enabled || !settings.followUpEnabled) return
        val triggerAt = nextTriggerMillis(time, settings.followUpDelayMinutes)
        setAlarm(
            context = context,
            triggerAtMillis = triggerAt,
            operation = pendingIntent(context, ACTION_FOLLOW_UP, FOLLOW_UP_REQUEST_CODE)
        )
    }

    private fun setAlarm(context: Context, triggerAtMillis: Long, operation: PendingIntent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (canScheduleExact(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    private fun pendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseStorageTime(raw: String): LocalTime? = runCatching {
        LocalTime.parse(raw, storageTimeFormat)
    }.getOrNull()

    private fun nextTriggerMillis(time: LocalTime, offsetMinutes: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        var candidate = now.toLocalDate().atTime(time).atZone(zone).plusMinutes(offsetMinutes.toLong())
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
        return candidate.toInstant().toEpochMilli()
    }
}
