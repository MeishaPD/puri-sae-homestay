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
import androidx.navigation.NavHostController
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavHostController,
    onImageUploaded: (String) -> Unit = {},
    cloudinaryViewModel: CloudinaryViewModel = hiltViewModel()
) {
    val cloudinaryState by cloudinaryViewModel.uiState.collectAsState()

    LaunchedEffect(cloudinaryState.imageUrl) {
        cloudinaryState.imageUrl?.let { url ->
            onImageUploaded(url)
            navController.previousBackStackEntry?.savedStateHandle?.set("uploaded_image_url", url)
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        cloudinaryViewModel.resetState()
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
                            if (!cloudinaryState.isUploading) {
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
                text = "Pilih bukti pembayaran Anda",
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

            // Show success message briefly before navigating back
            if (cloudinaryState.imageUrl != null && !cloudinaryState.isUploading) {
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

                    Text(
                        text = "Kembali ke halaman sebelumnya...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            cloudinaryState.errorMessage?.let { error ->
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
                            onClick = { cloudinaryViewModel.resetErrorMessage() },
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

                    Text(
                        text = "• File akan otomatis terupload setelah dipilih",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "• Anda akan kembali ke halaman sebelumnya setelah upload selesai",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}