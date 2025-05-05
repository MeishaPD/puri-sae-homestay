package brawijaya.example.purisaehomestay.ui.screens.home.components.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import brawijaya.example.purisaehomestay.data.model.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID_NEWS = "news_channel"
        private const val CHANNEL_ID_PROMO = "promo_channel"
        private const val CHANNEL_NAME_NEWS = "Berita"
        private const val CHANNEL_NAME_PROMO = "Promo"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "From: ${remoteMessage.data}")

            val title = remoteMessage.data["title"] ?: "Notifikasi Baru"
            val message = remoteMessage.data["message"] ?: ""
            val type = remoteMessage.data["type"] ?: NotificationType.NEWS.name
            val referenceId = remoteMessage.data["referenceId"] ?: ""

            showNotification(
                title = title,
                message = message,
                type = type,
                referenceId = referenceId
            )
        }

        remoteMessage.notification?.let {
            val title = it.title ?: "Notifikasi Baru"
            val message = it.body ?: ""
            val type = remoteMessage.data["type"] ?: NotificationType.NEWS.name
            val referenceId = remoteMessage.data["referenceId"] ?: ""

            showNotification(
                title = title,
                message = message,
                type = type,
                referenceId = referenceId
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save the token to Firestore for later use in sending targeted notifications
        // In a real app, you would update the user's document with this token
    }

    private fun showNotification(
        title: String,
        message: String,
        type: String,
        referenceId: String
    ) {
        val intent = Intent("brawijaya.example.purisaehomestay.OPEN_NOTIFICATION").apply {
            putExtra("type", type)
            putExtra("referenceId", referenceId)
            putExtra("title", title)
            putExtra("message", message)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            pendingIntentFlags
        )

        // Determine channel based on notification type
        val channelId = if (type == NotificationType.PROMO.name) CHANNEL_ID_PROMO else CHANNEL_ID_NEWS

        // Build notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channels for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // News channel
            val newsChannel = NotificationChannel(
                CHANNEL_ID_NEWS,
                CHANNEL_NAME_NEWS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for homestay news notifications"
                enableLights(true)
                enableVibration(true)
            }

            // Promo channel
            val promoChannel = NotificationChannel(
                CHANNEL_ID_PROMO,
                CHANNEL_NAME_PROMO,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for homestay promo notifications"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(newsChannel)
            notificationManager.createNotificationChannel(promoChannel)
        }

        // Show notification
        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}