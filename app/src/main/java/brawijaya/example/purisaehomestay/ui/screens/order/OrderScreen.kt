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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.components.DateRangePicker
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.order.components.PackageCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    navController: NavController
) {

    val scrollState = rememberScrollState()

    val (checkInDate, setCheckInDate) = remember { mutableStateOf("") }
    val (checkOutDate, setCheckOutDate) = remember { mutableStateOf("") }
    val (guestCount, setGuestCount) = remember { mutableStateOf("") }
    val (selectedPackage, setSelectedPackage) = remember { mutableIntStateOf(1) }

    val (checkOutError, setCheckOutError) = remember { mutableStateOf<String?>(null) }

    val updateCheckInDate = { date: String ->
        setCheckInDate(date)

        if (checkOutDate.isNotEmpty()) {
            if (!DateUtils.isValidCheckOutDate(date, checkOutDate)) {
                setCheckOutError("Tanggal check-out harus setelah tanggal check-in")
            } else {
                setCheckOutError(null)
            }
        }
    }

    val updateCheckOutDate = { date: String ->
        setCheckOutDate(date)

        if (checkInDate.isNotEmpty()) {
            if (!DateUtils.isValidCheckOutDate(checkInDate, date)) {
                setCheckOutError("Tanggal check-out harus setelah tanggal check-in")
            } else {
                setCheckOutError(null)
            }
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
//                    onNavigate = {}
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
                OrderScreenContent(
                    checkInDate = checkInDate,
                    onCheckInDateSelected = updateCheckInDate,
                    checkOutDate = checkOutDate,
                    onCheckOutDateSelected = updateCheckOutDate,
                    guestCount = guestCount,
                    onGuestCountChange = setGuestCount,
                    selectedPackage = selectedPackage,
                    onPackageSelected = setSelectedPackage,
                    checkOutError = checkOutError
                )
            }
        }

    }
}

@Composable
fun OrderScreenContent(
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

    val dropdownExpanded = remember { mutableStateOf(false) }
    val selectedPaymentOption = remember { mutableStateOf("Jenis Pembayaran") }

    val paketList = remember {
        listOf(
            Paket(
                id = 1,
                title = "Sewa Bungalow",
                features = listOf(
                    "2 Lantai",
                    "Kapasitas 4-6 Orang",
                    "Wifi, AC dan Air Panas",
                    "Kolam Renang"
                ),
                weekdayPrice = 500000.0,
                weekendPrice = 550000.0,
                imageUrl = R.drawable.bungalow_single
            ),
            Paket(
                id = 2,
                title = "Paket Rombongan (sampai 20 orang)",
                features = listOf(
                    "3 Bungalow",
                    "Free 3 Ekstra Bed",
                    "Dapur, Wifi, AC dan Air Panas",
                    "Kolam Renang",
                    "Joglo (Karaoke)"
                ),
                weekdayPrice = 2000000.0,
                weekendPrice = 2150000.0,
                imageUrl = R.drawable.bungalow_group
            ),
            Paket(
                id = 3,
                title = "Paket Venue Wedding",
                features = listOf(
                    "Bungalow",
                    "Joglo Utama",
                    "Dapur",
                    "Area Makan",
                    "Kolam Renang"
                ),
                weekdayPrice = 7000000.0,
                weekendPrice = 0.0,
                imageUrl = R.drawable.wedding_venue
            )
        )
    }

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

            paketList.forEachIndexed { index, paket ->
                PackageCard(
                    paket = paket,
                    isSelected = selectedPackage == paket.id,
                    onSelect = { onPackageSelected(paket.id) }
                )

                if (index < paketList.lastIndex) {
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
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                OutlinedTextField(
                    value = selectedPaymentOption.value,
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
                        IconButton(onClick = { dropdownExpanded.value = true }) {
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
                        .clickable { dropdownExpanded.value = true }
                        .padding(bottom = 16.dp)
                )

                DropdownMenu(
                    expanded = dropdownExpanded.value,
                    onDismissRequest = { dropdownExpanded.value = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.TopCenter)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Pembayaran DP 25%")
                        },
                        onClick = {
                            selectedPaymentOption.value = "Pembayaran DP 25%"
                            dropdownExpanded.value = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Pembayaran Lunas")
                        },
                        onClick = {
                            selectedPaymentOption.value = "Pembayaran Lunas"
                            dropdownExpanded.value = false
                        }
                    )
                }
            }

            Button(
                colors = ButtonDefaults.buttonColors(PrimaryGold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                onClick = {  }
            ) {
                Text("Pesan Sekarang",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),)
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