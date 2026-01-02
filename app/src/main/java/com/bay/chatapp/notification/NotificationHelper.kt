package com.bay.chatapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bay.chatapp.R
import com.bay.chatapp.view.ChatActivity
import android.graphics.BitmapFactory

object NotificationHelper {
    const val CHANNEL_ID = "messages"
    private const val CHANNEL_NAME = "Messages"
    const val SERVICE_CHANNEL_ID = "service"
    private const val SERVICE_CHANNEL_NAME = "Background Service"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Chat message notifications"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(chan)

            val serviceChan = NotificationChannel(
                SERVICE_CHANNEL_ID,
                SERVICE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs background listeners for messages"
                setShowBadge(false)
            }
            nm.createNotificationChannel(serviceChan)
        }
    }

    fun notifyMessages(
        context: Context,
        otherUid: String,
        otherDisplayName: String,
        otherPhotoUrl: String,
        lines: List<String>,
        notificationId: Int
    ) {
        val intent = Intent(context, ChatActivity::class.java)
            .putExtra("otherUid", otherUid)
            .putExtra("otherUsername", otherDisplayName)
            .putExtra("otherPhotoUrl", otherPhotoUrl)
        val pi = androidx.core.app.TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(notificationId, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val joined = lines.take(3).joinToString(separator = "\n") { it }
        val style = NotificationCompat.BigTextStyle().bigText(joined)

        val title = if (otherDisplayName.isNotBlank()) otherDisplayName else "New messages"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setStyle(style)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun cancelForChat(context: Context, currentUid: String, otherUid: String) {
        val id = listOf(currentUid, otherUid).sorted().joinToString("_").hashCode()
        NotificationManagerCompat.from(context).cancel(id)
    }

    fun buildServiceNotification(context: Context): android.app.Notification {
        val builder = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle("Listening for messages")
            .setContentText("Deliveries and notifications are active")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
        return builder.build()
    }
}
