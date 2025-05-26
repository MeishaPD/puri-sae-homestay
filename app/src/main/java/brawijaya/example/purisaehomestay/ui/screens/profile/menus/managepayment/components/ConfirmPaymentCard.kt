package brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepayment.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.OrderData
import brawijaya.example.purisaehomestay.data.model.PaymentStatusStage
import brawijaya.example.purisaehomestay.ui.components.ImagePreviewDialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ConfirmPaymentCard(
    orderData: OrderData,
    paketTitle: String,
    onComplete: () -> Unit,
    onReject: () -> Unit,
) {
    var showImagePreview by remember { mutableStateOf(false) }
    var initialImageIndex by remember { mutableIntStateOf(0) }
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    numberFormat.maximumFractionDigits = 0

    val displayAmount = when (orderData.paymentStatus) {
        PaymentStatusStage.DP -> orderData.totalPrice * 0.25
        PaymentStatusStage.SISA -> orderData.totalPrice * 0.75
        PaymentStatusStage.LUNAS -> orderData.totalPrice
        else -> orderData.paidAmount
    }

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "${orderData.guestName} | ${orderData.guestPhone} | ${orderData.guestQty} Orang",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = paketTitle,
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Membayar",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = numberFormat.format(displayAmount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleSmall
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Jenis Pembayaran:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "${orderData.paymentType}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = "Bukti Transfer:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            orderData.paymentUrls.forEachIndexed { index, url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Bukti Transfer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(80.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            initialImageIndex = index
                            showImagePreview = true
                        },
                    placeholder = painterResource(id = R.drawable.bungalow_single),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }
        }
        if (orderData.paymentStatus === PaymentStatusStage.DP || orderData.paymentStatus === PaymentStatusStage.SISA || orderData.paymentStatus === PaymentStatusStage.LUNAS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { onReject() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = PrimaryGold,
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, PrimaryGold),
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text(
                        text = "Tolak",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Button(
                    onClick = { onComplete() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGold,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Konfirmasi Pembayaran",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else if (orderData.paymentStatus === PaymentStatusStage.COMPLETED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Payment Confirmed",
                    tint = Color.Green,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Pembayaran telah dikonfirmasi",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else if (orderData.paymentStatus === PaymentStatusStage.WAITING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = "Waiting for Payment",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Menunggu pelunasan",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else if (orderData.paymentStatus === PaymentStatusStage.REJECTED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    contentDescription = "Payment Rejected",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Pembayaran ditolak",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
    if (showImagePreview) {
        ImagePreviewDialog(
            imageUrls = orderData.paymentUrls,
            initialImageIndex = initialImageIndex,
            onDismiss = { showImagePreview = false }
        )
    }
}