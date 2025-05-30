package brawijaya.example.purisaehomestay.ui.screens.order.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import java.text.NumberFormat
import java.util.Date
import brawijaya.example.purisaehomestay.utils.DateUtils
import java.util.Locale

@Composable
fun HistoryCard(
    date: Date,
    isPaid: Boolean? = true,
    imageUrl: Painter,
    title: String,
    totalPrice: Int,
    amountToBePaid: Int? = null,
    onButtonClick: (() -> Unit)? = null
) {
    val formattedDate = DateUtils.formatDate(date)
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    numberFormat.maximumFractionDigits = 0

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
            Image(
                painter = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )

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

                if (isPaid == false && amountToBePaid != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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

        if (isPaid == false && onButtonClick != null ) {
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGold
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Bayar Sekarang",
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