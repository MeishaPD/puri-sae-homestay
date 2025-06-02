package brawijaya.example.purisaehomestay.ui.screens.promo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.ui.components.DateRangePicker
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.components.ImageUploader
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.CloudinaryViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.PromoViewModel
import brawijaya.example.purisaehomestay.utils.DateUtils
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromoScreen(
    navController: NavController,
    viewModel: PromoViewModel = hiltViewModel(),
    cldViewModel: CloudinaryViewModel = hiltViewModel(),
    promoId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val cldUiState by cldViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var promoToDelete by remember { mutableStateOf<PromoData?>(null) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val isEditMode = promoId != null && promoId != ""

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("") }
    var promoCode by remember { mutableStateOf("") }
    var packageRef by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    var isGeneratingCode by remember { mutableStateOf(false) }
    var isCheckingAvailability by remember { mutableStateOf(false) }
    var promoCodeAvailability by remember { mutableStateOf<Boolean?>(null) }
    var lastCheckedCode by remember { mutableStateOf("") }

    val promo = uiState.selectedPromo
    val isLoading = uiState.isLoadingForm
    val errorMessage = uiState.formError
    val isUploadingImage = cldUiState.isUploading
    val cloudinaryImageUrl = cldUiState.imageUrl

    BackHandler(enabled = hasUnsavedChanges) {
        if (hasUnsavedChanges) {
            showCancelDialog = true
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(promoId) {
        if (!promoId.isNullOrEmpty()) {
            viewModel.getPromoById(promoId)
        } else {
            viewModel.resetSelectedPromo()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFormState()
        }
    }

    LaunchedEffect(promo) {
        promo?.let {
            title = it.title
            description = it.description
            promoCode = it.promoCode
            packageRef = it.packageRef
            discountPercentage = it.discountPercentage.toString()
            imageUrl = it.imageUrl
            startDate = DateUtils.formatDate(it.startDate.toDate())
            endDate = DateUtils.formatDate(it.endDate.toDate())
        }
    }

    LaunchedEffect(
        title,
        description,
        startDate,
        endDate,
        discountPercentage,
        promoCode,
        packageRef,
        cloudinaryImageUrl
    ) {
        if (isEditMode && promo != null) {
            hasUnsavedChanges = title != promo.title ||
                    description != promo.description ||
                    startDate != DateUtils.formatDate(promo.startDate.toDate()) ||
                    endDate != DateUtils.formatDate(promo.endDate.toDate()) ||
                    discountPercentage != promo.discountPercentage.toString() ||
                    promoCode != promo.promoCode ||
                    packageRef != promo.packageRef ||
                    cloudinaryImageUrl != null
        } else if (!isEditMode) {
            hasUnsavedChanges = title.isNotBlank() ||
                    description.isNotBlank() ||
                    startDate.isNotBlank() ||
                    endDate.isNotBlank() ||
                    discountPercentage.isNotBlank() ||
                    promoCode.isNotBlank() ||
                    packageRef.isNotBlank() ||
                    cloudinaryImageUrl != null
        }
    }

    LaunchedEffect(uiState.formSuccess) {
        if (uiState.formSuccess) {
            navController.popBackStack()
            viewModel.clearFormState()
        }
    }

    LaunchedEffect(promoCode) {
        if (promoCode.isNotBlank() && promoCode != lastCheckedCode && promoCode.length >= 3) {
            isCheckingAvailability = true
            kotlinx.coroutines.delay(500)

            if (promoCode == promoCode) {
                viewModel.checkReferralCodeAvailability(promoCode.uppercase()) { isAvailable ->
                    val actuallyAvailable = if (isEditMode && promo?.promoCode == promoCode.uppercase()) {
                        true
                    } else {
                        isAvailable
                    }

                    promoCodeAvailability = actuallyAvailable
                    lastCheckedCode = promoCode
                    isCheckingAvailability = false
                }
            } else {
                isCheckingAvailability = false
            }
        } else if (promoCode.isBlank()) {
            promoCodeAvailability = null
            lastCheckedCode = ""
            isCheckingAvailability = false
        }
    }

    fun handleBackNavigation() {
        navController.popBackStack()
    }

    fun generatePromoCode() {
        isGeneratingCode = true
        viewModel.generateUniqueReferralCode { generatedCode ->
            if (generatedCode != null) {
                promoCode = generatedCode
                promoCodeAvailability = true
                lastCheckedCode = generatedCode
            } else {
                viewModel.updateFormErrorMessage("Gagal menghasilkan kode promo. Silakan coba lagi.")
            }
            isGeneratingCode = false
        }
    }

    fun validateAndSavePromo() {
        val currentImageUrl = cloudinaryImageUrl ?: imageUrl
        val startDateParsed = DateUtils.parseDate(startDate)
        val endDateParsed = DateUtils.parseDate(endDate)

        if (currentImageUrl.isNullOrBlank()) {
            viewModel.updateFormErrorMessage("Gambar promo harus dipilih")
            return
        }

        if (title.isBlank()) {
            viewModel.updateFormErrorMessage("Judul promo tidak boleh kosong")
            return
        }

        if (description.isBlank()) {
            viewModel.updateFormErrorMessage("Deskripsi promo tidak boleh kosong")
            return
        }

        if (promoCode.isBlank()) {
            viewModel.updateFormErrorMessage("Kode promo tidak boleh kosong")
            return
        }

        if (promoCodeAvailability == false) {
            viewModel.updateFormErrorMessage("Kode promo sudah digunakan. Silakan gunakan kode lain.")
            return
        }

        val discountPercentageValue = discountPercentage.toDoubleOrNull()
        if (discountPercentageValue == null || discountPercentageValue <= 0 || discountPercentageValue > 100) {
            viewModel.updateFormErrorMessage("Persentase diskon harus antara 1-100%")
            return
        }

        if (startDate.isBlank() || endDate.isBlank()) {
            viewModel.updateFormErrorMessage("Tanggal mulai dan berakhir harus dipilih")
            return
        }

        if (startDateParsed == null || endDateParsed == null) {
            viewModel.updateFormErrorMessage("Format tanggal tidak valid")
            return
        }

        try {
            val startTimestamp = Timestamp(startDateParsed)
            val endTimestamp = Timestamp(endDateParsed)

            if (startTimestamp.toDate().after(endTimestamp.toDate())) {
                viewModel.updateFormErrorMessage("Tanggal mulai tidak boleh setelah tanggal berakhir")
                return
            }

            if (isEditMode && promo != null) {
                val updatedPromo = promo.copy(
                    title = title.trim(),
                    description = description.trim(),
                    startDate = startTimestamp,
                    endDate = endTimestamp,
                    discountPercentage = discountPercentageValue,
                    promoCode = promoCode.trim().uppercase(),
                    packageRef = packageRef.trim(),
                    imageUrl = currentImageUrl
                )
                viewModel.updatePromo(updatedPromo)
            } else {
                viewModel.createPromo(
                    title = title.trim(),
                    description = description.trim(),
                    startDate = startTimestamp,
                    endDate = endTimestamp,
                    discountPercentage = discountPercentageValue,
                    promoCode = promoCode.trim().uppercase(),
                    packageRef = packageRef.trim(),
                    imageUrl = currentImageUrl
                )
            }
        } catch (e: Exception) {
            viewModel.updateFormErrorMessage("Format tanggal tidak valid")
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
                        text = if (isEditMode) "Edit Promo" else "Tambah Promo Baru",
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
                        .verticalScroll(rememberScrollState())
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Promo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryGold
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi Promo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryGold
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = promoCode,
                        onValueChange = {
                            promoCode = it.uppercase()
                            promoCodeAvailability = null
                        },
                        label = { Text("Kode Promo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = when (promoCodeAvailability) {
                                true -> Color.Green.copy(alpha = 0.7f)
                                false -> Color.Red.copy(alpha = 0.7f)
                                null -> PrimaryGold.copy(alpha = 0.5f)
                            },
                            focusedBorderColor = when (promoCodeAvailability) {
                                true -> Color.Green
                                false -> Color.Red
                                null -> PrimaryGold
                            }
                        ),
                        singleLine = true,
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                if (promoCode.isNotBlank() && promoCode.length >= 3) {
                                    when {
                                        isCheckingAvailability -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = PrimaryGold,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                        promoCodeAvailability == true -> {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = "Available",
                                                tint = Color.Green,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        promoCodeAvailability == false -> {
                                            Icon(
                                                imageVector = Icons.Rounded.Cancel,
                                                contentDescription = "Not Available",
                                                tint = Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Button(
                                    onClick = { generatePromoCode() },
                                    enabled = !isGeneratingCode && !isLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryGold.copy(alpha = 0.1f),
                                        contentColor = PrimaryGold
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    if (isGeneratingCode) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = PrimaryGold,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Generate",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        placeholder = { Text("Contoh: DISKON50") },
                        supportingText = {
                            when {
                                promoCode.isNotBlank() && promoCode.length < 3 -> {
                                    Text(
                                        text = "Kode promo minimal 3 karakter",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                promoCodeAvailability == false -> {
                                    Text(
                                        text = "Kode promo sudah digunakan",
                                        color = Color.Red,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                promoCodeAvailability == true -> {
                                    Text(
                                        text = "Kode promo tersedia",
                                        color = Color.Green,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Klik 'Generate' untuk membuat kode otomatis",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = discountPercentage,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                discountPercentage = it
                            }
                        },
                        label = { Text("Persentase Diskon (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryGold
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("Contoh: 25") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DateRangePicker(
                        startDate = startDate,
                        onStartDateSelected = { startDate = it },
                        endDate = endDate,
                        onEndDateSelected = { endDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        minStartDate = System.currentTimeMillis() - 1000,
                        errorMessage = null,
                        isEditPromo = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (hasUnsavedChanges) {
                                    showCancelDialog = true
                                } else {
                                    navController.popBackStack()
                                }
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
                            onClick = { validateAndSavePromo() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGold,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading && !isUploadingImage && !isGeneratingCode &&
                                    promoCodeAvailability != false &&
                                    (promoCode.isBlank() || promoCode.length >= 3)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(16.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = if (isEditMode) "Update" else "Simpan",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }

                    if (isEditMode) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                promoToDelete = promo
                                showDeleteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Red
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text(
                                text = "Hapus Promo",
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

    if (showDeleteDialog && promoToDelete != null) {
        GeneralDialog(
            message = "Apakah Anda yakin untuk menghapus promo ini?",
            onDismiss = {
                showDeleteDialog = false
                promoToDelete = null
            },
            onConfirm = {
                promoToDelete?.id?.let { viewModel.deletePromo(it) }
                showDeleteDialog = false
                promoToDelete = null
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