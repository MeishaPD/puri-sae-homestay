package brawijaya.example.purisaehomestay.ui.screens.promo.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.ui.theme.GoldAccent
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.utils.DateUtils
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.Timestamp

@Composable
fun PromoCard(
    promoData: PromoData,
    onEditClick: ((PromoData) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val startDate = DateUtils.formatDate(promoData.startDate.toDate(), "dd MMMM yyyy")
    val endDate = DateUtils.formatDate(promoData.endDate.toDate(), "dd MMMM yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(vertical = 8.dp)
            .clickable { onEditClick?.invoke(promoData) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(data = promoData.imageUrl)
                            .apply(block = {
                                crossfade(true)
                                placeholder(R.drawable.bungalow_single)
                                error(R.drawable.bungalow_single)
                            })
                            .build()
                    ),
                    contentDescription = promoData.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 16.dp, top = 8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = promoData.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        maxLines = 2,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )

                    Text(
                        text = promoData.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp
                        ),
                        maxLines = 4,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "$startDate - $endDate",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryGold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    OutlinedTextField(
                        value = promoData.promoCode,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PrimaryGold,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = GoldAccent,
                            focusedBorderColor = PrimaryGold,
                            focusedTextColor = Color.Black,
                            focusedContainerColor = GoldAccent
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(promoData.promoCode))
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy Referral Code"
                                )
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(45.dp)
                    .width(80.dp)
                    .background(
                        color = PrimaryGold,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 0.dp,
                            bottomEnd = 8.dp,
                            bottomStart = 0.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${promoData.discountPercentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

        }
    }
}

@Preview(showBackground = false)
@Composable
fun Preview() {
    val startDate = DateUtils.parseDate("30/05/2025")?.let { Timestamp(it) }
    val endDate = DateUtils.parseDate("01/06/2025")?.let { Timestamp(it) }

    PromoCard(
        promoData = PromoData(
            title = "Diskon Spesial Akhir Tahun!",
            description = "Nikmati potongan harga hingga 25% untuk semua pemesanan Homestay Puri Sae Malang",
            discountPercentage = 2.0,
            startDate = startDate!!,
            endDate = endDate!!,
            promoCode = "WEEKENDCERIA2025"
        )
    )
}