package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepackage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPackageScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel(),
    paketId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var packageToDelete by remember { mutableStateOf<Paket?>(null) }

    val paket = uiState.selectedPaket
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    LaunchedEffect(paketId) {
        if (paketId != null && paketId > 0) {
            viewModel.getPaketById(paketId)
        } else {
            viewModel.resetSelectedPaket()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetErrorMessage()
        }
    }

    var title by remember { mutableStateOf("") }
    var weekdayPrice by remember { mutableStateOf("") }
    var weekendPrice by remember { mutableStateOf("") }
    val features = remember { mutableStateListOf<String>() }
    var newFeature by remember { mutableStateOf("") }
    var imageResId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(paket) {
        paket?.let {
            title = it.title
            weekdayPrice = it.weekdayPrice.toString()
            weekendPrice = it.weekendPrice.toString()
            features.clear()
            features.addAll(it.features)
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
                        text = if (paketId != null && paketId > 0) "Edit Paket" else "Tambah Paket Baru",
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
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryGold, RoundedCornerShape(8.dp))
                            .clickable { /* TODO: Implement image picker */ },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageResId != null) {
                            Image(
                                painter = painterResource(id = imageResId!!),
                                contentDescription = "Package Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add Image",
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tambah Gambar",
                                    color = PrimaryGold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Paket") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryGold
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = weekdayPrice,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                weekdayPrice = it
                            }
                        },
                        label = { Text("Harga Weekday (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryGold
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = weekendPrice,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                weekendPrice = it
                            }
                        },
                        label = { Text("Harga Weekend/Holiday (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryGold
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Fitur-fitur Paket",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFeature,
                            onValueChange = { newFeature = it },
                            label = { Text("Tambah Fitur") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                                focusedBorderColor = PrimaryGold
                            ),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (newFeature.isNotBlank()) {
                                    features.add(newFeature.trim())
                                    newFeature = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Tambah Fitur",
                                tint = PrimaryGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (features.isEmpty()) {
                            Text(
                                text = "Belum ada fitur yang ditambahkan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        } else {
                            features.forEachIndexed { index, feature ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = feature,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        IconButton(
                                            onClick = { features.removeAt(index) },
                                            modifier = Modifier.padding(0.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Fitur",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                viewModel.updateErrorMessage("Judul paket tidak boleh kosong")
                                return@Button
                            }

                            if (weekdayPrice.isBlank()) {
                                viewModel.updateErrorMessage("Harga weekday tidak boleh kosong")
                                return@Button
                            }

                            if (features.isEmpty()) {
                                viewModel.updateErrorMessage("Minimal satu fitur harus ditambahkan")
                                return@Button
                            }

                            val defaultImageId = if (paketId == 1) {
                                R.drawable.bungalow_single
                            } else if (paketId == 2) {
                                R.drawable.bungalow_group
                            } else {
                                R.drawable.wedding_venue
                            }

                            val newPaket = Paket(
                                id = paketId ?: ((uiState.packageList.maxOfOrNull { it.id }
                                    ?: 0) + 1),
                                title = title,
                                features = features.toList(),
                                weekdayPrice = weekdayPrice.toDoubleOrNull() ?: 0.0,
                                weekendPrice = weekendPrice.toDoubleOrNull() ?: 0.0,
                                imageUrl = imageResId ?: defaultImageId
                            )

                            if (paketId != null && paketId > 0) {
                                viewModel.updatePackage(newPaket)
                            } else {
                                viewModel.createPackage(newPaket)
                            }
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (paketId != null && paketId > 0) "Simpan Perubahan" else "Tambah Paket",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            packageToDelete = paket
                            showDeleteDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = PrimaryGold
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PrimaryGold)
                    ) {
                        Text(
                            text = "Hapus Paket",
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

    if (showDeleteDialog && packageToDelete != null) {
        GeneralDialog(
            message = "Apakah Anda yakin untuk menghapus paket ini?",
            onDismiss = {
                showDeleteDialog = false
            },
            onConfirm = {
                packageToDelete?.id?.let { viewModel.deletePackage(packageToDelete!!.id) }
                showDeleteDialog = false
                packageToDelete = null
                navController.popBackStack()
            }
        )
    }
}