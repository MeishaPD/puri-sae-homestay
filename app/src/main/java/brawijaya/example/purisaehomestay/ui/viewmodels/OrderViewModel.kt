package brawijaya.example.purisaehomestay.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PackageData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.data.repository.CreateOrderResult
import brawijaya.example.purisaehomestay.data.repository.OrderRepository
import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import brawijaya.example.purisaehomestay.utils.DateUtils
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val packageList: List<PackageData> = emptyList(),
    val selectedPackage: PackageData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasSelectedDateRange: Boolean = false,
    val isCreatingOrder: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val orderList: List<OrderData> = emptyList(),
    val availablePackages: List<PackageData> = emptyList(),
    val selectedPackageData: PackageData? = null,
    val currentOrderId: String = "",
    val promoCode: String = "",
    val isValidatingPromo: Boolean = false,
    val appliedPromo: PromoData? = null,
    val originalPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val finalPrice: Double = 0.0,
    val promoValidationMessage: String? = null
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val packageRepository: PackageRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    fun getAllOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                orderRepository.orders
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                errorMessage = "Gagal memuat daftar pesanan: ${e.message}",
                                isLoading = false
                            )
                        }
                    }
                    .collect { orders ->
                        _uiState.update { it.copy(orderList = orders, isLoading = false) }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengambil semua pesanan: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getPackages() {
        viewModelScope.launch {
            val packages = packageRepository.getAllPackages()
            _uiState.update {
                it.copy(packageList = packages)
            }
        }
    }

    fun getPaketById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val paket = packageRepository.getPackageById(id)
                _uiState.update { it.copy(selectedPackageData = paket, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengambil data paket: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getAvailablePackages(checkInDate: String, checkOutDate: String) {
        if (checkInDate.isEmpty() || checkOutDate.isEmpty()) {
            _uiState.update {
                it.copy(
                    availablePackages = emptyList(),
                    hasSelectedDateRange = false,
                    errorMessage = "Silakan pilih tanggal check-in dan check-out terlebih dahulu"
                )
            }
            return
        }

        if (!DateUtils.isValidCheckOutDate(checkInDate, checkOutDate)) {
            _uiState.update {
                it.copy(
                    availablePackages = emptyList(),
                    hasSelectedDateRange = false,
                    errorMessage = "Tanggal check-out harus setelah tanggal check-in"
                )
            }
            return
        }

        viewModelScope.launch {
            clearMessages()
            _uiState.update { it.copy(isLoading = true, hasSelectedDateRange = true) }
            try {
                val checkIn = DateUtils.parseDate(checkInDate)
                    ?: throw Exception("Format tanggal check-in tidak valid")
                val checkOut = DateUtils.parseDate(checkOutDate)
                    ?: throw Exception("Format tanggal check-out tidak valid")

                val availablePackages = orderRepository.getAvailablePackages(checkIn, checkOut)
                _uiState.update {
                    it.copy(
                        availablePackages = availablePackages,
                        packageList = availablePackages,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mendapatkan paket yang tersedia: ${e.message}",
                        isLoading = false,
                        availablePackages = emptyList(),
                        packageList = emptyList()
                    )
                }
            }
        }
    }

    fun calculatePriceWithPromo(
        checkInDate: String,
        checkOutDate: String,
        selectedPackageId: Int
    ) {
        viewModelScope.launch {
            try {
                DateUtils.parseDate(checkInDate)
                    ?: throw Exception("Invalid check-in date format")
                DateUtils.parseDate(checkOutDate)
                    ?: throw Exception("Invalid check-out date format")

                val selectedPackage = _uiState.value.packageList.find { it.id == selectedPackageId }
                    ?: throw Exception("Selected package not found")

                val numberOfNights = DateUtils.calculateNights(checkInDate, checkOutDate)
                val pricePerNight = selectedPackage.price_weekday
                val originalPrice = pricePerNight * numberOfNights

                _uiState.update {
                    it.copy(
                        originalPrice = originalPrice,
                        finalPrice = originalPrice
                    )
                }

                val currentState = _uiState.value
                if (currentState.appliedPromo != null) {
                    applyPromoToPrice(originalPrice, currentState.appliedPromo)
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Gagal menghitung harga: ${e.message}")
                }
            }
        }
    }

    fun validatePromoCode(promoCode: String, selectedPackageId: Int?) {
        if (promoCode.isEmpty()) {
            clearPromo()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isValidatingPromo = true,
                    promoValidationMessage = null
                )
            }

            try {
                val packageRef = selectedPackageId?.let { "package/$it" }
                val originalPrice = _uiState.value.originalPrice

                val promoResult = orderRepository.validatePromoAndCalculatePrice(
                    promoCode = promoCode,
                    originalPrice = originalPrice,
                    packageRef = packageRef
                )

                if (promoResult.isValid && promoResult.promo != null) {
                    _uiState.update {
                        it.copy(
                            isValidatingPromo = false,
                            appliedPromo = promoResult.promo,
                            discountAmount = promoResult.discountAmount,
                            finalPrice = promoResult.finalPrice,
                            promoValidationMessage = "Promo berhasil diterapkan!",
                            promoCode = promoCode
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isValidatingPromo = false,
                            appliedPromo = null,
                            discountAmount = 0.0,
                            finalPrice = originalPrice,
                            promoValidationMessage = promoResult.errorMessage,
                            promoCode = ""
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isValidatingPromo = false,
                        appliedPromo = null,
                        discountAmount = 0.0,
                        finalPrice = _uiState.value.originalPrice,
                        promoValidationMessage = "Gagal memvalidasi promo: ${e.message}",
                        promoCode = ""
                    )
                }
            }
        }
    }

    private fun applyPromoToPrice(originalPrice: Double, promo: PromoData) {
        viewModelScope.launch {
            try {
                val promoResult = orderRepository.validatePromoAndCalculatePrice(
                    promoCode = promo.promoCode,
                    originalPrice = originalPrice
                )

                if (promoResult.isValid) {
                    _uiState.update {
                        it.copy(
                            discountAmount = promoResult.discountAmount,
                            finalPrice = promoResult.finalPrice
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error applying promo: ${e.message}")
            }
        }
    }

    fun clearPromo() {
        _uiState.update {
            it.copy(
                appliedPromo = null,
                discountAmount = 0.0,
                finalPrice = it.originalPrice,
                promoValidationMessage = null,
                promoCode = ""
            )
        }
    }

    fun createOrder(
        checkInDate: String,
        checkOutDate: String,
        guestName: String,
        guestPhone: String,
        guestCount: String,
        selectedPackageId: Int,
        paymentType: String,
        userRef: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCreatingOrder = true) }

                val checkIn = DateUtils.parseDate(checkInDate)
                    ?: throw Exception("Invalid check-in date format")
                val checkOut = DateUtils.parseDate(checkOutDate)
                    ?: throw Exception("Invalid check-out date format")
                val guestQty = guestCount.toIntOrNull()
                    ?: throw Exception("Invalid guest count")

                if (!DateUtils.isValidCheckOutDate(checkInDate, checkOutDate)) {
                    throw Exception("Check-out date must be after check-in date")
                }

                val selectedPackage = _uiState.value.packageList.find { it.id == selectedPackageId }
                    ?: throw Exception("Selected package not found")

                val packageRef = orderRepository.createPackageRef(selectedPackage.id.toString())

                val numberOfNights = DateUtils.calculateNights(checkInDate, checkOutDate)
                val pricePerNight = selectedPackage.price_weekday

                val currentState = _uiState.value

                val finalPrice = if (currentState.appliedPromo != null) {
                    currentState.finalPrice
                } else {
                    pricePerNight * numberOfNights
                }

                val paymentStage = if (paymentType == "Pembayaran Lunas") PaymentStatusStage.LUNAS else PaymentStatusStage.DP

                val orderData = OrderData(
                    check_in = Timestamp(checkIn),
                    check_out = Timestamp(checkOut),
                    guestName = guestName,
                    guestPhone = guestPhone,
                    guestQty = guestQty,
                    jogloQty = selectedPackage.jogloQty,
                    bungalowQty = selectedPackage.bungalowQty,
                    numberOfNights = numberOfNights,
                    occupiedDates = emptyList(),
                    packageRef = packageRef,
                    paidAmount = 0.0,
                    paymentType = paymentType,
                    paymentUrls = emptyList(),
                    pricePerNight = pricePerNight,
                    totalPrice = finalPrice,
                    paymentStatus = paymentStage,
                    promoRef = currentState.appliedPromo?.id ?: "",
                    userRef = userRef ?: "",
                    createdAt = Timestamp.now()
                )

                val result = if (currentState.appliedPromo != null) {
                    orderRepository.createOrderWithPromo(orderData, currentState.promoCode)
                } else {
                    val orderId = orderRepository.createOrder(orderData)
                    CreateOrderResult(
                        success = true,
                        orderId = orderId,
                        errorMessage = null
                    )
                }

                if (result.success && result.orderId != null) {
                    Log.d("ViewModel", "CurrentOrderId: ${result.orderId}")
                    _uiState.update {
                        it.copy(
                            currentOrderId = result.orderId,
                            isCreatingOrder = false,
                            successMessage = "Order created successfully",
                            showPaymentDialog = true
                        )
                    }
                } else {
                    throw Exception(result.errorMessage ?: "Failed to create order")
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingOrder = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun verifyPayment(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val order = orderRepository.getOrderById(orderId)
                if (order == null) {
                    throw Exception("Order not found")
                }

                val newPaidAmount: Double

                when (order.paymentType) {
                    "Pembayaran DP 25%" -> {
                        newPaidAmount = if (order.paymentStatus == PaymentStatusStage.DP) {
                            order.totalPrice * 0.25
                        } else if (order.paymentStatus == PaymentStatusStage.SISA) {
                            order.totalPrice
                        } else {
                            throw Exception("Invalid payment status for DP payment type")
                        }
                    }
                    "Pembayaran Lunas" -> {
                        if (order.paymentStatus == PaymentStatusStage.LUNAS) {
                            newPaidAmount = order.totalPrice
                        } else {
                            throw Exception("Invalid payment status for Lunas payment type")
                        }
                    }
                    else -> throw Exception("Invalid payment type: ${order.paymentType}")
                }

                orderRepository.verifyPayment(orderId, newPaidAmount, order.paymentStatus)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Pembayaran berhasil diverifikasi",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal memverifikasi pembayaran: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun rejectPayment(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                orderRepository.rejectPayment(orderId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Pembayaran berhasil ditolak",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal menolak pembayaran: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getOrdersByUser(userRef: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                Log.d("UserRefOnVM", userRef)
                val userOrders = orderRepository.getOrdersByUser(userRef)
                _uiState.update {
                    it.copy(
                        orderList = userOrders,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengambil pesanan pengguna: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updatePaymentUrl(imageUrl: String) {
        val orderId = _uiState.value.currentOrderId
        if (orderId.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    orderRepository.updatePaymentUrl(orderId, imageUrl)
                    _uiState.update {
                        it.copy(
                            successMessage = "Payment URL updated successfully",
                            showPaymentDialog = false
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to update payment URL: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun updateDPImageUrl(imageUrl: String) {
        val orderId = _uiState.value.currentOrderId
        if (orderId.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }

                    orderRepository.DPPayment(orderId, imageUrl)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Payment completed successfully",
                            showPaymentDialog = false,
                            currentOrderId = ""
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to complete payment: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    fun setShowPaymentDialog(show: Boolean) {
        _uiState.update { it.copy(showPaymentDialog = show) }
    }

    fun setCurrentOrderId(orderId: String) {
        Log.d("ViewModel", "CurrentOrderId: $orderId")
        _uiState.update { it.copy(currentOrderId = orderId) }
    }

    fun dismissPaymentDialog() {
        _uiState.update { it.copy(showPaymentDialog = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clearPromoValidationMessage() {
        _uiState.update { it.copy(promoValidationMessage = null) }
    }
}