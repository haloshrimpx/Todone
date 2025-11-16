package com.sijayyx.todone.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationUtils {
    const val ALARM_CHANNEL_ID: String = "alarm_channel"
    const val ALARM_CHANNEL_NAME: String = "Reminder Alarm"

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(ALARM_CHANNEL_ID, ALARM_CHANNEL_NAME, importance).apply {
                description = "Test Notification"
            }

        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 时间戳是否有效？
     *
     * @param timestamp
     * @return
     */
    fun checkValidNotificationTimestamp(timestamp: Long): Boolean {
        return timestamp > 0
    }
}