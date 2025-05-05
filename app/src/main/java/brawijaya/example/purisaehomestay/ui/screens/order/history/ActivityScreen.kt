package brawijaya.example.purisaehomestay.ui.screens.order.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.Order
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.order.components.HistoryCard
import brawijaya.example.purisaehomestay.ui.screens.order.components.PaymentDialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    navController: NavController,

) {
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
                            navController.navigate(Screen.Order.route)
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            ActivityContent()
        }
    }
}

@Composable
fun ActivityContent() {
    var showPaymentDialog by remember { mutableStateOf(false) }

    val inProcessOrder = remember {
        Order(
            date = DateUtils.parseDate("18/04/2025") ?: DateUtils.getCurrentDate(),
            isPaid = false,
            title = "Paket 2 Paket Rombongan",
            totalPrice = 500000,
            amountToBePaid = 1500000,
            imageResId = R.drawable.bungalow_group
        )
    }

    val historyOrders = remember {
        listOf(
            Order(
                date = DateUtils.parseDate("24/03/2025") ?: DateUtils.getCurrentDate(),
                isPaid = true,
                title = "Paket 1 Sewa Bungalow",
                totalPrice = 500000,
                imageResId = R.drawable.bungalow_single
            ),
            Order(
                date = DateUtils.parseDate("16/03/2025") ?: DateUtils.getCurrentDate(),
                isPaid = true,
                title = "Paket 3 Paket Venue Wedding",
                totalPrice = 7000000,
                imageResId = R.drawable.wedding_venue
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dalam Proses",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HistoryCard(
            date = inProcessOrder.date,
            isPaid = inProcessOrder.isPaid,
            imageUrl = painterResource(id = inProcessOrder.imageResId),
            title = inProcessOrder.title,
            totalPrice = inProcessOrder.totalPrice,
            amountToBePaid = inProcessOrder.amountToBePaid,
            onButtonClick = {
                showPaymentDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Riwayat",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
        )

        historyOrders.forEachIndexed { index, order ->
            HistoryCard(
                date = order.date,
                isPaid = order.isPaid,
                imageUrl = painterResource(id = order.imageResId),
                title = order.title,
                totalPrice = order.totalPrice
            )

            if (index < historyOrders.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp
                )
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            order = inProcessOrder,
            onDismiss = { showPaymentDialog = false },
            onUploadClicked = {
                showPaymentDialog = false
            }
        )
    }
}