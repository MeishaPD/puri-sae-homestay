package brawijaya.example.purisaehomestay.ui.screens.order

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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.components.DateInputField
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

        if (checkOutDate.isEmpty()) {
            if (DateUtils.isCheckOutBeforeCheckIn(date, checkOutDate)) {
                setCheckOutError("Tanggal check-out tidak boleh sebelum tanggal check-in")
            } else {
                setCheckOutError(null)
            }
        }
    }

    val updateCheckOutDate = { date: String ->
        setCheckOutDate(date)

        if (checkInDate.isEmpty()) {
            if (DateUtils.isCheckOutBeforeCheckIn(checkInDate, date)) {
                setCheckOutError("Tanggal check-out tidak boleh sebelum tanggal check-in")
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
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
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

    val minCheckOutDate = if (checkInDate.isNotEmpty()) {
        DateUtils.getMillisFromDate(checkInDate)
    } else {
        null
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateInputField(
                    label = "Check In",
                    value = checkInDate,
                    onDateSelected = onCheckInDateSelected,
                    modifier = Modifier.weight(1f),
                    minDate = System.currentTimeMillis() - 1000
                )

                DateInputField(
                    label = "Check Out",
                    value = checkOutDate,
                    onDateSelected = onCheckOutDateSelected,
                    modifier = Modifier.weight(1f),
                    minDate = minCheckOutDate ?: (System.currentTimeMillis() - 1000),
                    errorText = checkOutError
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pilih Paket",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PackageCard(
                number = "1",
                title = "Sewa Bungalow",
                features = listOf(
                    "2 Lantai",
                    "Kapasitas 4-6 Orang",
                    "Wifi, AC dan Air Panas",
                    "Kolam Renang"
                ),
                weekdayPrice = "Rp 500.000/malam (weekday)",
                weekendPrice = "Rp 550.000 (weekend/holiday)",
                imageRes = R.drawable.bungalow_single,
                isSelected = selectedPackage == 1,
                onSelect = { onPackageSelected(1) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PackageCard(
                number = "2",
                title = "Paket Rombongan (sampai 20 orang)",
                features = listOf(
                    "3 Bungalow",
                    "Free 3 Ekstra Bed",
                    "Dapur, Wifi, AC dan Air Panas",
                    "Kolam Renang",
                    "Joglo (Karaoke)"
                ),
                weekdayPrice = "Rp 2.000.000/malam (weekday)",
                weekendPrice = "Rp 2.150.000 (weekend/holiday)",
                imageRes = R.drawable.bungalow_group,
                isSelected = selectedPackage == 2,
                onSelect = { onPackageSelected(2) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PackageCard(
                number = "3",
                title = "Paket Venue Wedding",
                features = listOf(
                    "Bungalow",
                    "Joglo Utama",
                    "Dapur",
                    "Area Makan",
                    "Kolam Renang"
                ),
                weekdayPrice = "Rp 7.000.000 ( half day 07.00-14.00 )",
                weekendPrice = "",
                imageRes = R.drawable.wedding_venue,
                isSelected = selectedPackage == 3,
                onSelect = { onPackageSelected(3) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = guestCount,
                onValueChange = onGuestCountChange,
                label = { Text("Jumlah Tamu") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
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
                        color = PrimaryGold
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Button(
                colors = ButtonDefaults.buttonColors(PrimaryGold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                onClick = {  }
            ) {
                Text("Pesan Sekarang")
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
                    text = "2. Cek in minimal jam 14.00 WIB dan telah melunasi biaya sewa, cek out maksimal jam 12.00 WIB.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "3. Cek in atau cek out diluar jam diatas harus dengan perjanjian sebelumnya.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}