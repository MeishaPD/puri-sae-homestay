package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.NotificationData
import brawijaya.example.purisaehomestay.data.model.NotificationType
import brawijaya.example.purisaehomestay.data.model.UserNotification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Result wrapper class
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class NotificationManager(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "NotificationManager"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
        private const val USER_NOTIFICATIONS_COLLECTION = "userNotifications"
    }

    /**
     * Get all notifications for a specific user
     */
    suspend fun getUserNotifications(userId: String): Result<List<Pair<NotificationData, UserNotification>>> {
        return try {
            val userNotificationsQuery = db.collection(USER_NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val userNotifications = userNotificationsQuery.documents.mapNotNull { doc ->
                val userNotification = UserNotification(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    notificationId = doc.getString("notificationId") ?: "",
                    isRead = doc.getBoolean("isRead") ?: false,
                    readAt = doc.getTimestamp("readAt")
                )
                userNotification
            }

            val notificationDataPairs = mutableListOf<Pair<NotificationData, UserNotification>>()

            for (userNotification in userNotifications) {
                val notificationDoc = db.collection(NOTIFICATIONS_COLLECTION)
                    .document(userNotification.notificationId)
                    .get()
                    .await()

                if (notificationDoc.exists()) {
                    val notificationData = NotificationData(
                        id = notificationDoc.id,
                        type = NotificationType.valueOf(notificationDoc.getString("type") ?: NotificationType.NEWS.name),
                        title = notificationDoc.getString("title") ?: "",
                        description = notificationDoc.getString("description") ?: "",
                        imageUrl = notificationDoc.getString("imageUrl"),
                        createdAt = notificationDoc.getTimestamp("createdAt") ?: Timestamp.now(),
                        referenceId = notificationDoc.getString("referenceId") ?: "",
                        isActive = notificationDoc.getBoolean("isActive") ?: true
                    )
                    notificationDataPairs.add(Pair(notificationData, userNotification))
                }
            }

            // Sort by notification creation date (newest first) and unread status
            val sortedNotifications = notificationDataPairs.sortedWith(
                compareByDescending<Pair<NotificationData, UserNotification>> { !it.second.isRead }
                    .thenByDescending { it.first.createdAt.seconds }
            )

            Result.Success(sortedNotifications)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user notifications", e)
            Result.Error("Failed to get user notifications: ${e.message}")
        }
    }

    /**
     * Get count of unread notifications for a user
     */
    suspend fun getUnreadNotificationsCount(userId: String): Result<Int> {
        return try {
            val query = db.collection(USER_NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            Result.Success(query.size())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get unread notifications count", e)
            Result.Error("Failed to get unread notifications count: ${e.message}")
        }
    }

    /**
     * Mark a notification as read
     */
    suspend fun markNotificationAsRead(userNotificationId: String): Result<Boolean> {
        return try {
            db.collection(USER_NOTIFICATIONS_COLLECTION)
                .document(userNotificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "readAt" to Timestamp.now()
                    )
                )
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification as read", e)
            Result.Error("Failed to mark notification as read: ${e.message}")
        }
    }

    /**
     * Create a news notification
     */
    suspend fun createNewsNotification(
        newsId: String,
        title: String,
        description: String,
        imageUrl: String?
    ): Result<String> {
        return try {
            val notificationData = hashMapOf(
                "type" to NotificationType.NEWS.name,
                "title" to title,
                "description" to description,
                "imageUrl" to imageUrl,
                "createdAt" to Timestamp.now(),
                "referenceId" to newsId,
                "isActive" to true
            )

            val documentRef = db.collection(NOTIFICATIONS_COLLECTION)
                .add(notificationData)
                .await()

            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create news notification", e)
            Result.Error("Failed to create news notification: ${e.message}")
        }
    }

    /**
     * Create a promo notification
     */
    suspend fun createPromoNotification(
        promoId: String,
        title: String,
        description: String,
        imageUrl: String?
    ): Result<String> {
        return try {
            val notificationData = hashMapOf(
                "type" to NotificationType.PROMO.name,
                "title" to title,
                "description" to description,
                "imageUrl" to imageUrl,
                "createdAt" to Timestamp.now(),
                "referenceId" to promoId,
                "isActive" to true
            )

            val documentRef = db.collection(NOTIFICATIONS_COLLECTION)
                .add(notificationData)
                .await()

            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create promo notification", e)
            Result.Error("Failed to create promo notification: ${e.message}")
        }
    }

    /**
     * Send notification to a specific user
     */
    suspend fun sendNotificationToUser(userId: String, notificationId: String): Result<String> {
        return try {
            val userNotificationData = hashMapOf(
                "userId" to userId,
                "notificationId" to notificationId,
                "isRead" to false,
                "readAt" to null,
                "createdAt" to Timestamp.now()
            )

            val documentRef = db.collection(USER_NOTIFICATIONS_COLLECTION)
                .add(userNotificationData)
                .await()

            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification to user", e)
            Result.Error("Failed to send notification to user: ${e.message}")
        }
    }

    /**
     * Send notification to all users
     */
    suspend fun sendNotificationToAllUsers(userIds: List<String>, notificationId: String): Result<Boolean> {
        return try {
            val batch = db.batch()

            for (userId in userIds) {
                val userNotificationRef = db.collection(USER_NOTIFICATIONS_COLLECTION).document()

                val userNotificationData = hashMapOf(
                    "userId" to userId,
                    "notificationId" to notificationId,
                    "isRead" to false,
                    "readAt" to null,
                    "createdAt" to Timestamp.now()
                )

                batch.set(userNotificationRef, userNotificationData)
            }

            batch.commit().await()
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification to all users", e)
            Result.Error("Failed to send notification to all users: ${e.message}")
        }
    }

    /**
     * Delete a notification and all its user links
     */
    suspend fun deleteNotification(notificationId: String): Result<Boolean> {
        return try {
            // Delete the notification
            db.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .delete()
                .await()

            // Delete all user notification links
            val userNotificationsQuery = db.collection(USER_NOTIFICATIONS_COLLECTION)
                .whereEqualTo("notificationId", notificationId)
                .get()
                .await()

            val batch = db.batch()
            for (doc in userNotificationsQuery.documents) {
                batch.delete(db.collection(USER_NOTIFICATIONS_COLLECTION).document(doc.id))
            }
            batch.commit().await()

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete notification", e)
            Result.Error("Failed to delete notification: ${e.message}")
        }
    }
}