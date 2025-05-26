package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepayment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepayment.components.ConfirmPaymentCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePaymentScreen(
    navController: NavController
) {

    val snackbarHostState = remember { SnackbarHostState() }

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
            ManagePaymentContent()
        }
    }
}

@Composable
fun ManagePaymentContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            ConfirmPaymentCard(
                orderData = OrderData(
                    guestName = "Bambang",
                    guestPhone = "0878787878",
                    guestQty = 5,
                    paidAmount = 500000.0,
                    paymentUrls = listOf("https://res.cloudinary.com/dkwbc3had/image/upload/v1748222780/PuriSaeHomestay/upload_4389952778423432799_mz36cf.jpg", "https://res.cloudinary.com/dkwbc3had/image/upload/v1748222780/PuriSaeHomestay/upload_4389952778423432799_mz36cf.jpg"),
                    paymentStatus = PaymentStatusStage.DP
                ),
                paketTitle = "Paket 2 Paket Rombongan"
            )
        }
        item {
            ConfirmPaymentCard(
                orderData = OrderData(
                    guestName = "Agus",
                    guestPhone = "0878787878",
                    guestQty = 15,
                    paidAmount = 500000.0,
                    paymentUrls = listOf("https://res.cloudinary.com/dkwbc3had/image/upload/v1748222780/PuriSaeHomestay/upload_4389952778423432799_mz36cf.jpg", "https://res.cloudinary.com/dkwbc3had/image/upload/v1748222780/PuriSaeHomestay/upload_4389952778423432799_mz36cf.jpg"),
                    paymentStatus = PaymentStatusStage.COMPLETED
                ),
                paketTitle = "Paket 2 Paket Rombongan"
            )
        }
        item {
            ConfirmPaymentCard(
                orderData = OrderData(
                    guestName = "Lukman",
                    guestPhone = "0878787878",
                    guestQty = 10,
                    paidAmount = 500000.0,
                    paymentUrls = listOf("https://res.cloudinary.com/dkwbc3had/image/upload/v1748095611/tmp835219501901363598_gl8vup.jpg", "https://res.cloudinary.com/dkwbc3had/image/upload/v1748222780/PuriSaeHomestay/upload_4389952778423432799_mz36cf.jpg"),
                    paymentStatus = PaymentStatusStage.REJECTED
                ),
                paketTitle = "Paket 2 Paket Rombongan"
            )
        }
        item {
            ConfirmPaymentCard(
                orderData = OrderData(
                    guestName = "Susilo Bambang Yudhoyono Agus Wibowo",
                    guestPhone = "0869420599",
                    guestQty = 3,
                    paidAmount = 1500000.0,
                    paymentUrls = listOf("https://res.cloudinary.com/dkwbc3had/image/upload/v1748222780/PuriSaeHomestay/upload_4389952778423432799_mz36cf.jpg", "https://res.cloudinary.com/dkwbc3had/image/upload/v1748095611/tmp835219501901363598_gl8vup.jpg"),
                    paymentStatus = PaymentStatusStage.COMPLETED
                ),
                paketTitle = "Paket 69 Paket Gangbang"
            )
        }
    }
}