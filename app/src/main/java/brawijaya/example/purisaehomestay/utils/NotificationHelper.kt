package brawijaya.example.purisaehomestay.utils

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.data.model.NotificationData
import brawijaya.example.purisaehomestay.data.model.NotificationType
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.data.repository.NotificationManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object NotificationHelper {

    // Get current user's FCM token
    fun getCurrentUserToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                callback(null)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Registration Token: $token")
            callback(token)
        }
    }

    // Save token to user document in Firestore
    fun saveTokenToUser(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved to user document")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error saving token to user document", e)
            }
    }

    // Notification functions for specific events
    suspend fun notifyNewBooking(orderData: OrderData) {
        val notification = NotificationData(
            type = NotificationType.BOOKING_CREATED,
            title = "Pesanan Baru!",
            message = "Ada pesanan baru dari ${orderData.guestName} untuk ${orderData.numberOfNights} malam",
            extraData = mapOf(
                "order_id" to orderData.documentId,
                "guest_name" to orderData.guestName
            )
        )
        NotificationManager.getInstance().sendNotificationToAdmins(notification)
    }

    suspend fun notifyPaymentReceived(orderData: OrderData) {
        val paymentType = when (orderData.paymentStatus) {
            PaymentStatusStage.DP -> "DP (25%)"
            PaymentStatusStage.SISA -> "Pelunasan"
            PaymentStatusStage.LUNAS -> "Pembayaran Penuh"
            else -> "Pembayaran"
        }

        val notification = NotificationData(
            type = NotificationType.PAYMENT_RECEIVED,
            title = "Pembayaran Diterima!",
            message = "Bukti $paymentType dari ${orderData.guestName} telah diunggah",
            extraData = mapOf(
                "order_id" to orderData.documentId,
                "payment_status" to orderData.paymentStatus.name
            )
        )
        NotificationManager.getInstance().sendNotificationToAdmins(notification)
    }

    suspend fun notifyPaymentConfirmed(orderData: OrderData, userToken: String) {
        val notification = NotificationData(
            type = NotificationType.PAYMENT_CONFIRMED,
            title = "Pembayaran Dikonfirmasi!",
            message = "Pembayaran Anda untuk pesanan ${orderData.documentId} telah dikonfirmasi",
            extraData = mapOf(
                "order_id" to orderData.documentId
            )
        )
        NotificationManager.getInstance().sendNotificationToUser(userToken, notification)
    }

    suspend fun notifyPaymentRejected(orderData: OrderData, userToken: String, reason: String = "") {
        val message = if (reason.isNotEmpty()) {
            "Pembayaran untuk pesanan ${orderData.documentId} ditolak. Alasan: $reason"
        } else {
            "Pembayaran untuk pesanan ${orderData.documentId} ditolak. Silakan hubungi admin"
        }

        val notification = NotificationData(
            type = NotificationType.PAYMENT_REJECTED,
            title = "Pembayaran Ditolak",
            message = message,
            extraData = mapOf(
                "order_id" to orderData.documentId,
                "reason" to reason
            )
        )
        NotificationManager.getInstance().sendNotificationToUser(userToken, notification)
    }

    suspend fun notifyNewPromo(promoData: PromoData) {
        val notification = NotificationData(
            type = NotificationType.PROMO,
            title = "Promo Baru!",
            message = "${promoData.title} - Diskon ${promoData.discountPercentage}%",
            extraData = mapOf(
                "promo_id" to promoData.id,
                "promo_code" to promoData.promoCode
            )
        )
        NotificationManager.getInstance().sendNotificationToAllUsers(notification)
    }

    suspend fun notifyNewNews(newsData: NewsData) {
        val notification = NotificationData(
            type = NotificationType.NEWS,
            title = "Berita Terbaru!",
            message = newsData.desc.take(100) + if (newsData.desc.length > 100) "..." else "",
            extraData = mapOf(
                "news_id" to newsData.id
            )
        )
        NotificationManager.getInstance().sendNotificationToAllUsers(notification)
    }
}