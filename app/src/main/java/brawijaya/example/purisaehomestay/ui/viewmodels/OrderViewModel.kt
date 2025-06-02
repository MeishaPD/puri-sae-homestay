package brawijaya.example.purisaehomestay.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PackageData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.data.repository.OrderRepository
import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import brawijaya.example.purisaehomestay.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingOrderData(
    val checkInDate: String,
    val checkOutDate: String,
    val guestName: String,
    val guestPhone: String,
    val guestCount: String,
    val selectedPackageId: Int,
    val paymentType: String,
    val userRef: String?,
    val promoCode: String?
)

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
    val promoValidationMessage: String? = null,
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val pendingOrderData: PendingOrderData? = null,
    val awaitingPaymentProof: Boolean = false
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
            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasSelectedDateRange = true,
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate
                )
            }
            try {
                val availablePackages = orderRepository.getAvailablePackages(checkInDate, checkOutDate)
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
                        finalPrice = originalPrice,
                        checkInDate = checkInDate,
                        checkOutDate = checkOutDate
                    )
                }

                val currentState = _uiState.value
                if (currentState.appliedPromo != null && currentState.promoCode.isNotEmpty()) {
                    validatePromoCode(currentState.promoCode, selectedPackageId)
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
                val checkInDate = _uiState.value.checkInDate
                val checkOutDate = _uiState.value.checkOutDate

                val promoResult = orderRepository.validatePromoAndCalculatePrice(
                    promoCode = promoCode,
                    originalPrice = originalPrice,
                    packageRef = packageRef,
                    checkInDateString = checkInDate,
                    checkOutDateString = checkOutDate
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

    fun prepareOrderForPayment(
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
                guestCount.toIntOrNull() ?: throw Exception("Invalid guest count")

                if (!DateUtils.isValidCheckOutDate(checkInDate, checkOutDate)) {
                    throw Exception("Check-out date must be after check-in date")
                }

                _uiState.value.packageList.find { it.id == selectedPackageId } ?: throw Exception("Selected package not found")

                val currentState = _uiState.value

                val pendingOrder = PendingOrderData(
                    checkInDate = checkInDate,
                    checkOutDate = checkOutDate,
                    guestName = guestName,
                    guestPhone = guestPhone,
                    guestCount = guestCount,
                    selectedPackageId = selectedPackageId,
                    paymentType = paymentType,
                    userRef = userRef,
                    promoCode = if (currentState.appliedPromo != null) currentState.promoCode else null
                )

                _uiState.update {
                    it.copy(
                        pendingOrderData = pendingOrder,
                        awaitingPaymentProof = true,
                        showPaymentDialog = true,
                        successMessage = "Silakan lakukan pembayaran dan unggah bukti pembayaran",
                        errorMessage = null
                    )
                }

                Log.d("OrderViewModel", "Order prepared for payment. Awaiting payment proof...")

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message,
                        awaitingPaymentProof = false,
                        showPaymentDialog = false
                    )
                }
            }
        }
    }

    fun createOrderAfterPayment(paymentProofUrl: String) {
        val pendingOrder = _uiState.value.pendingOrderData
        if (pendingOrder == null) {
            _uiState.update {
                it.copy(errorMessage = "No pending order data found")
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCreatingOrder = true) }

                val selectedPackage = _uiState.value.packageList.find { it.id == pendingOrder.selectedPackageId }
                    ?: throw Exception("Selected package not found")

                Log.d("OrderViewModel", "Creating order with payment proof: $paymentProofUrl")

                val result = orderRepository.createOrderWithDateStrings(
                    checkInDateString = pendingOrder.checkInDate,
                    checkOutDateString = pendingOrder.checkOutDate,
                    guestName = pendingOrder.guestName,
                    guestPhone = pendingOrder.guestPhone,
                    guestQty = pendingOrder.guestCount.toInt(),
                    selectedPackage = selectedPackage,
                    paymentType = pendingOrder.paymentType,
                    userRef = pendingOrder.userRef ?: "",
                    promoCode = pendingOrder.promoCode
                )

                if (result.success && result.orderId != null) {
                    orderRepository.updatePaymentUrl(result.orderId, paymentProofUrl)

                    Log.d("OrderViewModel", "Order created successfully with ID: ${result.orderId}")

                    _uiState.update {
                        it.copy(
                            currentOrderId = result.orderId,
                            isCreatingOrder = false,
                            awaitingPaymentProof = false,
                            pendingOrderData = null,
                            successMessage = "Pesanan berhasil dibuat dengan bukti pembayaran!",
                            showPaymentDialog = false,
                            errorMessage = null
                        )
                    }
                } else {
                    throw Exception(result.errorMessage ?: "Failed to create order")
                }

            } catch (e: Exception) {
                Log.e("OrderViewModel", "Failed to create order: ${e.message}")
                _uiState.update {
                    it.copy(
                        isCreatingOrder = false,
                        errorMessage = "Gagal membuat pesanan: ${e.message}"
                    )
                }
            }
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
        Log.d("OrderViewModel", "Starting order creation process...")
        prepareOrderForPayment(
            checkInDate, checkOutDate, guestName, guestPhone,
            guestCount, selectedPackageId, paymentType, userRef
        )
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

    /**
     * Handle payment proof upload (main entry point from upload screen)
     */
    fun handlePaymentProofUpload(imageUrl: String) {
        Log.d("OrderViewModel", "Payment proof uploaded: $imageUrl")

        if (_uiState.value.awaitingPaymentProof) {
            Log.d("OrderViewModel", "Creating order: $imageUrl")
            createOrderAfterPayment(imageUrl)
        } else {
            // Legacy behavior for existing orders (fallback)
            Log.d("OrderViewModel", "Updating order payment: $imageUrl")
            updateExistingOrderPayment(imageUrl)
        }
    }

    /**
     * Handle DP payment upload
     */
    fun handleDPPaymentUpload(imageUrl: String) {
        Log.d("OrderViewModel", "DP payment proof uploaded: $imageUrl")

        if (_uiState.value.awaitingPaymentProof) {
            // Create order with the uploaded DP payment proof
            createOrderAfterPayment(imageUrl)
        } else {
            // Legacy behavior for existing orders
            updateExistingDPPayment(imageUrl)
        }
    }

    /**
     * Legacy method for updating existing orders (kept for backward compatibility)
     */
    private fun updateExistingOrderPayment(imageUrl: String) {
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

    /**
     * Legacy method for updating existing DP payments
     */
    private fun updateExistingDPPayment(imageUrl: String) {
        val orderId = _uiState.value.currentOrderId
        if (orderId.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }

                    orderRepository.DPPayment(orderId, imageUrl)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "DP Payment completed successfully",
                            showPaymentDialog = false,
                            currentOrderId = ""
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to complete DP payment: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    // DEPRECATED: Use handlePaymentProofUpload instead
    @Deprecated("Use handlePaymentProofUpload instead")
    fun updatePaymentUrl(imageUrl: String) {
        handlePaymentProofUpload(imageUrl)
    }

    @Deprecated("Use handleDPPaymentUpload instead")
    fun updateDPImageUrl(imageUrl: String) {
        handleDPPaymentUpload(imageUrl)
    }

    fun setShowPaymentDialog(show: Boolean) {
        _uiState.update { it.copy(showPaymentDialog = show) }
    }

    fun setCurrentOrderId(orderId: String) {
        Log.d("ViewModel", "CurrentOrderId: $orderId")
        _uiState.update { it.copy(currentOrderId = orderId) }
    }

    fun dismissPaymentDialog() {
        _uiState.update {
            it.copy(
                showPaymentDialog = false,
                errorMessage = null
            )
        }
    }

    fun cancelPendingOrder() {
        _uiState.update {
            it.copy(
                showPaymentDialog = false,
                awaitingPaymentProof = false,
                pendingOrderData = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun isAwaitingPaymentProof(): Boolean {
        return _uiState.value.awaitingPaymentProof
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clearPromoValidationMessage() {
        _uiState.update { it.copy(promoValidationMessage = null) }
    }
}