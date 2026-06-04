package com.example.smartcrm.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartcrm.MainActivity
import com.example.smartcrm.R

object NotificationHelper {
    const val CHANNEL_ID = "smart_crm_notifications"
    const val CHANNEL_NAME = "Smart CRM Reminders"

    // Stałe ID dla różnych typów powiadomień, aby się nie nadpisywały
    const val ID_OVERDUE = 1001
    const val ID_SOON = 1002
    const val ID_FROZEN = 1003
    const val ID_SYSTEM = 1000

    fun showNotification(context: Context, title: String, message: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Usunięto tworzenie kanału stąd, bo robimy to w MainActivity przy starcie

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Bardziej standardowa ikona
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
