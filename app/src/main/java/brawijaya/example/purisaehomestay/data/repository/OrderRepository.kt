package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PackageData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.utils.DateUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk mengelola pesanan
 * Menggunakan Firebase Firestore sebagai sumber data
 */
@Singleton
class OrderRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val promoRepository: PromoRepository
) {
    private val orderCollection = db.collection("orders")
    private val packageCollection = db.collection("package")

    /**
     * Mendapatkan semua pesanan sebagai Flow
     */
    val orders: Flow<List<OrderData>> = callbackFlow {
        val listener = orderCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val orderList = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject<OrderData>()?.copy(documentId = document.id)
                    } catch (e: Exception) {
                        Log.e("OrderRepository", "Error parsing order: ${e.message}")
                        null
                    }
                } ?: emptyList()

                trySend(orderList)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Mendapatkan paket yang tersedia berdasarkan rentang tanggal
     * Updated to use DateUtils for date handling
     */
    suspend fun getAvailablePackages(checkInDate: Date, checkOutDate: Date): List<PackageData> {
        return try {
            val checkInTimestamp = Timestamp(checkInDate)
            val checkOutTimestamp = Timestamp(checkOutDate)

            Log.d("OrderRepository", "Check-in: ${DateUtils.formatDate(checkInDate)}")
            Log.d("OrderRepository", "Check-out: ${DateUtils.formatDate(checkOutDate)}")

            val overlappingOrders1 = orderCollection
                .whereLessThan("check_in", checkOutTimestamp)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        val order = document.toObject<OrderData>()
                        if (order != null && order.check_out > checkInTimestamp) {
                            order
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("OrderRepository", "Error parsing overlapping order: ${e.message}")
                        null
                    }
                }

            var usedBungalowQty = 0
            var usedJogloQty = 0

            overlappingOrders1.forEach { order ->
                usedBungalowQty += order.bungalowQty
                usedJogloQty += order.jogloQty
            }

            val totalBungalowQty = 3
            val totalJogloQty = 1
            val remainingBungalowQty = totalBungalowQty - usedBungalowQty
            val remainingJogloQty = totalJogloQty - usedJogloQty

            Log.d("OrderRepository", "Remaining - Bungalow: $remainingBungalowQty, Joglo: $remainingJogloQty")

            val allPackages = packageCollection.get().await().documents.mapNotNull { document ->
                try {
                    document.toObject<PackageData>()?.copy(id = document.id.hashCode())
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Error parsing package: ${e.message}")
                    null
                }
            }

            val availablePackages = allPackages.filter { paket ->
                paket.bungalowQty <= remainingBungalowQty && paket.jogloQty <= remainingJogloQty
            }

            availablePackages

        } catch (e: Exception) {
            throw Exception("Failed to get available packages: ${e.message}")
        }
    }

    /**
     * Helper function to get available packages using date strings
     * Uses DateUtils for consistent date parsing
     */
    suspend fun getAvailablePackages(checkInDateString: String, checkOutDateString: String): List<PackageData> {
        val checkInDate = DateUtils.parseDate(checkInDateString)
            ?: throw Exception("Invalid check-in date format: $checkInDateString")
        val checkOutDate = DateUtils.parseDate(checkOutDateString)
            ?: throw Exception("Invalid check-out date format: $checkOutDateString")

        if (!DateUtils.isValidCheckOutDate(checkInDateString, checkOutDateString)) {
            throw Exception("Check-out date must be after check-in date")
        }

        return getAvailablePackages(checkInDate, checkOutDate)
    }

    /**
     * Validasi kode promo dan hitung harga
     */
    suspend fun validatePromoAndCalculatePrice(
        promoCode: String,
        originalPrice: Double,
        packageRef: String? = null
    ): PromoCalculationResult {
        return try {
            val validationResult = promoRepository.validatePromoCode(promoCode, packageRef)

            if (!validationResult.isValid) {
                return PromoCalculationResult(
                    isValid = false,
                    errorMessage = validationResult.errorMessage,
                    originalPrice = originalPrice,
                    finalPrice = originalPrice,
                    discountAmount = 0.0,
                    promo = null
                )
            }

            val promo = validationResult.promo!!
            val discountAmount = promoRepository.getDiscountAmount(originalPrice, promo)
            val finalPrice = promoRepository.calculateDiscountedPrice(originalPrice, promo)

            PromoCalculationResult(
                isValid = true,
                errorMessage = null,
                originalPrice = originalPrice,
                finalPrice = finalPrice,
                discountAmount = discountAmount,
                promo = promo
            )

        } catch (e: Exception) {
            PromoCalculationResult(
                isValid = false,
                errorMessage = "Gagal memvalidasi promo: ${e.message}",
                originalPrice = originalPrice,
                finalPrice = originalPrice,
                discountAmount = 0.0,
                promo = null
            )
        }
    }

    /**
     * Membuat pesanan baru
     */
    suspend fun createOrder(orderData: OrderData): String {
        return try {
            val docRef = orderCollection.add(orderData).await()
            docRef.id
        } catch (e: Exception) {
            throw Exception("Failed to create order: ${e.message}")
        }
    }

    /**
     * Membuat pesanan baru dengan dukungan promo
     */
    suspend fun createOrderWithPromo(
        orderData: OrderData,
        promoCode: String? = null
    ): CreateOrderResult {
        return try {
            var finalOrderData = orderData
            var appliedPromo: PromoData? = null
            var discountAmount = 0.0

            if (!promoCode.isNullOrEmpty()) {
                val promoResult = validatePromoAndCalculatePrice(
                    promoCode = promoCode,
                    originalPrice = orderData.totalPrice,
                    packageRef = orderData.packageRef
                )

                if (!promoResult.isValid) {
                    return CreateOrderResult(
                        success = false,
                        orderId = null,
                        errorMessage = promoResult.errorMessage
                    )
                }

                appliedPromo = promoResult.promo
                discountAmount = promoResult.discountAmount
                finalOrderData = orderData.copy(
                    totalPrice = promoResult.finalPrice,
                    promoRef = appliedPromo?.id
                )
            }

            val docRef = orderCollection.add(finalOrderData).await()

            CreateOrderResult(
                success = true,
                orderId = docRef.id,
                errorMessage = null,
                appliedPromo = appliedPromo,
                discountAmount = discountAmount,
                finalPrice = finalOrderData.totalPrice
            )

        } catch (e: Exception) {
            CreateOrderResult(
                success = false,
                orderId = null,
                errorMessage = "Failed to create order: ${e.message}"
            )
        }
    }

    /**
     * Helper function to create order using date strings
     * Uses DateUtils for consistent date parsing
     */
    suspend fun createOrderWithDateStrings(
        checkInDateString: String,
        checkOutDateString: String,
        guestName: String,
        guestPhone: String,
        guestQty: Int,
        selectedPackage: PackageData,
        paymentType: String,
        userRef: String,
        promoCode: String? = null
    ): CreateOrderResult {
        return try {
            // Parse dates using DateUtils
            val checkInDate = DateUtils.parseDate(checkInDateString)
                ?: throw Exception("Invalid check-in date format")
            val checkOutDate = DateUtils.parseDate(checkOutDateString)
                ?: throw Exception("Invalid check-out date format")

            // Validate date range
            if (!DateUtils.isValidCheckOutDate(checkInDateString, checkOutDateString)) {
                throw Exception("Check-out date must be after check-in date")
            }

            // Calculate nights and pricing
            val numberOfNights = DateUtils.calculateNights(checkInDateString, checkOutDateString)
            val pricePerNight = selectedPackage.price_weekday
            val totalPrice = pricePerNight * numberOfNights

            // Determine payment status
            val paymentStatus = if (paymentType == "Pembayaran Lunas") {
                PaymentStatusStage.LUNAS
            } else {
                PaymentStatusStage.DP
            }

            // Create order data
            val orderData = OrderData(
                check_in = Timestamp(checkInDate),
                check_out = Timestamp(checkOutDate),
                guestName = guestName,
                guestPhone = guestPhone,
                guestQty = guestQty,
                jogloQty = selectedPackage.jogloQty,
                bungalowQty = selectedPackage.bungalowQty,
                numberOfNights = numberOfNights,
                occupiedDates = generateOccupiedDates(checkInDateString, checkOutDateString),
                packageRef = createPackageRef(selectedPackage.id.toString()),
                paidAmount = 0.0,
                paymentType = paymentType,
                paymentUrls = emptyList(),
                pricePerNight = pricePerNight,
                totalPrice = totalPrice,
                paymentStatus = paymentStatus,
                promoRef = "",
                userRef = userRef,
                createdAt = Timestamp.now()
            )

            // Create order with or without promo
            if (!promoCode.isNullOrEmpty()) {
                createOrderWithPromo(orderData, promoCode)
            } else {
                val orderId = createOrder(orderData)
                CreateOrderResult(
                    success = true,
                    orderId = orderId,
                    errorMessage = null,
                    finalPrice = totalPrice
                )
            }

        } catch (e: Exception) {
            CreateOrderResult(
                success = false,
                orderId = null,
                errorMessage = "Failed to create order: ${e.message}"
            )
        }
    }

    /**
     * Generate occupied dates between check-in and check-out
     * Uses DateUtils for consistent date formatting
     */
    private fun generateOccupiedDates(checkInDateString: String, checkOutDateString: String): List<String> {
        return try {
            val checkInDate = DateUtils.parseDate(checkInDateString) ?: return emptyList()
            val checkOutDate = DateUtils.parseDate(checkOutDateString) ?: return emptyList()

            val occupiedDates = mutableListOf<String>()
            val calendar = java.util.Calendar.getInstance()
            calendar.time = checkInDate

            while (calendar.time.before(checkOutDate)) {
                occupiedDates.add(DateUtils.formatDate(calendar.time))
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }

            occupiedDates
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error generating occupied dates: ${e.message}")
            emptyList()
        }
    }

    /**
     * Mengambil pesanan berdasarkan ID
     */
    suspend fun getOrderById(id: String): OrderData? {
        return try {
            val document = orderCollection.document(id).get().await()
            document.toObject<OrderData>()
        } catch (e: Exception) {
            throw Exception("Failed to get order: ${e.message}")
        }
    }

    /**
     * Mengambil pesanan berdasarkan ID dengan informasi promo
     */
    suspend fun getOrderWithPromoById(id: String): OrderWithPromoInfo? {
        return try {
            val document = orderCollection.document(id).get().await()
            val order = document.toObject<OrderData>()

            if (order == null) return null

            var promoInfo: PromoData? = null
            if (!order.promoRef.isNullOrEmpty()) {
                promoInfo = promoRepository.getPromoById(order.promoRef)
            }

            OrderWithPromoInfo(
                order = order,
                promo = promoInfo
            )

        } catch (e: Exception) {
            throw Exception("Failed to get order with promo: ${e.message}")
        }
    }

    /**
     * Mengambil semua pesanan
     */
    suspend fun getAllOrders(): List<OrderData> {
        return try {
            val snapshot = orderCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<OrderData>()
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Error parsing order: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to get orders: ${e.message}")
        }
    }

    /**
     * Mengubah data pesanan
     */
    suspend fun updateOrder(id: String, orderData: OrderData) {
        try {
            orderCollection.document(id).set(orderData).await()
        } catch (e: Exception) {
            throw Exception("Failed to update order: ${e.message}")
        }
    }

    /**
     * Menghapus pesanan berdasarkan ID
     */
    suspend fun deleteOrder(id: String) {
        try {
            orderCollection.document(id).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete order: ${e.message}")
        }
    }

    /**
     * Memverifikasi pembayaran
     */
    suspend fun verifyPayment(orderId: String, paidAmount: Double, paymentStatus: PaymentStatusStage) {
        try {
            val orderDoc = orderCollection.document(orderId)
            val currentOrder = getOrderById(orderId)

            val paymentStage = when {
                currentOrder?.paymentType == "Pembayaran Lunas" && paymentStatus == PaymentStatusStage.LUNAS -> PaymentStatusStage.COMPLETED
                currentOrder?.paymentType == "Pembayaran DP 25%" && paymentStatus == PaymentStatusStage.DP -> PaymentStatusStage.WAITING
                currentOrder?.paymentType == "Pembayaran DP 25%" && paymentStatus == PaymentStatusStage.SISA -> PaymentStatusStage.COMPLETED
                else -> PaymentStatusStage.NONE
            }

            orderDoc.update(
                mapOf(
                    "paymentStatus" to paymentStage.name,
                    "paidAmount" to paidAmount,
                )
            ).await()
        } catch (e: Exception) {
            throw Exception("Failed to verify payment: ${e.message}")
        }
    }

    /**
     * Update payment URL for order
     */
    suspend fun updatePaymentUrl(orderId: String, paymentUrl: String) {
        try {
            val orderDoc = orderCollection.document(orderId)
            orderDoc.update("paymentUrls", FieldValue.arrayUnion(paymentUrl)).await()
        } catch (e: Exception) {
            throw Exception("Failed to update payment URL: ${e.message}")
        }
    }

    /**
     * Complete payment
     */
    suspend fun DPPayment(orderId: String, paymentUrl: String) {
        try {
            val orderDoc = orderCollection.document(orderId)
            val currentOrder = getOrderById(orderId) ?: throw Exception("Order not found")

            val updatedPaymentUrls = currentOrder.paymentUrls.toMutableList()
            updatedPaymentUrls.add(paymentUrl)

            orderDoc.update(
                mapOf(
                    "paymentStatus" to PaymentStatusStage.SISA,
                    "paymentUrls" to updatedPaymentUrls
                )
            ).await()
        } catch (e: Exception) {
            throw Exception("Failed to complete payment: ${e.message}")
        }
    }

    /**
     * Mengambil pesanan berdasarkan user
     */
    suspend fun getOrdersByUser(userRef: String): List<OrderData> {
        return try {
            val snapshot = orderCollection
                .whereEqualTo("userRef", userRef)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<OrderData>()?.copy(documentId = document.id)
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Error parsing user order: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to get user orders: ${e.message}")
        }
    }

    /**
     * Get orders by date range using DateUtils
     */
    suspend fun getOrdersByDateRange(startDateString: String, endDateString: String): List<OrderData> {
        return try {
            val startDate = DateUtils.parseDate(startDateString)
                ?: throw Exception("Invalid start date format")
            val endDate = DateUtils.parseDate(endDateString)
                ?: throw Exception("Invalid end date format")

            val startTimestamp = Timestamp(startDate)
            val endTimestamp = Timestamp(endDate)

            val snapshot = orderCollection
                .whereGreaterThanOrEqualTo("check_in", startTimestamp)
                .whereLessThanOrEqualTo("check_in", endTimestamp)
                .orderBy("check_in", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<OrderData>()?.copy(documentId = document.id)
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Error parsing order by date range: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to get orders by date range: ${e.message}")
        }
    }

    suspend fun rejectPayment(orderId: String) {
        try {
            val orderRef = orderCollection.document(orderId)
            orderRef.update(
                mapOf(
                    "paymentStatus" to PaymentStatusStage.REJECTED.name
                )
            ).await()
        } catch (e: Exception) {
            throw Exception("Failed to reject payment: ${e.message}")
        }
    }

    /**
     * Helper function to create package reference
     */
    fun createPackageRef(packageUid: String): String {
        return "package/$packageUid"
    }
}

data class PromoCalculationResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val originalPrice: Double,
    val finalPrice: Double,
    val discountAmount: Double,
    val promo: PromoData?
)

data class CreateOrderResult(
    val success: Boolean,
    val orderId: String?,
    val errorMessage: String?,
    val appliedPromo: PromoData? = null,
    val discountAmount: Double = 0.0,
    val finalPrice: Double = 0.0
)

data class OrderWithPromoInfo(
    val order: OrderData,
    val promo: PromoData?
)