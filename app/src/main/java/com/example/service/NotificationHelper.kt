package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "agri_robot_critical_alerts"
        const val CHANNEL_NAME = "Agri Robot Critical Alerts"
        const val FIRE_NOTIFICATION_ID = 1001
        const val WATER_NOTIFICATION_ID = 1002
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for Agricultural Robot (Fire, Low Water)"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun postFireAlert() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("CRITICAL: FIRE DETECTED!")
            .setContentText("Flame sensor triggered on AI AGRI ROBOT. Check robot immediately.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(FIRE_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Handled gracefully if POST_NOTIFICATIONS is not yet granted
        }
    }

    fun dismissFireAlert() {
        notificationManager.cancel(FIRE_NOTIFICATION_ID)
    }
}
