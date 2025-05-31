package brawijaya.example.purisaehomestay.service

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import brawijaya.example.purisaehomestay.MainActivity
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.FCMRequest
import brawijaya.example.purisaehomestay.data.model.FCMResponse
import brawijaya.example.purisaehomestay.data.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlin.jvm.java

interface FCMService {
    @POST("fcm/send")
    suspend fun sendNotification(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body notification: FCMRequest
    ): Response<FCMResponse>
}

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // Save token to SharedPreferences and Firebase
        saveTokenToPreferences(token)
        updateTokenInFirebase(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Purisae Homestay"
            val body = notification.body ?: ""

            // Extract notification type from data
            val notificationType = remoteMessage.data["type"]?.let {
                NotificationType.valueOf(it)
            } ?: NotificationType.NEWS

            showNotification(title, body, notificationType, remoteMessage.data)
        }

        // Handle data payload (when app is in foreground)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    private fun saveTokenToPreferences(token: String) {
        val sharedPref = getSharedPreferences("fcm_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("fcm_token", token)
            apply()
        }
    }

    private fun updateTokenInFirebase(token: String) {
        // Update token in Firebase Firestore for current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error updating token", e)
                }
        }
    }

    @RequiresPermission(POST_NOTIFICATIONS)
    private fun showNotification(
        title: String,
        body: String,
        type: NotificationType,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extras based on notification type
            when (type) {
                NotificationType.BOOKING_CREATED -> {
                    putExtra("navigate_to", "orders")
                    putExtra("order_id", data["order_id"])
                }
                NotificationType.PROMO -> {
                    putExtra("navigate_to", "promos")
                    putExtra("promo_id", data["promo_id"])
                }
                NotificationType.NEWS -> {
                    putExtra("navigate_to", "news")
                    putExtra("news_id", data["news_id"])
                }
                NotificationType.PAYMENT_CONFIRMED,
                NotificationType.PAYMENT_REJECTED -> {
                    putExtra("navigate_to", "order_detail")
                    putExtra("order_id", data["order_id"])
                }
                NotificationType.PAYMENT_RECEIVED -> {
                    putExtra("navigate_to", "admin_orders")
                    putExtra("order_id", data["order_id"])
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "purisae_homestay_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_under_text)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Purisae Homestay Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for booking updates, promos, and news"
                enableLights(true)
                lightColor = Color.BLACK
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle data-only messages (when app is running)
        // You can broadcast this data to update UI in real-time
        val intent = Intent("com.purisae.FCM_MESSAGE_RECEIVED")
        data.forEach { (key, value) ->
            intent.putExtra(key, value)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}