package brawijaya.example.purisaehomestay.ui.screens.order.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.order.components.HistoryCard
import brawijaya.example.purisaehomestay.ui.screens.order.components.PaymentDialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            viewModel.getOrdersByUser("/users/${user.uid}")
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    savedStateHandle?.getLiveData<String>("uploaded_image_url")?.observe(
        LocalLifecycleOwner.current
    ) { imageUrl ->
        imageUrl?.let {
            viewModel.updateDPImageUrl(it)
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
                        text = "Aktivitas",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .padding(start = 2.dp)
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
        }
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
                ActivityContent(
                    orders = uiState.orderList,
                    onShowPaymentDialog = { orderId ->
                        viewModel.setCurrentOrderId(orderId)
                        viewModel.setShowPaymentDialog(true)
                    }
                )
            }

            if (uiState.showPaymentDialog) {
                val currentOrder = uiState.orderList.find { order ->
                    order.documentId == uiState.currentOrderId
                }

                currentOrder?.let { order ->
                    PaymentDialog(
                        order = order,
                        onDismiss = { viewModel.setShowPaymentDialog(false) },
                        onUploadClicked = {
                            navController.navigate(Screen.UploadPayment.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityContent(
    orders: List<OrderData>,
    onShowPaymentDialog: (String) -> Unit
) {
    val inProcessOrders = orders.filter { it.paymentStatus != PaymentStatusStage.COMPLETED }
    val historyOrders = orders.filter { it.paymentStatus === PaymentStatusStage.COMPLETED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (inProcessOrders.isNotEmpty()) {
            item {
                Text(
                    text = "Dalam Proses",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(inProcessOrders) { order ->
                HistoryCard(
                    date = order.check_in.toDate(),
                    paymentStatus = order.paymentStatus,
                    imageUrl = painterResource(id = getImageResourceForOrder(order)),
                    title = getOrderTitle(order),
                    totalPrice = order.totalPrice.toInt(),
                    amountToBePaid = (order.totalPrice - order.paidAmount).toInt(),
                    onButtonClick = {
                        onShowPaymentDialog(order.documentId)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (historyOrders.isNotEmpty()) {
            item {
                Text(
                    text = "Riwayat",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                )
            }

            itemsIndexed(historyOrders) { index, order ->
                HistoryCard(
                    date = order.check_in.toDate(),
                    paymentStatus = order.paymentStatus,
                    imageUrl = painterResource(id = getImageResourceForOrder(order)),
                    title = getOrderTitle(order),
                    totalPrice = order.totalPrice.toInt(),
                )

                if (index < historyOrders.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Text(
                        text = "Belum ada pesanan",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}


@Composable
private fun getImageResourceForOrder(order: OrderData): Int {
    return when {
        order.bungalowQty > 0 && order.jogloQty > 0 -> R.drawable.bungalow_group
        order.bungalowQty > 1 -> R.drawable.bungalow_group
        order.bungalowQty == 1 -> R.drawable.bungalow_single
        order.jogloQty > 0 -> R.drawable.wedding_venue
        else -> R.drawable.bungalow_single
    }
}

private fun getOrderTitle(order: OrderData): String {
    return when {
        order.bungalowQty > 0 && order.jogloQty > 0 -> "Paket Kombinasi (${order.bungalowQty} Bungalow + ${order.jogloQty} Joglo)"
        order.bungalowQty > 1 -> "Paket ${order.bungalowQty} Bungalow"
        order.bungalowQty == 1 -> "Paket 1 Sewa Bungalow"
        order.jogloQty > 0 -> "Paket ${order.jogloQty} Venue Wedding"
        else -> "Paket Homestay"
    }
}