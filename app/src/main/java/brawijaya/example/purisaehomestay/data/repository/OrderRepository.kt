package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.data.model.PaymentVerificationStage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.FieldValue
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
    private val db: FirebaseFirestore
) {
    private val orderCollection = db.collection("orders")
    private val packageCollection = db.collection("package")

    /**
     * Mendapatkan semua pesanan sebagai Flow
     */
    val orders: Flow<List<OrderData>> = callbackFlow {
        val listener = orderCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val orderList = snapshot?.documents?.mapNotNull { document ->
                try {
                    document.toObject<OrderData>()
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Error parsing order: ${e.message}")
                    null
                }
            } ?: emptyList()

            Log.d("OrderRepository", "Order list: $orderList")
            trySend(orderList)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Mendapatkan paket yang tersedia berdasarkan rentang tanggal
     */
    suspend fun getAvailablePackages(checkInDate: Date, checkOutDate: Date): List<Paket> {
        return try {
            val checkInTimestamp = Timestamp(checkInDate)
            val checkOutTimestamp = Timestamp(checkOutDate)

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


            val allPackages = packageCollection.get().await().documents.mapNotNull { document ->
                try {
                    document.toObject<Paket>()?.copy(id = document.id.hashCode())
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
     * Mengambil semua pesanan
     */
    suspend fun getAllOrders(): List<OrderData> {
        return try {
            val snapshot = orderCollection.get().await()
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
    suspend fun verifyPayment(orderId: String, paidAmount: Double, paymentStatus: Int) {
        try {
            val orderDoc = orderCollection.document(orderId)

            val currentOrder = getOrderById(orderId)
            val verificationStage = when {
                currentOrder?.paymentType == "Pembayaran Lunas" && paymentStatus == 2 -> PaymentVerificationStage.LUNAS
                currentOrder?.paymentType == "Pembayaran DP 25%" && paymentStatus == 1 -> PaymentVerificationStage.DP
                currentOrder?.paymentType == "Pembayaran DP 25%" && paymentStatus == 2 -> PaymentVerificationStage.LUNAS
                else -> PaymentVerificationStage.NONE
            }

            orderDoc.update(
                mapOf(
                    "paymentStatus" to paymentStatus,
                    "paidAmount" to paidAmount,
                    "paymentVerificationStage" to verificationStage.name
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
     * Mengambil pesanan berdasarkan user
     */
    suspend fun getOrdersByUser(userRef: String): List<OrderData> {
        return try {
            val snapshot = orderCollection
                .whereEqualTo("userRef", userRef)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<OrderData>()
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
     * Helper function to create package reference
     */
    fun createPackageRef(packageUid: String): String {
        return "package/$packageUid"
    }
}