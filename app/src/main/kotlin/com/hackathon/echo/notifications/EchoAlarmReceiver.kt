package com.hackathon.echo.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hackathon.echo.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EchoAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val echoId = intent.getIntExtra("echo_id", -1)
        android.util.Log.d("EchoAlarm", "RECEIVER: Alarm received for ID: $echoId")
        if (echoId == -1) return

        val database = AppDatabase.getDatabase(context)
        val notificationManager = EchoNotificationManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            val dao = database.echoDao()
            val item = dao.getEchoById(echoId)
            android.util.Log.d("EchoAlarm", "RECEIVER: Item fetched: ${item?.title}, status: ${item?.status}")
            if (item != null && item.status == "PENDING") {
                notificationManager.showNotification(item)
                dao.insert(item.copy(status = "ECHOED"))
                android.util.Log.d("EchoAlarm", "RECEIVER: Notification sent and status updated to ECHOED")
            }
        }
    }
}
