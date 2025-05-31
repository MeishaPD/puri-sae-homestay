package brawijaya.example.purisaehomestay.ui.screens.upload

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.ui.components.ImageUploader
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.CloudinaryViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    onImageUploaded: (String) -> Unit = {},
    cloudinaryViewModel: CloudinaryViewModel = hiltViewModel(),
    orderId: String?,
    orderViewModel: OrderViewModel,
    source: String = "order"
) {
    val cloudinaryState by cloudinaryViewModel.uiState.collectAsState()
    val orderState by orderViewModel.uiState.collectAsState()

    var uploadCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(cloudinaryState.imageUrl) {
        cloudinaryState.imageUrl?.let { url ->
            Log.d("UploadScreen", "Image uploaded successfully: $url, source: $source")

            when (source) {
                "order" -> {
                    if (orderState.awaitingPaymentProof) {
                        Log.d("UploadScreen", "Handling payment proof upload for pending order")
                        orderViewModel.handlePaymentProofUpload(url)
                    } else {
                        Log.d("UploadScreen", "Calling onImageUploaded callback")
                        onImageUploaded(url)
                        uploadCompleted = true
                    }
                }
                "activity" -> {
                    Log.d("UploadScreen", "Handling DP payment upload")
                    onImageUploaded(url)
                    uploadCompleted = true
                }
            }
        }
    }

    LaunchedEffect(orderState.successMessage) {
        if (orderState.successMessage?.contains("berhasil dibuat") == true) {
            Log.d("UploadScreen", "Order created successfully, navigating back")
            delay(1500)
            navController.popBackStack()
        }
    }

    LaunchedEffect(uploadCompleted) {
        if (uploadCompleted) {
            Log.d("UploadScreen", "Upload completed, navigating back after delay")
            delay(1500)
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        cloudinaryViewModel.resetState()
        Log.d("UploadScreen", "Source: $source, awaitingPaymentProof: ${orderState.awaitingPaymentProof}")
        Log.d("UploadScreen", "pendingOrderData: ${orderState.pendingOrderData}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Unggah Bukti Pembayaran",
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
                            if (!cloudinaryState.isUploading && !orderState.isCreatingOrder) {
                                when (source) {
                                    "order" -> orderViewModel.cancelPendingOrder()
                                }
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryGold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (source) {
                    "order" -> {
                        if (orderState.awaitingPaymentProof) {
                            "Unggah bukti pembayaran untuk menyelesaikan pesanan"
                        } else {
                            "Pilih bukti pembayaran Anda"
                        }
                    }
                    "activity" -> "Unggah bukti pelunasan pembayaran"
                    else -> "Pilih bukti pembayaran Anda"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ImageUploader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                imageUrl = cloudinaryState.imageUrl,
                onImageUrlChanged = { uri ->
                    cloudinaryViewModel.uploadImage(uri)
                },
                isUploading = cloudinaryState.isUploading
            )

            when {
                cloudinaryState.isUploading -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = PrimaryGold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Mengunggah gambar...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                orderState.isCreatingOrder -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = PrimaryGold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Membuat pesanan...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                cloudinaryState.imageUrl != null -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color.Green,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Upload berhasil!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Green
                        )

                        val statusText = when (source) {
                            "order" -> {
                                if (orderState.awaitingPaymentProof) "Membuat pesanan..."
                                else "Kembali ke halaman sebelumnya..."
                            }
                            "activity" -> "Memproses pembayaran..."
                            else -> "Kembali ke halaman sebelumnya..."
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            val errorMessage = cloudinaryState.errorMessage ?: orderState.errorMessage
            errorMessage?.let { error ->

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                cloudinaryViewModel.resetErrorMessage()
                                orderViewModel.clearMessages()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGold
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Coba Lagi",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryGold.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Petunjuk:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryGold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Pastikan foto bukti pembayaran jelas dan dapat dibaca",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    val instructionText = when (source) {
                        "order" -> {
                            if (orderState.awaitingPaymentProof) {
                                "• Pesanan akan dibuat otomatis setelah upload selesai"
                            } else {
                                "• File akan otomatis terupload setelah dipilih"
                            }
                        }
                        "activity" -> "• Pembayaran akan diproses setelah upload selesai"
                        else -> "• File akan otomatis terupload setelah dipilih"
                    }

                    Text(
                        text = instructionText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "• Anda akan kembali ke halaman sebelumnya setelah proses selesai",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}