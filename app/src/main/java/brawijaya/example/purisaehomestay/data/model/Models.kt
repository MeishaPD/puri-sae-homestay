package brawijaya.example.purisaehomestay.data.model
import androidx.compose.ui.graphics.painter.Painter
import com.google.firebase.Timestamp
import java.util.Date

data class UserData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "Penyewa",
    val photoUrl: String? = null
)

data class PackageData(
    val id: Int,
    val title: String = "",
    val features: List<String>,
    val weekdayPrice: Double = 0.0,
    val weekendPrice: Double = 0.0,
    val imageUrl: Painter? = null,
    val homestayRequirements: List<HomestayRequirement> = listOf(),
    val minDurasiMenginap: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Paket(
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

data class HomestayRequirement(
    val homestayId: String = "",
    val jumlah: Int = 1
)

data class HomestayAvailabilityData(
    val id: String = "",
    val homestayId: String = "",
    val date: Timestamp = Timestamp.now(),
    val status: String = "available",
    val bookingsId: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class BookingsData(
    val id: String = "",
    val userId: String = "",
    val paketId: String = "",
    val namaPemesan: String = "",
    val nomorWa: String = "",
    val tanggalCheckIn: Timestamp = Timestamp.now(),
    val tanggalCheckOut: Timestamp = Timestamp.now(),
    val jumlahTamu: Int = 1,
    val homestayBookings: List<HomestayBooking> = listOf(),
    val statusPemesanan: String = "tentative",
    val totalHarga: Double = 0.0,
    val promoId: String? = null,
    val diskon: Double = 0.0,
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val completedAt: Timestamp? = null
)

data class HomestayBooking(
    val homestayId: String = "",
    val namaHomestay: String = ""
)

data class PaymentsData(
    val id: String = "",
    val pemesananId: String = "",
    val jumlahPembayaran: Double = 0.0,
    val metodePembayaran: String = "",
    val tanggalPembayaran: Timestamp = Timestamp.now(),
    val statusVerifikasi: Boolean = false,
    val buktiPembayaran: String = "",
    val capLunas: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class NewsData(
    val id: Int,
    val description: String = "",
    val date: String = "",
    val imageUrl: List<Int> = emptyList<Int>(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)

data class NotificationData(
    val id: String = "",
    val type: NotificationType = NotificationType.NEWS,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val referenceId: String = "",
    val isActive: Boolean = true
)

data class UserNotification(
    val id: String = "",
    val userId: String = "",
    val notificationId: String = "",
    val isRead: Boolean = false,
    val readAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now()
)

data class PromoData(
    val id: String = "",
    val applicablePackageIds: List<Map<String, Any>> = emptyList(),
    val isActive: Boolean = true,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val description: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val discountAmount: Double = 0.0,
    val discountType: String = "percentage",
    val minBookings: Int = 1,
    val updatedAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)

enum class NotificationType {
    PROMO, NEWS
}

data class Order(
    val date: Date,
    val isPaid: Boolean,
    val title: String,
    val totalPrice: Int,
    val amountToBePaid: Int? = null,
    val imageResId: Int
)

data class OrderData(
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
    val paymentStatus: Boolean = false,
    val paymentUrl: String = "",
    val pricePerNight: Double = 0.0,
    val totalPrice: Double = 0.0,
    val userRef: String = ""
) {
    constructor() : this(
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
        paymentStatus = false,
        paymentUrl = "",
        pricePerNight = 0.0,
        totalPrice = 0.0,
        userRef = ""
    )
}
