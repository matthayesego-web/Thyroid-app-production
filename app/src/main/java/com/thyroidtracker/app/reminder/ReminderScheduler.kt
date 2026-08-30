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
    const val ACTION_DAILY_CHECK_IN = "com.thyroidtracker.app.reminder.DAILY_CHECK_IN"

    private const val PRIMARY_REQUEST_CODE = 4101
    private const val FOLLOW_UP_REQUEST_CODE = 4102
    private const val DAILY_CHECK_IN_REQUEST_CODE = 4103
    private val storageTimeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val noon = LocalTime.NOON
    private val latestDailyReminder = LocalTime.of(23, 55)

    fun scheduleAll(context: Context, settings: ReminderSettings) {
        cancelAll(context)
        scheduleDailyCheckIn(context, settings)

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

    fun scheduleDailyCheckIn(context: Context, settings: ReminderSettings) {
        val triggerAt = nextTriggerMillis(dailyCheckInTime(settings), 0)
        setAlarm(
            context = context,
            triggerAtMillis = triggerAt,
            operation = pendingIntent(context, ACTION_DAILY_CHECK_IN, DAILY_CHECK_IN_REQUEST_CODE)
        )
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, ACTION_PRIMARY, PRIMARY_REQUEST_CODE))
        alarmManager.cancel(pendingIntent(context, ACTION_FOLLOW_UP, FOLLOW_UP_REQUEST_CODE))
        alarmManager.cancel(pendingIntent(context, ACTION_DAILY_CHECK_IN, DAILY_CHECK_IN_REQUEST_CODE))
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

    private fun dailyCheckInTime(settings: ReminderSettings): LocalTime {
        val medicationTime = if (settings.enabled) parseStorageTime(settings.reminderTime) else null
        if (medicationTime == null || !medicationTime.isAfter(noon)) return noon

        val delayed = medicationTime.plusMinutes(30)
        return when {
            delayed.isBefore(medicationTime) -> latestDailyReminder
            delayed.isAfter(latestDailyReminder) -> latestDailyReminder
            else -> delayed
        }
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
