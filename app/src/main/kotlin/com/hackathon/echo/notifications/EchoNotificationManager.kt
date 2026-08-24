package com.hackathon.echo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hackathon.echo.MainActivity
import com.hackathon.echo.R
import com.hackathon.echo.data.EchoItem

class EchoNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "echo_reminders"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Echo Reminders"
            val descriptionText = "Notifications for your saved intentions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(echo: EchoItem) {
        Log.d("EchoNotification", "NOTIFICATION: Creating notification for ID: ${echo.id}")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("echo_id", echo.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, echo.id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Echo: ${echo.category}")
            .setContentText("${echo.intent}: ${echo.title}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${echo.intent}: ${echo.title}\n\n${echo.rawText.take(200)}..."))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Increased priority
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(echo.id, builder.build())
            Log.d("EchoNotification", "NOTIFICATION: Successfully posted notification for ID: ${echo.id}")
        } catch (e: Exception) {
            Log.e("EchoNotification", "NOTIFICATION: Failed to post notification", e)
        }
    }
}
