package brawijaya.example.purisaehomestay.ui.screens.profile.menus.monthlyreport

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.order.components.HistoryCard
import brawijaya.example.purisaehomestay.ui.screens.order.history.getImageResourceForOrder
import brawijaya.example.purisaehomestay.ui.screens.order.history.getOrderTitle
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.ProfileUiState
import brawijaya.example.purisaehomestay.ui.viewmodels.ProfileViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Laporan",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Back",
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
            MonthlyReportContent(
                orderList = uiState.orderList,
                profileState = profileState
            )
        }
    }
}

@Composable
fun MonthlyReportContent(
    orderList: List<OrderData>,
    profileState: ProfileUiState
) {
    var selectedMonth by remember { mutableStateOf("Januari") }
    var selectedYear by remember { mutableStateOf("2025") }

    var showMonthDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }

    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear).map { it.toString() }

    val filteredOrders by remember(orderList, selectedMonth, selectedYear) {
        derivedStateOf {
            val selectedMonthIndex = months.indexOf(selectedMonth) + 1
            val selectedYearInt = selectedYear.toIntOrNull() ?: currentYear

            orderList.filter { order ->
                val calendar = Calendar.getInstance()
                calendar.time = order.createdAt.toDate()
                val orderMonth = calendar.get(Calendar.MONTH) + 1
                val orderYear = calendar.get(Calendar.YEAR)

                orderMonth == selectedMonthIndex && orderYear == selectedYearInt
            }
        }
    }

    val totalPaidAmount by remember(filteredOrders) {
        derivedStateOf {
            filteredOrders.sumOf { it.paidAmount }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Bulan",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                )

                OutlinedTextField(
                    value = selectedMonth,
                    onValueChange = {
                        showMonthDialog = true
                    },
                    readOnly = true,
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryGold,
                        unfocusedLabelColor = Color.LightGray,
                        focusedLabelColor = Color.Black,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledBorderColor = PrimaryGold,
                        disabledTextColor = Color.Black
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showMonthDialog = true }) {
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
                        .padding(top = 8.dp)
                        .clickable {
                            showMonthDialog = true
                        }
                )
            }

            Spacer(Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tahun",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                )

                OutlinedTextField(
                    value = selectedYear,
                    onValueChange = {
                        showYearDialog = true
                    },
                    readOnly = true,
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = PrimaryGold.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryGold,
                        unfocusedLabelColor = Color.LightGray,
                        focusedLabelColor = Color.Black,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledBorderColor = PrimaryGold,
                        disabledTextColor = Color.Black
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showYearDialog = true }) {
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
                        .padding(top = 8.dp)
                        .clickable {
                            showYearDialog = true
                        }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp)
        ) {
            items(filteredOrders) { order ->
                HistoryCard(
                    guestName = order.guestName,
                    guestPhone = order.guestPhone,
                    guestQty = order.guestQty,
                    profileUiState = profileState,
                    currentRoute = Screen.MonthlyReport.route,
                    date = order.createdAt.toDate(),
                    paymentStatus = order.paymentStatus,
                    imageUrl = painterResource(id = getImageResourceForOrder(order)),
                    title = getOrderTitle(order),
                    totalPrice = order.totalPrice.toInt(),
                    amountToBePaid = (order.totalPrice - order.paidAmount).toInt()
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp
                )

            }

            if (filteredOrders.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryGold.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, PrimaryGold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Total Pendapatan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )

                            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                            numberFormat.maximumFractionDigits = 0

                            Text(
                                text = numberFormat.format(totalPaidAmount).replace("Rp", "Rp "),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDarkGreen,
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = "Periode: $selectedMonth $selectedYear",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Gray.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "Tidak ada data pesanan untuk periode $selectedMonth $selectedYear",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showMonthDialog) {
        AlertDialog(
            onDismissRequest = { showMonthDialog = false },
            title = {
                Text(
                    text = "Pilih Bulan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                LazyColumn {
                    itemsIndexed(months) { index, month ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    selectedMonth = month
                                    showMonthDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (month == selectedMonth) {
                                    PrimaryGold.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = if (month == selectedMonth) BorderStroke(
                                1.dp,
                                PrimaryGold
                            ) else null
                        ) {
                            Text(
                                text = month,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (month == selectedMonth) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showMonthDialog = false }
                ) {
                    Text(
                        text = "Tutup",
                        color = PrimaryGold
                    )
                }
            }
        )
    }

    if (showYearDialog) {
        AlertDialog(
            onDismissRequest = { showYearDialog = false },
            title = {
                Text(
                    text = "Pilih Tahun",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                LazyColumn {
                    itemsIndexed(years.reversed()) { index, year ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    selectedYear = year
                                    showYearDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (year == selectedYear) {
                                    PrimaryGold.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = if (year == selectedYear) BorderStroke(
                                1.dp,
                                PrimaryGold
                            ) else null
                        ) {
                            Text(
                                text = year,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (year == selectedYear) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showYearDialog = false }
                ) {
                    Text(
                        text = "Tutup",
                        color = PrimaryGold
                    )
                }
            }
        )
    }
}