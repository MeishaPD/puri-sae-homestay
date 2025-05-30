package brawijaya.example.purisaehomestay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.ProfileUiState
import java.text.NumberFormat
import java.util.Date
import brawijaya.example.purisaehomestay.utils.DateUtils
import brawijaya.example.purisaehomestay.R
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun HistoryCard(
    guestName: String? = null,
    guestPhone: String? = null,
    guestQty: Int? = null,
    profileUiState: ProfileUiState? = null,
    currentRoute: String? = null,
    date: Date,
    paymentStatus: PaymentStatusStage,
    imageUrl: Any,
    title: String,
    totalPrice: Int,
    amountToBePaid: Int? = 0,
    paidAmount: Int? = 0,
    onButtonClick: (() -> Unit)? = null
) {
    val formattedDate = DateUtils.formatDate(date)
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    numberFormat.maximumFractionDigits = 0

    val isPaid = paymentStatus === PaymentStatusStage.COMPLETED
    val isOnVerification =
        paymentStatus === PaymentStatusStage.DP || paymentStatus === PaymentStatusStage.LUNAS || paymentStatus === PaymentStatusStage.SISA

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.bungalow_single),
                error = painterResource(id = R.drawable.bungalow_single)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (profileUiState?.isAdmin == true) {
                    Text(
                        text = "$guestName | $guestPhone | $guestQty Orang",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                        ) {
                            if (isPaid == true) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Paid",
                                    tint = Color.Green
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = "Not Paid",
                                    tint = Color.Red
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isPaid == true) "LUNAS" else "BELUM LUNAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isPaid == true) Color.Green else Color.Red,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Text(
                        text = numberFormat.format(totalPrice).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }

                if ((isPaid == false && amountToBePaid != null) || (isPaid == false && currentRoute == Screen.MonthlyReport.route)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentRoute == Screen.MonthlyReport.route) {
                            Text(
                                text = "Jumlah terverifikasi",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )

                            Text(
                                text = numberFormat.format(paidAmount)
                                    .replace("Rp", "Rp "),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            if (isOnVerification) {
                                Text(
                                    text = "Jumlah yang sedang diverifikasi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )

                                if (paymentStatus === PaymentStatusStage.SISA || paymentStatus === PaymentStatusStage.LUNAS) {
                                    Text(
                                        text = numberFormat.format(amountToBePaid)
                                            .replace("Rp", "Rp "),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                } else {
                                    Text(
                                        text = numberFormat.format(totalPrice * 0.25)
                                            .replace("Rp", "Rp "),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = "Jumlah yang harus dilunasi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Red,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp
                                    ),
                                )

                                Text(
                                    text = numberFormat.format(amountToBePaid).replace("Rp", "Rp "),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (currentRoute != Screen.MonthlyReport.route) {
            if (isPaid == false && onButtonClick != null) {
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGold
                    ),
                    enabled = paymentStatus === PaymentStatusStage.WAITING,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (paymentStatus === PaymentStatusStage.WAITING) "Bayar Sekarang" else "Menunggu Verifikasi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}