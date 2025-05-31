package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.FCMNotification
import brawijaya.example.purisaehomestay.data.model.FCMRequest
import brawijaya.example.purisaehomestay.data.model.NotificationData
import brawijaya.example.purisaehomestay.service.FCMService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class NotificationManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: NotificationManager? = null

        // Your Firebase Server Key (get from Firebase Console > Project Settings > Cloud Messaging)
        private const val SERVER_KEY = "YOUR_FIREBASE_SERVER_KEY_HERE"
        private const val FCM_URL = "https://fcm.googleapis.com/"

        fun getInstance(): NotificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationManager().also { INSTANCE = it }
            }
        }
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(FCM_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    private val fcmService = retrofit.create(FCMService::class.java)

    suspend fun sendNotificationToUser(
        userToken: String,
        notification: NotificationData
    ): Boolean {
        return try {
            val request = FCMRequest(
                to = userToken,
                notification = FCMNotification(
                    title = notification.title,
                    body = notification.message
                ),
                data = notification.extraData + mapOf("type" to notification.type.name)
            )

            val response = fcmService.sendNotification(
                authorization = "key=$SERVER_KEY",
                notification = request
            )

            response.isSuccessful && (response.body()?.success ?: 0) > 0
        } catch (e: Exception) {
            Log.e("NotificationManager", "Error sending notification", e)
            false
        }
    }

    // Send notification to multiple users
    suspend fun sendNotificationToUsers(
        userTokens: List<String>,
        notification: NotificationData
    ): Boolean {
        return try {
            val request = FCMRequest(
                registration_ids = userTokens,
                notification = FCMNotification(
                    title = notification.title,
                    body = notification.message
                ),
                data = notification.extraData + mapOf("type" to notification.type.name)
            )

            val response = fcmService.sendNotification(
                authorization = "key=$SERVER_KEY",
                notification = request
            )

            response.isSuccessful && (response.body()?.success ?: 0) > 0
        } catch (e: Exception) {
            Log.e("NotificationManager", "Error sending notifications", e)
            false
        }
    }

    suspend fun sendNotificationToAllUsers(notification: NotificationData): Boolean {
        val db = FirebaseFirestore.getInstance()
        return try {
            val usersSnapshot = db.collection("users")
                .whereNotEqualTo("fcmToken", null)
                .get()
                .await()

            val tokens = usersSnapshot.documents.mapNotNull { doc ->
                doc.getString("fcmToken")
            }

            if (tokens.isNotEmpty()) {
                sendNotificationToUsers(tokens, notification)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("NotificationManager", "Error getting user tokens", e)
            false
        }
    }

    // Send notification to admins only
    suspend fun sendNotificationToAdmins(notification: NotificationData): Boolean {
        val db = FirebaseFirestore.getInstance()
        return try {
            val adminSnapshot = db.collection("users")
                .whereEqualTo("role", "Admin")
                .whereNotEqualTo("fcmToken", null)
                .get()
                .await()

            val adminTokens = adminSnapshot.documents.mapNotNull { doc ->
                doc.getString("fcmToken")
            }

            if (adminTokens.isNotEmpty()) {
                sendNotificationToUsers(adminTokens, notification)
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("NotificationManager", "Error getting admin tokens", e)
            false
        }
    }
}