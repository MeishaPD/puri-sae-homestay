package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.data.model.NotificationData
import brawijaya.example.purisaehomestay.data.model.NotificationType
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.data.model.UserData
import brawijaya.example.purisaehomestay.data.model.UserNotification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    val currentUser: FirebaseUser?
    val isUserLoggedIn: Boolean

    suspend fun signUp(email: String, password: String, name: String, phoneNumber: String? = null): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun resetPassword(email: String): AuthResult
    fun signOut()
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signUp(email: String, password: String, name: String, phoneNumber: String?): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            val photoUri = "https://firebasestorage.googleapis.com/v0/b/fitly-test-app.appspot.com/o/avatars%2F7.png?alt=media&token=7f31d3b4-55ec-46d5-b463-17e4c2da6929".toUri()

            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(photoUri)
                    .build()
                user.updateProfile(profileUpdates).await()



                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to (phoneNumber ?: ""),
                    "role" to "Penyewa",
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()

                AuthResult.Success
            } else {
                AuthResult.Error("Pendaftaran gagal, silakan coba lagi")
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("email address is already in use") == true -> {
                    AuthResult.Error("Email sudah terdaftar")
                }
                e.message?.contains("password is invalid") == true -> {
                    AuthResult.Error("Password minimal 6 karakter")
                }
                else -> {
                    AuthResult.Error("Pendaftaran gagal: ${e.message}")
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record") == true -> {
                    AuthResult.Error("Email tidak terdaftar")
                }
                e.message?.contains("password is invalid") == true -> {
                    AuthResult.Error("Password salah")
                }
                else -> {
                    AuthResult.Error("Login gagal: ${e.message}")
                }
            }
        }
    }

    override suspend fun resetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record") == true -> {
                    AuthResult.Error("Email tidak terdaftar")
                }
                else -> {
                    AuthResult.Error("Gagal mengirim email reset password")
                }
            }
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    suspend fun getCurrentUserData(): UserData? {
        val firebaseUser = auth.currentUser ?: return null

        try {
            val name = firebaseUser.displayName ?: ""
            val email = firebaseUser.email ?: ""
            val photoUrl = firebaseUser.photoUrl?.toString()

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val role = userDoc.getString("role") ?: "Penyewa"
            val phoneNumber = userDoc.getString("phoneNumber") ?: ""

            val userData = UserData(
                id = firebaseUser.uid,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                role = role,
                photoUrl = photoUrl
            )

            _userData.value = userData
            return userData
        } catch (e: Exception) {
            return UserData(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = "Penyewa"
            )
            Log.w(e.toString(), "Error getting current user data:")
        }
    }

    fun refreshUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _userData.value = null
        }
    }

    fun isUserAdmin(): Boolean {
        return _userData.value?.role == "Admin"
    }
}

class DataRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    companion object {
        private const val TAG = "DataRepository"
    }

    // Collection references
    private val newsRef = db.collection("news")
    private val promoRef = db.collection("promo")
    private val usersRef = db.collection("users")
    private val notificationsRef = db.collection("notifications")
    private val userNotificationsRef = db.collection("userNotifications")

    // Get all news
//    suspend fun getAllNews(): Result<List<NewsData>> {
//        return try {
//            val snapshot = newsRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
//            val newsDataList = snapshot.documents.map { doc ->
//                NewsData(
//                    id = doc.id,
//                    description = doc.getString("description") ?: "",
//                    createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
//                    updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now(),
//                    isRead = doc.getBoolean("isRead") ?: false
//                )
//            }
//            Result.Success(newsDataList)
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to get news", e)
//            Result.Error("Failed to get news: ${e.message}")
//        }
//    }

    // Get all active promos
    suspend fun getActivePromos(): Result<List<PromoData>> {
        return try {
            val currentTime = Timestamp.now()
            val snapshot = promoRef
                .whereEqualTo("promoStatus", true)
                .whereLessThanOrEqualTo("startDate", currentTime)
                .whereGreaterThanOrEqualTo("endsDate", currentTime)
                .orderBy("startDate", Query.Direction.ASCENDING)
                .orderBy("endsDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val promoDataList = snapshot.documents.map { doc ->
                val applicablePackages = doc.get("applicablePackageIds") as? List<Map<String, Any>> ?: emptyList()

                PromoData(
                    id = doc.id,
                    applicablePackageIds = applicablePackages,
                    isActive = doc.getBoolean("promoStatus") ?: false,
                    startDate = doc.getTimestamp("startDate") ?: Timestamp.now(),
                    endDate = doc.getTimestamp("endsDate") ?: Timestamp.now(),
                    description = doc.getString("description") ?: "",
                    createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    discountAmount = doc.getDouble("discountAmount") ?: 0.0,
                    discountType = doc.getString("discountType") ?: "percentage",
                    minBookings = doc.getLong("minBookings")?.toInt() ?: 1,
                    updatedAt = doc.getTimestamp("updatedAt") ?: Timestamp.now(),
                    isRead = doc.getBoolean("isRead") ?: false
                )
            }
            Result.Success(promoDataList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get active promos", e)
            Result.Error("Failed to get active promos: ${e.message}")
        }
    }

    // Get user by ID
    suspend fun getUserById(userId: String): Result<UserData> {
        return try {
            val userDoc = usersRef.document(userId).get().await()
            if (userDoc.exists()) {
                val user = UserData(
                    id = userDoc.id,
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    phoneNumber  = userDoc.getString("phoneNumber") ?: "",
                    role = userDoc.getString("role") ?: "guest",
                )
                Result.Success(user)
            } else {
                Result.Error("User not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user", e)
            Result.Error("Failed to get user: ${e.message}")
        }
    }

    // Get all users
    suspend fun getAllUsers(): Result<List<UserData>> {
        return try {
            val snapshot = usersRef.get().await()
            val userList = snapshot.documents.map { doc ->
                UserData(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    role = doc.getString("role") ?: "guest",
                )
            }
            Result.Success(userList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get users", e)
            Result.Error("Failed to get users: ${e.message}")
        }
    }

    // Create news
    suspend fun createNews(newsData: NewsData): Result<String> {
        return try {
            val newsMap = hashMapOf(
                "description" to newsData.description,
                "imageUrl" to newsData.imageUrl,
                "createdAt" to newsData.createdAt,
                "updatedAt" to (newsData.updatedAt ?: Timestamp.now()),
                "isRead" to newsData.isRead
            )

            val documentRef = newsRef.add(newsMap).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create news", e)
            Result.Error("Failed to create news: ${e.message}")
        }
    }

    // Create promo
    suspend fun createPromo(promoData: PromoData): Result<String> {
        return try {
            val promoMap = hashMapOf(
                "applicablePackageIds" to promoData.applicablePackageIds,
                "promoStatus" to promoData.isActive,
                "startDate" to promoData.startDate,
                "endsDate" to promoData.endDate,
                "description" to promoData.description,
                "createdAt" to (promoData.createdAt ?: Timestamp.now()),
                "discountAmount" to promoData.discountAmount,
                "discountType" to promoData.discountType,
                "minBookings" to promoData.minBookings,
                "updatedAt" to (promoData.updatedAt ?: Timestamp.now()),
                "isRead" to promoData.isRead
            )

            val documentRef = promoRef.add(promoMap).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create promo", e)
            Result.Error("Failed to create promo: ${e.message}")
        }
    }

    // Mark news as read
    suspend fun markNewsAsRead(newsId: String): Result<Boolean> {
        return try {
            newsRef.document(newsId).update("is_read", true).await()
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark news as read", e)
            Result.Error("Failed to mark news as read: ${e.message}")
        }
    }

    // Mark promo as read
    suspend fun markPromoAsRead(promoId: String): Result<Boolean> {
        return try {
            promoRef.document(promoId).update("is_read", true).await()
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark promo as read", e)
            Result.Error("Failed to mark promo as read: ${e.message}")
        }
    }

    // ---------- Notification Methods ----------

    /**
     * Get all notifications
     */
    suspend fun getAllNotifications(): Result<List<NotificationData>> {
        return try {
            val snapshot = notificationsRef
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val notificationList = snapshot.documents.map { doc ->
                NotificationData(
                    id = doc.id,
                    type = NotificationType.valueOf(doc.getString("type") ?: NotificationType.NEWS.name),
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    referenceId = doc.getString("referenceId") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true
                )
            }
            Result.Success(notificationList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all notifications", e)
            Result.Error("Failed to get all notifications: ${e.message}")
        }
    }

    /**
     * Get notification by ID
     */
    suspend fun getNotificationById(notificationId: String): Result<NotificationData> {
        return try {
            val doc = notificationsRef.document(notificationId).get().await()
            if (doc.exists()) {
                val notification = NotificationData(
                    id = doc.id,
                    type = NotificationType.valueOf(doc.getString("type") ?: NotificationType.NEWS.name),
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                    referenceId = doc.getString("referenceId") ?: "",
                    isActive = doc.getBoolean("isActive") ?: true
                )
                Result.Success(notification)
            } else {
                Result.Error("Notification not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get notification by ID", e)
            Result.Error("Failed to get notification: ${e.message}")
        }
    }

    /**
     * Get all notifications for a specific user
     */
    suspend fun getUserNotifications(userId: String): Result<List<Pair<NotificationData, UserNotification>>> {
        return try {
            val userNotificationsQuery = userNotificationsRef
                .whereEqualTo("userId", userId)
                .orderBy("isRead")
                .orderBy("notificationId", Query.Direction.DESCENDING)
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
                val notificationDoc = notificationsRef.document(userNotification.notificationId).get().await()
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

            Result.Success(notificationDataPairs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user notifications", e)
            Result.Error("Failed to get user notifications: ${e.message}")
        }
    }

    /**
     * Create a new notification
     */
    suspend fun createNotification(notificationData: NotificationData): Result<String> {
        return try {
            val notificationMap = hashMapOf(
                "type" to notificationData.type.name,
                "title" to notificationData.title,
                "description" to notificationData.description,
                "imageUrl" to notificationData.imageUrl,
                "createdAt" to (notificationData.createdAt ?: Timestamp.now()),
                "referenceId" to notificationData.referenceId,
                "isActive" to notificationData.isActive
            )

            val documentRef = notificationsRef.add(notificationMap).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification", e)
            Result.Error("Failed to create notification: ${e.message}")
        }
    }

    /**
     * Send notification to a specific user
     */
    suspend fun sendNotificationToUser(userId: String, notificationId: String): Result<String> {
        return try {
            val userNotificationMap = hashMapOf(
                "userId" to userId,
                "notificationId" to notificationId,
                "isRead" to false,
                "readAt" to null,
                "createdAt" to Timestamp.now()
            )

            val documentRef = userNotificationsRef.add(userNotificationMap).await()
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
            for (userId in userIds) {
                val userNotificationMap = hashMapOf(
                    "userId" to userId,
                    "notificationId" to notificationId,
                    "isRead" to false,
                    "readAt" to null,
                    "createdAt" to Timestamp.now()
                )
                userNotificationsRef.add(userNotificationMap).await()
            }
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification to all users", e)
            Result.Error("Failed to send notification to all users: ${e.message}")
        }
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(userNotificationId: String): Result<Boolean> {
        return try {
            userNotificationsRef.document(userNotificationId).update(
                mapOf(
                    "isRead" to true,
                    "readAt" to Timestamp.now()
                )
            ).await()
            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification as read", e)
            Result.Error("Failed to mark notification as read: ${e.message}")
        }
    }

    /**
     * Get unread notifications count
     */
    suspend fun getUnreadNotificationsCount(userId: String): Result<Int> {
        return try {
            val query = userNotificationsRef
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
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Boolean> {
        return try {
            notificationsRef.document(notificationId).delete().await()

            // Delete all user notifications for this notification
            val userNotificationsQuery = userNotificationsRef
                .whereEqualTo("notificationId", notificationId)
                .get()
                .await()

            for (doc in userNotificationsQuery.documents) {
                userNotificationsRef.document(doc.id).delete().await()
            }

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete notification", e)
            Result.Error("Failed to delete notification: ${e.message}")
        }
    }

    /**
     * Create a promo notification and send to all users
     */
    suspend fun createPromoNotificationAndSendToAll(
        promoId: String,
        title: String,
        description: String,
        imageUrl: String? = null
    ): Result<Boolean> {
        return try {
            // Create notification
            val notificationData = NotificationData(
                type = NotificationType.PROMO,
                title = title,
                description = description,
                imageUrl = imageUrl,
                createdAt = Timestamp.now(),
                referenceId = promoId,
                isActive = true
            )

            val notificationResult = createNotification(notificationData)
            if (notificationResult is Result.Error) {
                return notificationResult
            }

            val notificationId = (notificationResult as Result.Success).data

            // Get all users
            val usersResult = getAllUsers()
            if (usersResult is Result.Error) {
                return Result.Error(usersResult.message)
            }

            val userIds = (usersResult as Result.Success).data.map { it.id }

            // Send to all users
            val sendResult = sendNotificationToAllUsers(userIds, notificationId)
            if (sendResult is Result.Error) {
                return sendResult
            }

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create and send promo notification", e)
            Result.Error("Failed to create and send promo notification: ${e.message}")
        }
    }

    /**
     * Create a news notification and send to all users
     */
    suspend fun createNewsNotificationAndSendToAll(
        newsId: String,
        title: String,
        description: String,
        imageUrl: String? = null
    ): Result<Boolean> {
        return try {
            // Create notification
            val notificationData = NotificationData(
                type = NotificationType.NEWS,
                title = title,
                description = description,
                imageUrl = imageUrl,
                createdAt = Timestamp.now(),
                referenceId = newsId,
                isActive = true
            )

            val notificationResult = createNotification(notificationData)
            if (notificationResult is Result.Error) {
                return notificationResult
            }

            val notificationId = (notificationResult as Result.Success).data

            // Get all users
            val usersResult = getAllUsers()
            if (usersResult is Result.Error) {
                return Result.Error(usersResult.message)
            }

            val userIds = (usersResult as Result.Success).data.map { it.id }

            // Send to all users
            val sendResult = sendNotificationToAllUsers(userIds, notificationId)
            if (sendResult is Result.Error) {
                return sendResult
            }

            Result.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create and send news notification", e)
            Result.Error("Failed to create and send news notification: ${e.message}")
        }
    }
}

/**
 * Repository untuk mengelola paket-paket penginapan
 * Sementara menggunakan local variable sebagai sumber data
 */
@Singleton
class PackageRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    private val _packages = MutableStateFlow<List<Paket>>(getInitialPaketList())
    val packages: Flow<List<Paket>> = _packages.asStateFlow()

    /**
     * Mendapatkan daftar paket awal
     */
    private fun getInitialPaketList(): List<Paket> {
        return listOf(
            Paket(
                id = 1,
                title = "Sewa Bungalow",
                features = listOf(
                    "2 Lantai",
                    "Kapasitas 4-6 Orang",
                    "Wifi, AC dan Air Panas",
                    "Kolam Renang"
                ),
                weekdayPrice = 500000.0,
                weekendPrice = 550000.0,
                imageUrl = R.drawable.bungalow_single
            ),
            Paket(
                id = 2,
                title = "Paket Rombongan (sampai 20 orang)",
                features = listOf(
                    "3 Bungalow",
                    "Free 3 Ekstra Bed",
                    "Dapur, Wifi, AC dan Air Panas",
                    "Kolam Renang",
                    "Joglo (Karaoke)"
                ),
                weekdayPrice = 2000000.0,
                weekendPrice = 2150000.0,
                imageUrl = R.drawable.bungalow_group
            ),
            Paket(
                id = 3,
                title = "Paket Venue Wedding",
                features = listOf(
                    "Bungalow",
                    "Joglo Utama",
                    "Dapur",
                    "Area Makan",
                    "Kolam Renang"
                ),
                weekdayPrice = 7000000.0,
                weekendPrice = 7500000.0,
                imageUrl = R.drawable.wedding_venue
            )
        )
    }

    /**
     * Membuat paket baru
     */
    fun createPackage(paket: Paket) {
        val currentList = _packages.value.toMutableList()

        if (currentList.any { it.id == paket.id }) {
            return
        }

        currentList.add(paket)
        _packages.value = currentList
    }

    /**
     * Mengambil paket berdasarkan ID
     */
    fun getPackageById(id: Int): Paket? {
        return _packages.value.find { it.id == id }
    }

    /**
     * Mengambil semua paket
     */
    fun getAllPackages(): List<Paket> {
        return _packages.value
    }

    /**
     * Mengubah data paket
     */
    fun updatePackage(paket: Paket) {
        val currentList = _packages.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == paket.id }

        if (index != -1) {
            currentList[index] = paket
            _packages.value = currentList
        }
    }

    /**
     * Menghapus paket berdasarkan ID
     */
    fun deletePackage(id: Int) {
        val currentList = _packages.value.toMutableList()
        currentList.removeIf { it.id == id }
        _packages.value = currentList
    }
}