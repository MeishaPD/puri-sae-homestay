package brawijaya.example.purisaehomestay.data.repository

import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.utils.DateUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val _promo = MutableStateFlow<List<PromoData>>(emptyList())
    val promo: Flow<List<PromoData>> = _promo.asStateFlow()

    private val promoCollection = firestore.collection("promo")

    suspend fun fetchAllPromo(): List<PromoData> {
        return try {
            val result = promoCollection
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val promoList = result.documents.mapNotNull { document ->
                document.toObject(PromoData::class.java)?.copy(id = document.id)
            }

            _promo.value = promoList
            promoList
        } catch (e: Exception) {
            throw Exception("Failed to fetch promo: ${e.message}")
        }
    }

    suspend fun getPromoById(id: String): PromoData? {
        return try {
            if (id.isBlank()) return null

            val document = promoCollection.document(id).get().await()
            if (document.exists()) {
                document.toObject(PromoData::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to get promo by ID: ${e.message}")
        }
    }

    suspend fun getPromoByPromoCode(promoCode: String): PromoData? {
        return try {
            if (promoCode.isBlank()) return null

            val result = promoCollection
                .whereEqualTo("promoCode", promoCode)
                .limit(1)
                .get()
                .await()

            if (result.documents.isNotEmpty()) {
                val document = result.documents.first()
                document.toObject(PromoData::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Failed to get promo by reference code: ${e.message}")
        }
    }

    suspend fun validatePromoCode(promoCode: String, packageRef: String? = null): PromoValidationResult {
        return try {
            if (promoCode.isBlank()) {
                return PromoValidationResult(
                    isValid = false,
                    errorMessage = "Kode promo tidak boleh kosong"
                )
            }

            val promo = getPromoByPromoCode(promoCode)

            if (promo == null) {
                return PromoValidationResult(
                    isValid = false,
                    errorMessage = "Kode promo tidak ditemukan"
                )
            }

            val currentDate = DateUtils.getCurrentDate()
            val startDate = promo.startDate.toDate()
            val endDate = promo.endDate.toDate()

            if (currentDate.before(startDate)) {
                return PromoValidationResult(
                    isValid = false,
                    errorMessage = "Promo belum dimulai"
                )
            }

            if (currentDate.after(endDate)) {
                return PromoValidationResult(
                    isValid = false,
                    errorMessage = "Promo sudah berakhir"
                )
            }

            if (promo.packageRef.isNotEmpty() && !packageRef.isNullOrEmpty()) {
                if (promo.packageRef != packageRef) {
                    return PromoValidationResult(
                        isValid = false,
                        errorMessage = "Promo tidak berlaku untuk paket ini"
                    )
                }
            }

            if (promo.discountPercentage <= 0 || promo.discountPercentage > 100) {
                return PromoValidationResult(
                    isValid = false,
                    errorMessage = "Persentase diskon harus antara 1-100%"
                )
            }

            PromoValidationResult(
                isValid = true,
                promo = promo,
                errorMessage = null
            )

        } catch (e: Exception) {
            PromoValidationResult(
                isValid = false,
                errorMessage = "Gagal memvalidasi kode promo: ${e.message}"
            )
        }
    }

    fun calculateDiscountedPrice(originalPrice: Double, promoData: PromoData): Double {
        if (originalPrice <= 0) return originalPrice

        val discountPercent = promoData.discountPercentage.coerceIn(0.0, 100.0)
        val discountAmount = originalPrice * (discountPercent / 100)
        val discountedPrice = originalPrice - discountAmount
        return discountedPrice.coerceAtLeast(0.0)
    }

    fun getDiscountAmount(originalPrice: Double, promoData: PromoData): Double {
        if (originalPrice <= 0) return 0.0

        val discountPercent = promoData.discountPercentage.coerceIn(0.0, 100.0)
        val discount = originalPrice * (discountPercent / 100)
        return discount.coerceAtMost(originalPrice)
    }

    suspend fun createPromo(promo: PromoData): String {
        return try {
            if (promo.title.isBlank()) {
                throw Exception("Judul promo tidak boleh kosong")
            }

            if (promo.promoCode.isBlank()) {
                throw Exception("Kode referral tidak boleh kosong")
            }

            if (promo.discountPercentage <= 0 || promo.discountPercentage > 100) {
                throw Exception("Persentase diskon harus antara 1-100%")
            }

            val existingPromo = getPromoByPromoCode(promo.promoCode)
            if (existingPromo != null) {
                throw Exception("Kode referral sudah digunakan")
            }

            val startDate = promo.startDate.toDate()
            val endDate = promo.endDate.toDate()

            if (startDate.after(endDate)) {
                throw Exception("Tanggal mulai tidak boleh setelah tanggal berakhir")
            }

            val docRef = promoCollection.document()
            val promoWithId = promo.copy(
                id = docRef.id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            docRef.set(promoWithId).await()

            fetchAllPromo()

            docRef.id
        } catch (e: Exception) {
            throw Exception("Failed to create promo: ${e.message}")
        }
    }

    suspend fun updatePromo(promo: PromoData) {
        try {
            if (promo.id.isBlank()) {
                throw Exception("ID promo tidak valid")
            }

            if (promo.title.isBlank()) {
                throw Exception("Judul promo tidak boleh kosong")
            }

            if (promo.promoCode.isBlank()) {
                throw Exception("Kode referral tidak boleh kosong")
            }

            if (promo.discountPercentage <= 0 || promo.discountPercentage > 100) {
                throw Exception("Persentase diskon harus antara 1-100%")
            }

            val existingPromo = getPromoByPromoCode(promo.promoCode)
            if (existingPromo != null && existingPromo.id != promo.id) {
                throw Exception("Kode referral sudah digunakan")
            }

            val startDate = promo.startDate.toDate()
            val endDate = promo.endDate.toDate()

            if (startDate.after(endDate)) {
                throw Exception("Tanggal mulai tidak boleh setelah tanggal berakhir")
            }

            val updatedPromo = promo.copy(
                promoCode = promo.promoCode.trim().uppercase(),
                updatedAt = Timestamp.now()
            )
            promoCollection.document(promo.id).set(updatedPromo).await()

            fetchAllPromo()
        } catch (e: Exception) {
            throw Exception("Failed to update promo: ${e.message}")
        }
    }

    suspend fun deletePromo(id: String) {
        try {
            if (id.isBlank()) {
                throw Exception("ID promo tidak valid")
            }

            promoCollection.document(id).delete().await()

            fetchAllPromo()
        } catch (e: Exception) {
            throw Exception("Failed to delete promo: ${e.message}")
        }
    }

    fun generateReferralCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val code = StringBuilder()

        repeat(8) {
            code.append(chars[random.nextInt(chars.length)])
        }

        return code.toString()
    }

    suspend fun generateUniqueReferralCode(maxAttempts: Int = 10): String {
        repeat(maxAttempts) {
            val code = generateReferralCode()
            val existingPromo = getPromoByPromoCode(code)
            if (existingPromo == null) {
                return code
            }
        }
        throw Exception("Tidak dapat menghasilkan kode referral unik setelah $maxAttempts percobaan")
    }

    suspend fun isReferralCodeAvailable(promoCode: String): Boolean {
        return try {
            val existingPromo = getPromoByPromoCode(promoCode)
            existingPromo == null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get active promos (current date is between start and end date)
     */
    suspend fun getActivePromos(): List<PromoData> {
        return try {
            val currentTimestamp = Timestamp.now()

            val result = promoCollection
                .whereLessThanOrEqualTo("startDate", currentTimestamp)
                .whereGreaterThanOrEqualTo("endDate", currentTimestamp)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                document.toObject(PromoData::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch active promos: ${e.message}")
        }
    }

    /**
     * Get promos by package reference
     */
    suspend fun getPromosByPackage(packageRef: String): List<PromoData> {
        return try {
            val result = promoCollection
                .whereEqualTo("packageRef", packageRef)
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                document.toObject(PromoData::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch promos by package: ${e.message}")
        }
    }
}

data class PromoValidationResult(
    val isValid: Boolean,
    val promo: PromoData? = null,
    val errorMessage: String? = null
)