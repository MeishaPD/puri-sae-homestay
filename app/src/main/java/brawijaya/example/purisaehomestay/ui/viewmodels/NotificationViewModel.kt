package brawijaya.example.purisaehomestay.ui.viewmodels
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import brawijaya.example.purisaehomestay.data.model.NewsData
//import brawijaya.example.purisaehomestay.data.model.NotificationData
//import brawijaya.example.purisaehomestay.data.model.PromoData
//import brawijaya.example.purisaehomestay.data.model.UserData
//import brawijaya.example.purisaehomestay.data.model.UserNotification
//import brawijaya.example.purisaehomestay.data.repository.DataRepository
//import brawijaya.example.purisaehomestay.data.repository.FCMTokenManager
//import brawijaya.example.purisaehomestay.data.repository.NotificationManager
//import brawijaya.example.purisaehomestay.data.repository.Result
//import com.google.firebase.Timestamp
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class NotificationViewModel(
//    private val notificationManager: NotificationManager = NotificationManager(),
//    private val dataRepository: DataRepository = DataRepository(),
//    private val fcmTokenManager: FCMTokenManager = FCMTokenManager()
//) : ViewModel() {
//
//    // LiveData for notifications
//    private val _notifications = MutableLiveData<List<Pair<NotificationData, UserNotification>>>()
//    val notifications: LiveData<List<Pair<NotificationData, UserNotification>>> = _notifications
//
//    // StateFlow for selected notification (for passing between screens)
//    private val _selectedNotification =
//        MutableStateFlow<Pair<NotificationData, UserNotification>?>(null)
//    val selectedNotification: StateFlow<Pair<NotificationData, UserNotification>?> = _selectedNotification.asStateFlow()
//
//    // For tracking unread count
//    private val _unreadCount = MutableLiveData<Int>()
//    val unreadCount: LiveData<Int> = _unreadCount
//
//    // Loading and error states
//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean> = _loading
//
//    private val _error = MutableLiveData<String?>()
//    val error: LiveData<String?> = _error
//
//    /**
//     * Get notifications for a specific user
//     */
//    fun getUserNotifications(userId: String) {
//        viewModelScope.launch {
//            _loading.value = true
//            _error.value = null
//
//            when (val result = notificationManager.getUserNotifications(userId)) {
//                is Result.Success -> {
//                    // Sort notifications by creation date (newest first)
//                    val sortedNotifications = result.data.sortedByDescending {
//                        it.first.createdAt.seconds
//                    }
//                    _notifications.value = sortedNotifications
//                    _loading.value = false
//                }
//                is Result.Error -> {
//                    _error.value = result.message
//                    _loading.value = false
//                }
//                is Result.Loading -> {
//                    // Already handled
//                }
//            }
//        }
//    }
//
//    /**
//     * Get count of unread notifications
//     */
//    fun getUnreadNotificationsCount(userId: String) {
//        viewModelScope.launch {
//            when (val result = notificationManager.getUnreadNotificationsCount(userId)) {
//                is Result.Success -> {
//                    _unreadCount.value = result.data
//                }
//                is Result.Error -> {
//                    _error.value = result.message
//                }
//                is Result.Loading -> {
//                    // Not needed
//                }
//            }
//        }
//    }
//
//    /**
//     * Mark a notification as read
//     */
//    fun markNotificationAsRead(userNotificationId: String) {
//        viewModelScope.launch {
//            when (val result = notificationManager.markNotificationAsRead(userNotificationId)) {
//                is Result.Success -> {
//                    // Update the notification in our local list
//                    _notifications.value = _notifications.value?.map { pair ->
//                        if (pair.second.id == userNotificationId) {
//                            // Create a new UserNotification with isRead set to true
//                            val updatedUserNotification = pair.second.copy(
//                                isRead = true,
//                                readAt = Timestamp.Companion.now()
//                            )
//                            Pair(pair.first, updatedUserNotification)
//                        } else {
//                            pair
//                        }
//                    }
//
//                    // Update the unread count
//                    _unreadCount.value = (_unreadCount.value ?: 0) - 1
//                }
//                is Result.Error -> {
//                    _error.value = result.message
//                }
//                is Result.Loading -> {
//                    // Not needed
//                }
//            }
//        }
//    }
//
//    /**
//     * Set a notification as selected for detail view
//     */
//    fun selectNotification(notification: NotificationData, userNotification: UserNotification) {
//        _selectedNotification.value = Pair(notification, userNotification)
//    }
//
//    /**
//     * Create a promo notification and send to all users
//     */
//    fun createPromoNotificationAndSendToAll(promoData: PromoData) {
//        viewModelScope.launch {
//            _loading.value = true
//            _error.value = null
//
//            // Create the notification
//            val notificationResult = notificationManager.createPromoNotification(
//                promoId = promoData.id,
//                title = "Promo Baru: ${promoData.discountAmount}" +
//                        if (promoData.discountType == "percentage") "% OFF" else " Rupiah",
//                description = promoData.description,
//                imageUrl = null // You might want to add an image URL for the promo
//            )
//
//            when (notificationResult) {
//                is Result.Success -> {
//                    val notificationId = notificationResult.data
//
//                    // Get all users
//                    when (val usersResult = dataRepository.getAllUsers()) {
//                        is Result.Success -> {
//                            val userIds = usersResult.data.map { it.id }
//
//                            // Send notification to all users
//                            when (val sendResult = notificationManager.sendNotificationToAllUsers(userIds, notificationId)) {
//                                is Result.Success -> {
//                                    _loading.value = false
//                                }
//                                is Result.Error -> {
//                                    _error.value = sendResult.message
//                                    _loading.value = false
//                                }
//                                is Result.Loading -> {
//                                    // Not needed
//                                }
//                            }
//                        }
//                        is Result.Error -> {
//                            _error.value = usersResult.message
//                            _loading.value = false
//                        }
//                        is Result.Loading -> {
//                            // Not needed
//                        }
//                    }
//                }
//                is Result.Error -> {
//                    _error.value = notificationResult.message
//                    _loading.value = false
//                }
//                is Result.Loading -> {
//                    // Not needed
//                }
//            }
//        }
//    }
//
//    /**
//     * Create a news notification and send to all users
//     */
//    fun createNewsNotificationAndSendToAll(newsData: NewsData) {
//        viewModelScope.launch {
//            _loading.value = true
//            _error.value = null
//
//            // Create the notification
//            val notificationResult = notificationManager.createNewsNotification(
//                newsId = newsData.id,
//                title = newsData.title,
//                description = newsData.description,
//                imageUrl = newsData.imageUrl
//            )
//
//            when (notificationResult) {
//                is Result.Success -> {
//                    val notificationId = notificationResult.data
//
//                    // Get all users
//                    when (val usersResult = dataRepository.getAllUsers()) {
//                        is Result.Success -> {
//                            val userIds = usersResult.data.map { it.id }
//
//                            // Send notification to all users
//                            when (val sendResult = notificationManager.sendNotificationToAllUsers(userIds, notificationId)) {
//                                is Result.Success -> {
//                                    _loading.value = false
//                                }
//                                is Result.Error -> {
//                                    _error.value = sendResult.message
//                                    _loading.value = false
//                                }
//                                is Result.Loading -> {
//                                    // Not needed
//                                }
//                            }
//                        }
//                        is Result.Error -> {
//                            _error.value = usersResult.message
//                            _loading.value = false
//                        }
//                        is Result.Loading -> {
//                            // Not needed
//                        }
//                    }
//                }
//                is Result.Error -> {
//                    _error.value = notificationResult.message
//                    _loading.value = false
//                }
//                is Result.Loading -> {
//                    // Not needed
//                }
//            }
//        }
//    }
//
//    /**
//     * Register device token for a user
//     */
//    fun registerDeviceToken(userId: String) {
//        viewModelScope.launch {
//            when (val result = fcmTokenManager.registerUserToken(userId)) {
//                is Result.Success -> {
//                    // Token registered successfully
//                    Log.e("FCM Token register:", "Success")
//                }
//                is Result.Error -> {
//                    _error.value = result.message
//                }
//                is Result.Loading -> {
//                    // Not needed
//                }
//            }
//        }
//    }
//
//    /**
//     * Remove device token when user logs out
//     */
//    fun removeDeviceToken(userId: String) {
//        viewModelScope.launch {
//            fcmTokenManager.removeUserToken(userId)
//        }
//    }
//
//    // Get current user (simplified for demo)
//    private suspend fun getCurrentUser(): UserData? {
//        // In a real app, you would get this from a UserRepository or AuthRepository
//        // For this demo, we'll just return a hardcoded user ID
//        val userId = "04n7jPlbPtdRuiZKfZdtGP3xadV2"
//        return when (val result = dataRepository.getUserById(userId)) {
//            is Result.Success -> result.data
//            else -> null
//        }
//    }
//
//    fun clearError() {
//        _error.value = null
//    }
//
//    fun initializeDummyData() {
//        viewModelScope.launch {
//            createDummyNewsAndPromos()
//        }
//    }
//
//    private suspend fun createDummyNewsAndPromos() {
//        // Create dummy news
//        val newsData1 = NewsData(
//            title = "Grand Opening Homestay Baru",
//            description = "Kami dengan bangga mengumumkan pembukaan unit homestay baru kami di Bali. Nikmati diskon 20% untuk pemesanan pertama!",
//            createdAt = Timestamp.Companion.now()
//        )
//
//        // Create dummy promos
//        val promoData1 = PromoData(
//            applicablePackageIds = listOf(mapOf("package_id" to "package1")),
//            isActive = true,
//            startDate = Timestamp.Companion.now(),
//            endDate = Timestamp(Timestamp.Companion.now().seconds + 2592000, 0),
//            description = "Diskon 15% untuk pemesanan 3 malam atau lebih.",
//            discountAmount = 15.0,
//            discountType = "percentage",
//            minBookings = 3
//        )
//
//        val newsId1 = when (val result = dataRepository.createNews(newsData1)) {
//            is Result.Success -> result.data
//            else -> ""
//        }
//
//        val promoId1 = when (val result = dataRepository.createPromo(promoData1)) {
//            is Result.Success -> result.data
//            else -> ""
//        }
//
//        if (newsId1.isNotEmpty()) {
//            createNewsNotification(newsId1, newsData1)
//        }
//
//        if (promoId1.isNotEmpty()) {
//            createPromoNotification(promoId1, promoData1)
//        }
//    }
//
//    private suspend fun createNewsNotification(newsId: String, newsData: NewsData) {
//        val notificationId = when (val result = notificationManager.createNewsNotification(
//            newsId = newsId,
//            title = newsData.title,
//            description = newsData.description,
//            imageUrl = newsData.imageUrl
//        )) {
//            is Result.Success -> result.data
//            else -> ""
//        }
//
//        if (notificationId.isNotEmpty()) {
//            when (val result = dataRepository.getAllUsers()) {
//                is Result.Success -> {
//                    val userIds = result.data.map { it.id }
//                    notificationManager.sendNotificationToAllUsers(userIds, notificationId)
//                }
//                else -> {}
//            }
//        }
//    }
//
//    private suspend fun createPromoNotification(promoId: String, promoData: PromoData) {
//        val notificationId = when (val result = notificationManager.createPromoNotification(
//            promoId = promoId,
//            title = "Promo Baru: ${promoData.discountAmount}% OFF",
//            description = promoData.description,
//            imageUrl = null
//        )) {
//            is Result.Success -> result.data
//            else -> ""
//        }
//
//        if (notificationId.isNotEmpty()) {
//            when (val result = dataRepository.getAllUsers()) {
//                is Result.Success -> {
//                    val userIds = result.data.map { it.id }
//                    notificationManager.sendNotificationToAllUsers(userIds, notificationId)
//                }
//                else -> {}
//            }
//        }
//    }
//}