package it.simosw.enginegallery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EngineGalleryMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        if (notificationsEnabled()) AndroidPushRegistration.register(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!notificationsEnabled()) return
        val engineRef = message.data["engineRef"] ?: return
        if (!engineRef.matches(Regex("RML-[0-9]{4}-[0-9]{5}"))) return
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return

        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Motori consegnati", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_ENGINE_REF, engineRef)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, engineRef.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Motore consegnato")
            .setContentText("Il motore $engineRef è stato consegnato")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        NotificationManagerCompat.from(this).notify(engineRef.hashCode(), notification)
    }

    private fun notificationsEnabled(): Boolean =
        getSharedPreferences(MainActivity.NOTIFICATION_PREFERENCES, MODE_PRIVATE)
            .getBoolean(MainActivity.NOTIFICATIONS_ENABLED_KEY, true)

    companion object {
        private const val CHANNEL_ID = "delivered_engines"
    }
}
