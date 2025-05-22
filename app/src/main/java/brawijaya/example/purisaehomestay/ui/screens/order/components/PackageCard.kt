package brawijaya.example.purisaehomestay.ui.screens.order.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.text.NumberFormat
import java.util.Locale
import brawijaya.example.purisaehomestay.R

@Composable
fun PackageCard(
    idx: Int,
    paket: Paket,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    numberFormat.maximumFractionDigits = 0
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) BorderStroke(3.dp, PrimaryGold) else BorderStroke(
            1.dp,
            Color.LightGray
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Image(
                    painter = paket.thumbnail_url?.let {
                        rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(data = it)
                                .apply(block = {
                                    crossfade(true)
                                    placeholder(R.drawable.bungalow_single)
                                })
                                .build()
                        )
                    } ?: painterResource(id = R.drawable.bungalow_single),
                    contentDescription = paket.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(140.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                ) {
                    item {
                        Text(
                            text = paket.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    item {
                        paket.features.forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(Color.Black, CircleShape)
                                )
                                Text(
                                    text = feature,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "${
                                numberFormat.format(paket.price_weekday).replace("Rp", "Rp ")
                            }/malam (weekday)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (paket.price_weekend > 0) {
                            Text(
                                text = "${
                                    numberFormat.format(paket.price_weekend).replace("Rp", "Rp ")
                                } (weekend/holiday)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    text = idx.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}