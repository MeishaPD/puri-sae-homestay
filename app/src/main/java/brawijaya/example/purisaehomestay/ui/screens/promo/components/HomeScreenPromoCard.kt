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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.utils.DateUtils
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun HomeScreenPromoCard(
    promoData: PromoData,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val startDate = DateUtils.formatDate(promoData.startDate.toDate(), "dd MMMM yyyy")
    val endDate = DateUtils.formatDate(promoData.endDate.toDate(), "dd MMMM yyyy")

    Card(
        modifier = Modifier
            .width(240.dp)
            .height(120.dp)
            .padding(end = 8.dp)
            .clickable {
                onClick()
            },
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
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(100.dp)
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
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 3,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    Text(
                        text = "$startDate - $endDate",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PrimaryGold,
                        modifier = Modifier.padding(top = 2.dp)
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}