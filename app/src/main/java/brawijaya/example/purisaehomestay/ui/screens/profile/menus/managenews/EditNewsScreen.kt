package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managenews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.components.ImageUploader
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.CloudinaryViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.NewsViewModel
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNewsScreen(
    navController: NavController,
    viewModel: NewsViewModel = hiltViewModel(),
    cldViewModel: CloudinaryViewModel = hiltViewModel(),
    newsId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cldUiState by cldViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var newsToDelete by remember { mutableStateOf<NewsData?>(null) }

    val news = uiState.selectedNews
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage
    val isUploadingImage = cldUiState.isUploading

    LaunchedEffect(newsId) {
        if (!newsId.isNullOrEmpty()) {
            viewModel.getNewsById(newsId)
        } else {
            viewModel.resetSelectedNews()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetErrorMessage()
        }
    }

    var description by remember { mutableStateOf("") }

    LaunchedEffect(news) {
        news?.let {
            description = it.desc
            cldViewModel.setImageUrls(it.imageUrls)
        } ?: run {
            cldViewModel.clearImageUrls()
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
                        text = if (!newsId.isNullOrEmpty()) "Edit Berita" else "Tambah Berita Baru",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Kembali",
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryGold
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Deskripsi Berita",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Black,
                            unfocusedIndicatorColor = Color.Black,
                            cursorColor = PrimaryGold
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Column(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Tambahkan Foto",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        ImageUploader(
                            modifier = Modifier.fillMaxWidth(),
                            imageUrls = cldUiState.imageUrls,
                            onImageUrlsChanged = { uris ->
                                uris.forEach { uri ->
                                    cldViewModel.addImageToList(uri)
                                }
                            },
                            onImageRemoved = { imageUrl ->
                                cldViewModel.removeImageFromList(imageUrl)
                            },
                            isUploading = isUploadingImage,
                            isMultiple = true,
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = PrimaryGold
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Batal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (description.isBlank()) {
                                    viewModel.updateErrorMessage("Deskripsi berita tidak boleh kosong")
                                    return@Button
                                }
                                if (cldUiState.imageUrls.isEmpty()) {
                                    viewModel.updateErrorMessage("Minimal satu gambar harus ditambahkan")
                                    return@Button
                                }

                                val updatedNews = NewsData(
                                    id = newsId ?: "",
                                    desc = description,
                                    imageUrls = cldUiState.imageUrls,
                                    createdAt = news?.createdAt ?: Timestamp.now(),
                                    updatedAt = Timestamp.now()
                                )

                                if (!newsId.isNullOrEmpty()) {
                                    viewModel.updateNews(updatedNews)
                                } else {
                                    viewModel.createNews(updatedNews)
                                }
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = PrimaryGold,
                                contentColor = Color.White

                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Selesai",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (!newsId.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = {
                                newsToDelete = news
                                showDeleteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = PrimaryGold
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryGold)
                        ) {
                            Text(
                                text = "Hapus Berita",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                }
            }
        }
    }

    if (showDeleteDialog && newsToDelete != null) {
        GeneralDialog(
            message = "Apakah Anda yakin untuk menghapus berita ini?",
            onDismiss = {
                showDeleteDialog = false
            },
            onConfirm = {
                newsToDelete?.id?.let { viewModel.deleteNews(it) }
                showDeleteDialog = false
                newsToDelete = null
                navController.popBackStack()
            }
        )
    }
}