package com.bay.chatapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bay.chatapp.notification.MessageNotificationManager
import com.bay.chatapp.notification.NotificationHelper

class MessageService : Service() {

    override fun onCreate() {
        super.onCreate()
        try {
            val notification = NotificationHelper.buildServiceNotification(this)
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                startForeground(999, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(999, notification)
            }
            MessageNotificationManager.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
            // If we can't start foreground or GMS fails, stop self to avoid crash loop
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MessageNotificationManager.start(this)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageNotificationManager.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
