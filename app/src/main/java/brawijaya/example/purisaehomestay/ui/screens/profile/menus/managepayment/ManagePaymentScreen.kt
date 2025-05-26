package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepayment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepayment.components.ConfirmPaymentCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePaymentScreen(
    navController: NavController,
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by orderViewModel.uiState.collectAsState()

    var showCompleteDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf("") }
    var selectedOrderPaymentType by remember { mutableStateOf("") }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            orderViewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            orderViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Kelola Pembayaran",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = PrimaryGold
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ManagePaymentContent(
                    orderList = uiState.orderList,
                    onComplete = { orderId, paymentType ->
                        selectedOrderId = orderId
                        selectedOrderPaymentType = paymentType
                        showCompleteDialog = true
                    },
                    onReject = { orderId ->
                        selectedOrderId = orderId
                        showRejectDialog = true
                    }
                )
            }

            if (showCompleteDialog) {
                GeneralDialog(
                    message = "Apakah Anda yakin untuk menerima pembayaran ini?",
                    onDismiss = {
                        showCompleteDialog = false
                        selectedOrderId = ""
                        selectedOrderPaymentType = ""
                    },
                    onConfirm = {
                        orderViewModel.verifyPayment(selectedOrderId)
                        showCompleteDialog = false
                        selectedOrderId = ""
                        selectedOrderPaymentType = ""
                    }
                )
            }

            if (showRejectDialog) {
                GeneralDialog(
                    message = "Apakah Anda yakin untuk menolak pembayaran ini?",
                    onDismiss = {
                        showRejectDialog = false
                        selectedOrderId = ""
                    },
                    onConfirm = {
                        orderViewModel.rejectPayment(selectedOrderId)
                        showRejectDialog = false
                        selectedOrderId = ""
                    }
                )
            }
        }
    }
}

@Composable
fun ManagePaymentContent(
    orderList: List<brawijaya.example.purisaehomestay.data.model.OrderData>,
    onComplete: (String, String) -> Unit,
    onReject: (String) -> Unit,
) {
    val pendingOrders = orderList.filter { order ->
        order.paymentStatus in listOf(
            PaymentStatusStage.DP,
            PaymentStatusStage.SISA,
            PaymentStatusStage.LUNAS,
            PaymentStatusStage.WAITING,
            PaymentStatusStage.COMPLETED,
            PaymentStatusStage.REJECTED
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(pendingOrders) { order ->
            ConfirmPaymentCard(
                orderData = order,
                paketTitle = "Paket ${order.packageRef}",
                onComplete = {
                    onComplete(order.documentId, order.paymentType)
                },
                onReject = {
                    onReject(order.documentId)
                }
            )
        }
    }
}