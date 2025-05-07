package brawijaya.example.purisaehomestay.ui.screens.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.components.DateRangePicker
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.order.components.PackageCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel
import brawijaya.example.purisaehomestay.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var checkInDate by remember { mutableStateOf("") }
    var checkOutDate by remember { mutableStateOf("") }
    var guestCount by remember { mutableStateOf("") }
    var selectedPackageId by remember { mutableIntStateOf(1) }
    var checkOutError by remember { mutableStateOf<String?>(null) }

    val packageList = uiState.packageList

    if (uiState.selectedPaket == null) {
        viewModel.getPaketById(selectedPackageId)
    }

    val updateCheckInDate = { date: String ->
        checkInDate = date

        if (checkOutDate.isNotEmpty()) {
            checkOutError = if (!DateUtils.isValidCheckOutDate(date, checkOutDate)) {
                "Tanggal check-out harus setelah tanggal check-in"
            } else {
                null
            }
        }
    }

    val updateCheckOutDate = { date: String ->
        checkOutDate = date

        if (checkInDate.isNotEmpty()) {
            checkOutError = if (!DateUtils.isValidCheckOutDate(checkInDate, date)) {
                "Tanggal check-out harus setelah tanggal check-in"
            } else {
                null
            }
        }
    }

    val onPackageSelected = { id: Int ->
        selectedPackageId = id
        viewModel.getPaketById(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Pemesanan",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Activities.route)
                    }) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            painter = painterResource(id = R.drawable.overview),
                            contentDescription = "Riwayat Pemesanan",
                            tint = PrimaryGold
                        )
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BottomNavigation(
                    currentRoute = Screen.Order.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
            ) {
                // Show loading or error states if needed
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading...")
                    }
                }

                uiState.errorMessage?.let { errorMsg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMsg,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                OrderScreenContent(
                    packageList = packageList,
                    checkInDate = checkInDate,
                    onCheckInDateSelected = updateCheckInDate,
                    checkOutDate = checkOutDate,
                    onCheckOutDateSelected = updateCheckOutDate,
                    guestCount = guestCount,
                    onGuestCountChange = { guestCount = it },
                    selectedPackage = selectedPackageId,
                    onPackageSelected = onPackageSelected,
                    checkOutError = checkOutError
                )
            }
        }
    }
}

@Composable
fun OrderScreenContent(
    packageList: List<Paket>,
    checkInDate: String,
    onCheckInDateSelected: (String) -> Unit,
    checkOutDate: String,
    onCheckOutDateSelected: (String) -> Unit,
    guestCount: String,
    onGuestCountChange: (String) -> Unit,
    selectedPackage: Int,
    onPackageSelected: (Int) -> Unit,
    checkOutError: String? = null
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedPaymentOption by remember { mutableStateOf("Jenis Pembayaran") }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            DateRangePicker(
                checkInDate = checkInDate,
                onCheckInDateSelected = onCheckInDateSelected,
                checkOutDate = checkOutDate,
                onCheckOutDateSelected = onCheckOutDateSelected,
                modifier = Modifier.fillMaxWidth(),
                minCheckInDate = System.currentTimeMillis() - 1000,
                checkOutError = checkOutError
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pilih Paket",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Use the packages from the ViewModel
            packageList.forEachIndexed { index, paket ->
                PackageCard(
                    paket = paket,
                    isSelected = selectedPackage == paket.id,
                    onSelect = { onPackageSelected(paket.id) }
                )

                if (index < packageList.lastIndex) {
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = guestCount,
                onValueChange = onGuestCountChange,
                label = { Text("Jumlah Tamu", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Jumlah Tamu",
                        tint = PrimaryGold
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                    focusedBorderColor = PrimaryGold,
                    unfocusedLabelColor = Color.LightGray,
                    focusedLabelColor = Color.Black,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = {
                    Text(
                        modifier = Modifier
                            .padding(end = 16.dp),
                        text = "Orang",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryGold
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                OutlinedTextField(
                    value = selectedPaymentOption,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Jenis Pembayaran", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = "Jenis Pembayaran",
                            tint = PrimaryGold
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryGold,
                        unfocusedLabelColor = Color.LightGray,
                        focusedLabelColor = Color.Black,
                        disabledBorderColor = PrimaryGold.copy(alpha = 0.5f),
                        disabledLabelColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = PrimaryGold
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dropdownExpanded = true }
                        .padding(bottom = 16.dp)
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.TopCenter)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Pembayaran DP 25%")
                        },
                        onClick = {
                            selectedPaymentOption = "Pembayaran DP 25%"
                            dropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Pembayaran Lunas")
                        },
                        onClick = {
                            selectedPaymentOption = "Pembayaran Lunas"
                            dropdownExpanded = false
                        }
                    )
                }
            }

            Button(
                colors = ButtonDefaults.buttonColors(PrimaryGold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                onClick = { /* Create order logic here */ }
            ) {
                Text(
                    "Pesan Sekarang",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.LightGray
            )

            Text(
                text = "Catatan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "1. Reservasi wajib DP minimal 25% dari harga paket atau bisa langsung full payment.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "2. Check-in minimal jam 14.00 WIB dan telah melunasi biaya sewa, check-out maksimal jam 12.00 WIB.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "3. Check-in atau check-out di luar jam di atas harus dengan perjanjian sebelumnya.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}