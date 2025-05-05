package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FCMTokenManager(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {
    companion object {
        private const val TAG = "FCMTokenManager"
        private const val DEVICE_TOKENS_COLLECTION = "deviceTokens"
    }

    /**
     * Get and save the current device FCM token for the specified user.
     * This should be called when a user logs in.
     */
    suspend fun registerUserToken(userId: String): Result<String> {
        return try {
            val token = messaging.token.await()
            saveUserToken(userId, token)
            Result.Success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            Result.Error("Failed to get FCM token: ${e.message}")
        }
    }

    /**
     * Save a user's FCM token to Firestore.
     */
    private suspend fun saveUserToken(userId: String, token: String): Result<Boolean> {
        return try {
            val tokenData = hashMapOf(
                "userId" to userId,
                "token" to token,
                "platform" to "android",
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            // Check if token already exists for this user
            val existingTokenQuery = db.collection(DEVICE_TOKENS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("token", token)
                .get()
                .await()

            if (existingTokenQuery.isEmpty) {
                // Only add if it doesn't exist
                db.collection(DEVICE_TOKENS_COLLECTION)
                    .add(tokenData)
                    .await()
            }

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user token", e)
            Result.Error("Failed to save user token: ${e.message}")
        }
    }

    /**
     * Remove a user's FCM token when they log out.
     */
    suspend fun removeUserToken(userId: String): Result<Boolean> {
        return try {
            val token = messaging.token.await()

            val tokenQuery = db.collection(DEVICE_TOKENS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("token", token)
                .get()
                .await()

            for (document in tokenQuery.documents) {
                db.collection(DEVICE_TOKENS_COLLECTION)
                    .document(document.id)
                    .delete()
                    .await()
            }

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing user token", e)
            Result.Error("Failed to remove user token: ${e.message}")
        }
    }

    /**
     * Get all FCM tokens for a specific user (they might have multiple devices)
     */
    suspend fun getUserTokens(userId: String): Result<List<String>> {
        return try {
            val tokenQuery = db.collection(DEVICE_TOKENS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val tokens = tokenQuery.documents.mapNotNull { it.getString("token") }
            Result.Success(tokens)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user tokens", e)
            Result.Error("Failed to get user tokens: ${e.message}")
        }
    }

    /**
     * Get all user FCM tokens in the system
     */
    suspend fun getAllTokens(): Result<List<String>> {
        return try {
            val tokenQuery = db.collection(DEVICE_TOKENS_COLLECTION)
                .get()
                .await()

            val tokens = tokenQuery.documents.mapNotNull { it.getString("token") }
            Result.Success(tokens)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all tokens", e)
            Result.Error("Failed to get all tokens: ${e.message}")
        }
    }
}