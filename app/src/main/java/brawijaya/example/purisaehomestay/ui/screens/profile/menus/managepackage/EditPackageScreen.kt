package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepackage

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.PackageData
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.components.ImageUploader
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.CloudinaryViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.PackageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPackageScreen(
    navController: NavController,
    viewModel: PackageViewModel = hiltViewModel(),
    cldViewModel: CloudinaryViewModel = hiltViewModel(),
    paketId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cldUiState by cldViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var packageToDelete by remember { mutableStateOf<PackageData?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    var bungalowQtyDropdownExpanded by remember { mutableStateOf(false) }
    var jogloQtyDropdownExpanded by remember { mutableStateOf(false) }

    val paket = uiState.selectedPackageData
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage
    val isUploadingImage = cldUiState.isUploading
    val cloudinaryImageUrl = cldUiState.imageUrl

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    val isEditMode = paketId != null && paketId != 0 && paketId != -1

    LaunchedEffect(paketId) {
        if (isEditMode) {
            Log.d("PAKET", "PAKET ID $paketId")
            viewModel.getPaketById(paketId)
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
    var bungalowQty by remember { mutableIntStateOf(1) }
    var jogloQty by remember { mutableIntStateOf(0) }
    val features = remember { mutableStateListOf<String>() }
    var newFeature by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(paket) {
        paket?.let {
            title = it.title
            weekdayPrice = it.price_weekday.toString()
            weekendPrice = it.price_weekend.toString()
            features.clear()
            features.addAll(it.features)
            jogloQty = it.jogloQty
            bungalowQty = it.bungalowQty
            imageUrl = it.thumbnail_url
        }
    }

    LaunchedEffect(
        title,
        weekdayPrice,
        weekendPrice,
        bungalowQty,
        jogloQty,
        features.size,
        cloudinaryImageUrl
    ) {
        if (isEditMode && paket != null) {
            hasUnsavedChanges = title != paket.title ||
                    weekdayPrice != paket.price_weekday.toString() ||
                    weekendPrice != paket.price_weekend.toString() ||
                    bungalowQty != paket.bungalowQty ||
                    jogloQty != paket.jogloQty ||
                    features.toList() != paket.features ||
                    cloudinaryImageUrl != null
        } else if (!isEditMode) {
            hasUnsavedChanges = title.isNotBlank() ||
                    weekdayPrice.isNotBlank() ||
                    weekendPrice.isNotBlank() ||
                    features.isNotEmpty() ||
                    cloudinaryImageUrl != null
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        if (hasUnsavedChanges) {
            showCancelDialog = true
        } else {
            navController.popBackStack()
        }
    }

//    DisposableEffect(Unit) {
//        onDispose {
//           if (uiState.pendingImageForDeletion != null && !hasUnsavedChanges) {
//              viewModel.cleanupPendingImage()
//            }
//        }
//    }

    fun handleBackNavigation() {
//        if (uiState.pendingImageForDeletion != null) {
//            viewModel.cleanupPendingImage()
//        }
        navController.popBackStack()
    }

    fun validateAndSavePackage() {
        val currentImageUrl = cloudinaryImageUrl ?: imageUrl
        if (currentImageUrl.isNullOrBlank()) {
            viewModel.updateErrorMessage("Gambar paket harus dipilih")
            return
        }

        if (title.isBlank()) {
            viewModel.updateErrorMessage("Judul paket tidak boleh kosong")
            return
        }

        if (weekdayPrice.isBlank()) {
            viewModel.updateErrorMessage("Harga weekday tidak boleh kosong")
            return
        }

        if (weekendPrice.isBlank()) {
            viewModel.updateErrorMessage("Harga weekend tidak boleh kosong")
            return
        }

        if (features.isEmpty()) {
            viewModel.updateErrorMessage("Minimal satu fitur harus ditambahkan")
            return
        }

        val newPackageData = PackageData(
            id = paketId ?: (uiState.packageList.maxOfOrNull { it.id } ?: 0),
            title = title,
            features = features.toList(),
            price_weekday = weekdayPrice.toDoubleOrNull() ?: 0.0,
            price_weekend = weekendPrice.toDoubleOrNull() ?: 0.0,
            thumbnail_url = currentImageUrl,
            jogloQty = jogloQty.toInt(),
            bungalowQty = bungalowQty.toInt()
        )

        if (isEditMode) {
            viewModel.updatePackage(newPackageData)
        } else {
            viewModel.createPackage(newPackageData)
        }

//        viewModel.markImageAsSaved()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = if (isEditMode) "Edit Paket" else "Tambah Paket Baru",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showCancelDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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

                    ImageUploader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        imageUrl = cloudinaryImageUrl ?: imageUrl,
                        placeHolderResId = null,
                        onImageUrlChanged = { uri ->
                            cldViewModel.uploadImage(uri)
                        },
                        isUploading = isUploadingImage
                    )

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = bungalowQty.toString(),
                                onValueChange = {},
                                label = { Text("Jumlah Bungalow") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        bungalowQtyDropdownExpanded = true
                                    },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                                    focusedBorderColor = PrimaryGold,
                                    disabledBorderColor = PrimaryGold,
                                    disabledTextColor = Color.Black,
                                    disabledLabelColor = Color.Black
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                readOnly = true,
                                enabled = false,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            bungalowQtyDropdownExpanded = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            )

                            DropdownMenu(
                                expanded = bungalowQtyDropdownExpanded,
                                onDismissRequest = { bungalowQtyDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("1") },
                                    onClick = {
                                        bungalowQty = 1
                                        bungalowQtyDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("2") },
                                    onClick = {
                                        bungalowQty = 2
                                        bungalowQtyDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("3") },
                                    onClick = {
                                        bungalowQty = 3
                                        bungalowQtyDropdownExpanded = false
                                    }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = jogloQty.toString(),
                                onValueChange = {},
                                label = { Text("Jumlah Joglo") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        jogloQtyDropdownExpanded = true
                                    },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                                    focusedBorderColor = PrimaryGold,
                                    disabledBorderColor = PrimaryGold,
                                    disabledTextColor = Color.Black,
                                    disabledLabelColor = Color.Black
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                readOnly = true,
                                enabled = false,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            jogloQtyDropdownExpanded = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            )

                            DropdownMenu(
                                expanded = jogloQtyDropdownExpanded,
                                onDismissRequest = { jogloQtyDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("0") },
                                    onClick = {
                                        jogloQty = 0
                                        jogloQtyDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("1") },
                                    onClick = {
                                        jogloQty = 1
                                        jogloQtyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

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
                                    border = BorderStroke(1.dp, PrimaryGold)
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
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Hapus Fitur",
                                                tint = PrimaryGold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { validateAndSavePackage() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "Simpan Perubahan" else "Tambah Paket",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (isEditMode) {
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
    }

    if (showDeleteDialog && packageToDelete != null) {
        GeneralDialog(
            message = "Apakah Anda yakin untuk menghapus paket ini?",
            onDismiss = {
                showDeleteDialog = false
            },
            onConfirm = {
                packageToDelete?.id?.let { viewModel.deletePackage(it) }
                showDeleteDialog = false
                packageToDelete = null
                navController.popBackStack()
            }
        )
    }

    if (showCancelDialog) {
        GeneralDialog(
            message = if (hasUnsavedChanges) {
                "Anda memiliki perubahan yang belum disimpan. Apakah Anda yakin ingin keluar?"
            } else {
                "Apakah Anda yakin ingin membatalkan?"
            },
            onDismiss = {
                showCancelDialog = false
            },
            onConfirm = {
                showCancelDialog = false
                handleBackNavigation()
            }
        )
    }
}