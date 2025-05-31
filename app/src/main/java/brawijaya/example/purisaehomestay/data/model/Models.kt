package brawijaya.example.purisaehomestay.data.model

import com.google.firebase.Timestamp

data class UserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "Penyewa",
    val photoUrl: String? = null
)

data class PackageData(
    val id: Int = 0,
    val title: String = "",
    val features: List<String> = emptyList(),
    val price_weekday: Double = 0.0,
    val price_weekend: Double = 0.0,
    val thumbnail_url : String? = null,
    val bungalowQty: Int = 1,
    val jogloQty: Int = 0
) {
    constructor() : this(
        id = 0,
        title = "",
        features = emptyList(),
        price_weekday = 0.0,
        price_weekend = 0.0,
        thumbnail_url = null,
        bungalowQty = 0,
        jogloQty = 0
    )
}

data class FCMRequest(
    val to: String? = null,
    val registration_ids: List<String>? = null,
    val notification: FCMNotification,
    val data: Map<String, String>? = null,
    val priority: String = "high"
)

data class FCMNotification(
    val title: String,
    val body: String,
    val icon: String = "ic_notification",
    val sound: String = "default"
)

data class FCMResponse(
    val multicast_id: Long,
    val success: Int,
    val failure: Int,
    val canonical_ids: Int,
    val results: List<FCMResult>
)

data class FCMResult(
    val message_id: String?,
    val registration_id: String?,
    val error: String?
)

enum class NotificationType {
    PROMO, NEWS, BOOKING_CREATED, PAYMENT_CONFIRMED, PAYMENT_REJECTED, PAYMENT_RECEIVED
}

data class NotificationData(
    val type: NotificationType,
    val title: String,
    val message: String,
    val extraData: Map<String, String> = emptyMap()
)

data class PromoData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val discountPercentage: Double = 0.0,
    val discountType: String = "percentage",
    val imageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val promoCode: String = "",
    val packageRef: String = ""
) {
    constructor() : this(
        id = "",
        title = "",
        description = "",
        startDate = Timestamp.now(),
        endDate = Timestamp.now(),
        discountPercentage = 0.0,
        discountType = "",
        imageUrl = "",
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now(),
        promoCode = "",
        packageRef = ""

    )
}

enum class PaymentStatusStage {
    NONE,    // No payment submitted yet
    DP,      // DP payment submitted, waiting for admin verification
    SISA,    // Remaining payment submitted, waiting for admin verification
    LUNAS,    // Fully paid, waiting for admin verification
    WAITING,  // Waiting for another user payment
    COMPLETED,   // Order completed
    REJECTED  // Order Rejected
}

data class OrderData(
    val documentId: String = "",
    val check_in: Timestamp = Timestamp.now(),
    val check_out: Timestamp = Timestamp.now(),
    val guestName: String = "",
    val guestPhone: String = "",
    val guestQty: Int = 1,
    val jogloQty: Int = 0,
    val bungalowQty: Int = 0,
    val numberOfNights: Int = 1,
    val occupiedDates: List<String> = emptyList(),
    val packageRef: String = "",
    val paidAmount: Double = 0.0,
    val paymentType: String = "",
    val paymentUrls: List<String> = emptyList(), // For DP: [firstPaymentUrl, secondPaymentUrl], For Lunas: [paymentUrl]
    val paymentStatus: PaymentStatusStage = PaymentStatusStage.NONE,
    val pricePerNight: Double = 0.0,
    val totalPrice: Double = 0.0,
    val userRef: String = "",
    val promoRef: String? = "",
    val createdAt: Timestamp = Timestamp.now(),
) {
    constructor() : this(
        documentId = "",
        check_in = Timestamp.now(),
        check_out = Timestamp.now(),
        guestName = "",
        guestPhone = "",
        guestQty = 1,
        jogloQty = 0,
        bungalowQty = 0,
        numberOfNights = 1,
        occupiedDates = emptyList(),
        packageRef = "",
        paidAmount = 0.0,
        paymentType = "",
        paymentUrls = emptyList(),
        paymentStatus = PaymentStatusStage.NONE,
        pricePerNight = 0.0,
        totalPrice = 0.0,
        userRef = "",
        promoRef = "",
        createdAt = Timestamp.now()
    )
}

data class NewsData(
    val id: String = "",
    val imageUrls: List<String> = emptyList(),
    val desc: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    constructor() : this(
        id = "",
        imageUrls = emptyList(),
        desc = "",
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now()
    )
}