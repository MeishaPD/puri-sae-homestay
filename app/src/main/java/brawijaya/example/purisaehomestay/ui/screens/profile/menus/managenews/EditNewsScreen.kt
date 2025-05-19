package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managenews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.NewsViewModel
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNewsScreen(
    navController: NavController,
    viewModel: NewsViewModel = hiltViewModel(),
    newsId: Int? = null
) {

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var newsToDelete by remember { mutableStateOf<NewsData?>(null) }

    val news = uiState.selectedNews
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    LaunchedEffect(newsId) {
        if (newsId != null && newsId > 0) {
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
    var date by remember { mutableStateOf("") }
    var imageResId by remember { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(news) {
        news?.let {
            description = it.description
            date = it.date
            imageResId = it.imageUrl
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
                        text = if (newsId != null && newsId > 0) "Edit Berita" else "Tambah Berita Baru",
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

                    Text(
                        text = "Tambahkan Foto",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 14.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(imageResId) { imageRes ->
                            PhotoItem(
                                imageResId = imageRes,
                                onDeleteClick = {
                                    imageResId = imageResId.filter { it != imageRes }
                                }
                            )
                        }
                        if (imageResId.size < 4) {
                            item {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .fillMaxSize()
                                        .background(color = PrimaryGold)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add Image",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
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
//                                if (date.isBlank()) {
//                                    viewModel.updateErrorMessage("Tanggal berita tidak boleh kosong")
//                                    return@Button
//                                }
                                if (description.isBlank()) {
                                    viewModel.updateErrorMessage("Deskripsi berita tidak boleh kosong")
                                    return@Button
                                }
//                                if (imageResId.isEmpty()) {
//                                    viewModel.updateErrorMessage("Minimal satu gambar harus ditambahkan")
//                                    return@Button
//                                }

                                val defaultImageId = if (newsId == 1) {
                                    listOf(
                                        R.drawable.bungalow_single,
                                        R.drawable.bungalow_group,
                                        R.drawable.landscape_view
                                    )
                                } else if (newsId == 2) {
                                    listOf(
                                        R.drawable.bungalow_group,
                                        R.drawable.bungalow_single
                                    )
                                } else {
                                    listOf(
                                        R.drawable.landscape_view
                                    )
                                }

                                val updatedNews = NewsData(
                                    id = newsId ?: ((uiState.newsList.maxOfOrNull { it.id } ?: 0) + 1),
                                    description = description,
                                    date = if (date.isEmpty()) "12/12/2012" else date,
                                    updatedAt = Timestamp.now(),
                                    imageUrl = if (imageResId.isEmpty()) defaultImageId else imageResId
                                )

                                if (newsId != null && newsId > 0) {
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

                    if (newsId != null && newsId > 0){
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
                newsToDelete?.id?.let { viewModel.deleteNews(newsToDelete!!.id) }
                showDeleteDialog = false
                newsToDelete = null
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun PhotoItem(
    imageResId: Int?,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = painterResource(id = imageResId!!),
            contentDescription = "News Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(50))
                .background(PrimaryGold)
        ) {
            IconButton(
                onClick = onDeleteClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Photo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}