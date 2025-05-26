package brawijaya.example.purisaehomestay.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.data.repository.OrderRepository
import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val packageList: List<Paket> = emptyList(),
    val selectedPackage: Paket? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasSelectedDateRange: Boolean = false,
    val isCreatingOrder: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val orderList: List<OrderData> = emptyList(),
    val availablePackages: List<Paket> = emptyList(),
    val selectedPaket: Paket? = null,
    val currentOrderId: String = ""
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val packageRepository: PackageRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
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
        }
    }

    fun getPaketById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val paket = packageRepository.getPackageById(id)
                _uiState.update { it.copy(selectedPaket = paket, isLoading = false) }
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasSelectedDateRange = true) }
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val checkIn = dateFormat.parse(checkInDate) ?: throw Exception("Invalid check-in date format")
                val checkOut = dateFormat.parse(checkOutDate) ?: throw Exception("Invalid check-out date format")

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

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val checkIn = dateFormat.parse(checkInDate) ?: throw Exception("Invalid check-in date")
                val checkOut = dateFormat.parse(checkOutDate) ?: throw Exception("Invalid check-out date")
                val guestQty = guestCount.toIntOrNull() ?: throw Exception("Invalid guest count")

                val selectedPackage = _uiState.value.packageList.find { it.id == selectedPackageId }
                    ?: throw Exception("Selected package not found")

                val packageRef = orderRepository.createPackageRef(selectedPackage.id.toString())

                val numberOfNights = ((checkOut.time - checkIn.time) / (1000 * 60 * 60 * 24)).toInt()
                val pricePerNight = selectedPackage.price_weekday
                val totalPrice = pricePerNight * numberOfNights

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
                    totalPrice = totalPrice,
                    paymentStatus = paymentStage,
                    userRef = userRef ?: "",
                    createdAt = Timestamp.now()
                )

                val orderId = orderRepository.createOrder(orderData)
                Log.d("ViewModel", "CurrentOrderId: $orderId")

                _uiState.update {
                    it.copy(
                        currentOrderId = orderId,
                        isCreatingOrder = false,
                        successMessage = "Order created successfully",
                        showPaymentDialog = true
                    )
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
}