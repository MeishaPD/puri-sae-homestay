package brawijaya.example.purisaehomestay.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DummyDataUtils {

    private val db = FirebaseFirestore.getInstance()

    suspend fun populateDummyData() {
        createDummyNews()
        createDummyPromos()
        createDummyNotifications()
    }

    private suspend fun createDummyNews() {
        val newsCollection = db.collection("news")

        val existingNews = newsCollection.get().await()
        if (existingNews.isEmpty) {
            val news1 = hashMapOf(
                "title" to "Grand Opening Puri Sae Homestay",
                "description" to "Kami dengan bangga mengumumkan pembukaan unit homestay baru kami. Nikmati diskon 20%",
                "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/fitly-test-app.appspot.com/o/Puri%20Sae%20Homestay%2Fbungalow_group.png?alt=media&token=5d2dd7e2-e99e-4799-9de5-13dadbcc5ef4",
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "isRead" to false
            )

            newsCollection.add(news1).await()
        }
    }

    private suspend fun createDummyPromos() {
        val promoCollection = db.collection("promo")

        val existingPromos = promoCollection.get().await()
        if (existingPromos.isEmpty) {
            val packageIds = listOf("package1", "package2", "package3")

            val promo1 = hashMapOf(
                "applicablePackageIds" to listOf(mapOf("packageId" to packageIds[0])),
                "promoStatus" to true,
                "startDate" to Timestamp.now(),
                "endsDate" to Timestamp(Timestamp.now().seconds + 2592000, 0),
                "description" to "Promo Natal diskon 15% untuk pemesanan 3 malam atau lebih. Berlaku untuk semua jenis kamar standar",
                "discountAmount" to 15.0,
                "discountType" to "percentage",
                "minBookings" to 3,
                "updatedAt" to Timestamp.now(),
                "isRead" to false
            )

            promoCollection.add(promo1).await()
        }
    }

    private suspend fun createDummyNotifications() {
        val notificationsRef = db.collection("notifications")
        val userNotificationsRef = db.collection("userNotifications")

        val existingNotifications = notificationsRef.get().await()
        if (existingNotifications.isEmpty) {
            // Create news notification
            val newsNotification = hashMapOf(
                "type" to "NEWS",
                "title" to "Berita Terbaru",
                "description" to "Grand Opening Puri Sae Homestay! Nikmati penawaran spesial.",
                "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/fitly-test-app.appspot.com/o/Puri%20Sae%20Homestay%2Fbungalow_group.png?alt=media&token=5d2dd7e2-e99e-4799-9de5-13dadbcc5ef4",
                "createdAt" to Timestamp.now(),
                "referenceId" to "", // Will be updated after creating the news
                "isActive" to true
            )

            // Create promo notification
            val promoNotification = hashMapOf(
                "type" to "PROMO",
                "title" to "Promo Spesial",
                "description" to "Promo Natal diskon 15% untuk pemesanan 3 malam atau lebih.",
                "imageUrl" to null, // Optional image for promo
                "createdAt" to Timestamp.now(),
                "referenceId" to "", // Will be updated after creating the promo
                "isActive" to true
            )

            // Find reference IDs
            val newsQuery = db.collection("news")
                .whereEqualTo("title", "Grand Opening Puri Sae Homestay")
                .limit(1)
                .get()
                .await()

            if (!newsQuery.isEmpty) {
                val newsId = newsQuery.documents[0].id
                newsNotification["referenceId"] = newsId
            }

            val promoQuery = db.collection("promo")
                .whereEqualTo("discountAmount", 15.0)
                .limit(1)
                .get()
                .await()

            if (!promoQuery.isEmpty) {
                val promoId = promoQuery.documents[0].id
                promoNotification["referenceId"] = promoId
            }

            // Add notifications
            val newsNotificationRef = notificationsRef.add(newsNotification).await()
            val promoNotificationRef = notificationsRef.add(promoNotification).await()

            // Create user notification links for test users
            val testUserIds = listOf("04n7jPlbPtdRuiZKfZdtGP3xadV2", "4LSJW2tjIrbntfnXojHK5D7JFsK2")

            for (userId in testUserIds) {
                val userNewsNotification = hashMapOf(
                    "userId" to userId,  
                    "notificationId" to newsNotificationRef.id,
                    "isRead" to false,   
                    "readAt" to null,    
                    "createdAt" to Timestamp.now()  
                )

                val userPromoNotification = hashMapOf(
                    "userId" to userId,  
                    "notificationId" to promoNotificationRef.id,
                    "isRead" to false,   
                    "readAt" to null,    
                    "createdAt" to Timestamp.now()  
                )

                userNotificationsRef.add(userNewsNotification).await()
                userNotificationsRef.add(userPromoNotification).await()
            }
        }
    }
}