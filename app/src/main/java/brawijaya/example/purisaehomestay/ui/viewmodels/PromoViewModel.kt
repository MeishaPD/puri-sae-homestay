package brawijaya.example.purisaehomestay.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.data.repository.PromoRepository
import brawijaya.example.purisaehomestay.utils.DateUtils
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PromoUiState(
    val isLoadingList: Boolean = false,
    val listError: String? = null,
    val listSuccess: Boolean = false,
    val listSuccessMessage: String? = null,

    val isLoadingForm: Boolean = false,
    val formError: String? = null,
    val formSuccess: Boolean = false,
    val formSuccessMessage: String? = null,

    val isLoadingValidation: Boolean = false,
    val isValidPromo: Boolean = false,
    val validatedPromo: PromoData? = null,
    val validationError: String? = null,

    val promoList: List<PromoData> = emptyList(),
    val selectedPromo: PromoData? = null,
    val activePromos: List<PromoData> = emptyList(),
    val packagePromos: List<PromoData> = emptyList()
)

@HiltViewModel
class PromoViewModel @Inject constructor(
    private val promoRepository: PromoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromoUiState())
    val uiState: StateFlow<PromoUiState> = _uiState.asStateFlow()

    init {
        fetchAllPromos()
        observePromoChanges()
    }

    private fun observePromoChanges() {
        viewModelScope.launch {
            promoRepository.promo.collect { promoList ->
                _uiState.update { it.copy(promoList = promoList) }
            }
        }
    }

    fun fetchAllPromos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listError = null) }
            try {
                val promoList = promoRepository.fetchAllPromo()
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        promoList = promoList,
                        listError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listError = e.message ?: "Gagal memuat promo"
                    )
                }
            }
        }
    }

    /**
     * Fetch active promos for current date
     */
    fun fetchActivePromos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listError = null) }
            try {
                val activePromos = promoRepository.getActivePromos()
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        activePromos = activePromos,
                        listError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listError = e.message ?: "Gagal memuat promo aktif"
                    )
                }
            }
        }
    }

    /**
     * Fetch active promos for specific date range
     */
    fun fetchActivePromosForDateRange(checkInDate: Date, checkOutDate: Date) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listError = null) }
            try {
                val activePromos = promoRepository.getActivePromosForDateRange(checkInDate, checkOutDate)
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        activePromos = activePromos,
                        listError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listError = e.message ?: "Gagal memuat promo untuk tanggal tersebut"
                    )
                }
            }
        }
    }

    /**
     * Fetch active promos for date range using date strings
     */
    fun fetchActivePromosForDateRange(checkInDateString: String, checkOutDateString: String) {
        viewModelScope.launch {
            try {
                val checkInDate = DateUtils.parseDate(checkInDateString)
                val checkOutDate = DateUtils.parseDate(checkOutDateString)

                if (checkInDate != null && checkOutDate != null) {
                    fetchActivePromosForDateRange(checkInDate, checkOutDate)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingList = false,
                            listError = "Format tanggal tidak valid"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listError = e.message ?: "Gagal memproses tanggal"
                    )
                }
            }
        }
    }

    /**
     * Fetch promos by package reference
     */
    fun fetchPromosByPackage(packageRef: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listError = null) }
            try {
                val packagePromos = promoRepository.getPromosByPackage(packageRef)
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        packagePromos = packagePromos,
                        listError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listError = e.message ?: "Gagal memuat promo paket"
                    )
                }
            }
        }
    }

    fun createPromo(
        title: String,
        description: String,
        startDate: Timestamp,
        endDate: Timestamp,
        discountPercentage: Double,
        promoCode: String,
        packageRef: String = "",
        imageUrl: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingForm = true, formError = null) }
            try {
                val promo = PromoData(
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    discountPercentage = discountPercentage,
                    discountType = "percentage",
                    promoCode = promoCode,
                    packageRef = packageRef,
                    imageUrl = imageUrl
                )

                val promoId = promoRepository.createPromo(promo)
                _uiState.update {
                    it.copy(
                        isLoadingForm = false,
                        formSuccess = true,
                        formSuccessMessage = "Promo berhasil dibuat dengan ID: $promoId"
                    )
                }
                clearFormState()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingForm = false,
                        formError = e.message ?: "Gagal membuat promo"
                    )
                }
            }
        }
    }

    fun updatePromo(promo: PromoData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingForm = true, formError = null) }
            try {
                promoRepository.updatePromo(promo)
                _uiState.update {
                    it.copy(
                        isLoadingForm = false,
                        formSuccess = true,
                        formSuccessMessage = "Promo berhasil diperbarui"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingForm = false,
                        formError = e.message ?: "Gagal memperbarui promo"
                    )
                }
            }
        }
    }

    fun deletePromo(promoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true, listError = null) }
            try {
                promoRepository.deletePromo(promoId)
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listSuccess = true,
                        listSuccessMessage = "Promo berhasil dihapus"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingList = false,
                        listError = e.message ?: "Gagal menghapus promo"
                    )
                }
            }
        }
    }

    fun getPromoById(promoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingForm = true) }
            try {
                val promo = promoRepository.getPromoById(promoId)
                _uiState.update { it.copy(selectedPromo = promo, isLoadingForm = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formError = "Gagal mengambil data promo: ${e.message}",
                        isLoadingForm = false
                    )
                }
            }
        }
    }

    /**
     * Get promo by promo code
     */
    fun getPromoByPromoCode(promoCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingForm = true) }
            try {
                val promo = promoRepository.getPromoByPromoCode(promoCode)
                _uiState.update {
                    it.copy(
                        selectedPromo = promo,
                        isLoadingForm = false,
                        formError = if (promo == null) "Promo dengan kode '$promoCode' tidak ditemukan" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        formError = "Gagal mengambil data promo: ${e.message}",
                        isLoadingForm = false
                    )
                }
            }
        }
    }

    fun generateUniqueReferralCode(callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val code = promoRepository.generateUniqueReferralCode()
                callback(code)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    fun checkReferralCodeAvailability(promoCode: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isAvailable = promoRepository.isReferralCodeAvailable(promoCode)
                callback(isAvailable)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    /**
     * Validate promo code with date validation support
     */
    fun validatePromoCode(
        promoCode: String,
        packageRef: String? = null,
        checkInDate: Date? = null,
        checkOutDate: Date? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingValidation = true,
                    validationError = null,
                    isValidPromo = false
                )
            }

            try {
                val result = promoRepository.validatePromoCode(promoCode, packageRef, checkInDate, checkOutDate)
                _uiState.update {
                    it.copy(
                        isLoadingValidation = false,
                        isValidPromo = result.isValid,
                        validatedPromo = result.promo,
                        validationError = result.errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingValidation = false,
                        isValidPromo = false,
                        validationError = e.message ?: "Gagal memvalidasi kode promo"
                    )
                }
            }
        }
    }

    /**
     * Validate promo code using date strings
     */
    fun validatePromoCode(
        promoCode: String,
        packageRef: String? = null,
        checkInDateString: String = "",
        checkOutDateString: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingValidation = true,
                    validationError = null,
                    isValidPromo = false
                )
            }

            try {
                val result = promoRepository.validatePromoCode(promoCode, packageRef, checkInDateString, checkOutDateString)
                _uiState.update {
                    it.copy(
                        isLoadingValidation = false,
                        isValidPromo = result.isValid,
                        validatedPromo = result.promo,
                        validationError = result.errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingValidation = false,
                        isValidPromo = false,
                        validationError = e.message ?: "Gagal memvalidasi kode promo"
                    )
                }
            }
        }
    }

    fun calculateDiscountedPrice(originalPrice: Double, promo: PromoData): Double {
        return promoRepository.calculateDiscountedPrice(originalPrice, promo)
    }

    fun getDiscountAmount(originalPrice: Double, promo: PromoData): Double {
        return promoRepository.getDiscountAmount(originalPrice, promo)
    }

    /**
     * Enhanced price calculation with date validation
     */
    fun calculatePriceWithPromo(
        originalPrice: Double,
        promoCode: String,
        packageRef: String? = null,
        checkInDate: Date? = null,
        checkOutDate: Date? = null,
        callback: (PriceCalculationResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val validationResult = promoRepository.validatePromoCode(
                    promoCode,
                    packageRef,
                    checkInDate,
                    checkOutDate
                )

                if (!validationResult.isValid) {
                    callback(
                        PriceCalculationResult(
                            isValid = false,
                            originalPrice = originalPrice,
                            finalPrice = originalPrice,
                            discountAmount = 0.0,
                            errorMessage = validationResult.errorMessage
                        )
                    )
                    return@launch
                }

                val promo = validationResult.promo!!
                val discountAmount = promoRepository.getDiscountAmount(originalPrice, promo)
                val finalPrice = promoRepository.calculateDiscountedPrice(originalPrice, promo)

                callback(
                    PriceCalculationResult(
                        isValid = true,
                        originalPrice = originalPrice,
                        finalPrice = finalPrice,
                        discountAmount = discountAmount,
                        discountPercentage = promo.discountPercentage,
                        promo = promo,
                        errorMessage = null
                    )
                )

            } catch (e: Exception) {
                callback(
                    PriceCalculationResult(
                        isValid = false,
                        originalPrice = originalPrice,
                        finalPrice = originalPrice,
                        discountAmount = 0.0,
                        errorMessage = e.message ?: "Gagal menghitung harga promo"
                    )
                )
            }
        }
    }

    /**
     * Calculate price with promo using date strings
     */
    fun calculatePriceWithPromo(
        originalPrice: Double,
        promoCode: String,
        packageRef: String? = null,
        checkInDateString: String = "",
        checkOutDateString: String = "",
        callback: (PriceCalculationResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val checkInDate = if (checkInDateString.isNotEmpty()) DateUtils.parseDate(checkInDateString) else null
                val checkOutDate = if (checkOutDateString.isNotEmpty()) DateUtils.parseDate(checkOutDateString) else null

                calculatePriceWithPromo(
                    originalPrice = originalPrice,
                    promoCode = promoCode,
                    packageRef = packageRef,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    callback = callback
                )
            } catch (e: Exception) {
                callback(
                    PriceCalculationResult(
                        isValid = false,
                        originalPrice = originalPrice,
                        finalPrice = originalPrice,
                        discountAmount = 0.0,
                        errorMessage = "Error parsing dates: ${e.message}"
                    )
                )
            }
        }
    }

    fun updateFormErrorMessage(message: String) {
        _uiState.update { it.copy(formError = message) }
    }

    fun clearFormState() {
        _uiState.update {
            it.copy(
                formError = null,
                formSuccess = false,
                formSuccessMessage = null
            )
        }
    }

    fun clearListState() {
        _uiState.update {
            it.copy(
                listError = null,
                listSuccess = false,
                listSuccessMessage = null
            )
        }
    }

    fun clearValidationState() {
        _uiState.update {
            it.copy(
                isValidPromo = false,
                validatedPromo = null,
                validationError = null
            )
        }
    }

    fun resetAllStates() {
        _uiState.value = PromoUiState()
        fetchAllPromos()
    }

    fun resetSelectedPromo() {
        _uiState.update { it.copy(selectedPromo = null) }
    }

    fun refreshPromos() {
        fetchAllPromos()
    }

    fun clearActivePromos() {
        _uiState.update { it.copy(activePromos = emptyList()) }
    }

    fun clearPackagePromos() {
        _uiState.update { it.copy(packagePromos = emptyList()) }
    }
}

data class PriceCalculationResult(
    val isValid: Boolean,
    val originalPrice: Double,
    val finalPrice: Double,
    val discountAmount: Double,
    val discountPercentage: Double = 0.0,
    val promo: PromoData? = null,
    val errorMessage: String? = null
)