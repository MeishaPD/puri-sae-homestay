package brawijaya.example.purisaehomestay.ui.screens.order.components

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentDialog(
    totalPrice: Double,
    paidAmount: Double,
    remainingAmount: Double,
    onDismiss: () -> Unit,
    onUploadClicked: () -> Unit,
    discountAmount: Double? = null
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    numberFormat.maximumFractionDigits = 0

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val accountNumber = "3571357118"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Rincian Pembayaran",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "Pembayaran ke Bank Syariah Indonesia (BSI)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nomor Rekening",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(accountNumber))

                            Toast.makeText(context, "Nomor rekening disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy Number"
                        )
                    }

                    Text(
                        text = accountNumber,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Atas nama",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Wanudya Esty Wury",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Total Harga",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = numberFormat.format(totalPrice).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Jumlah yang sudah dibayar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = numberFormat.format(paidAmount).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Jumlah yang harus dilunasi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = numberFormat.format(remainingAmount).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                    )
                }

                if (discountAmount != null && discountAmount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Anda hemat sebesar",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = numberFormat.format(discountAmount).replace("Rp", "Rp "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = numberFormat.format(remainingAmount).replace("Rp", "Rp "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryGold
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onUploadClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGold
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Unggah Bukti Pembayaran",
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