package brawijaya.example.purisaehomestay.ui.screens.order.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold

@Composable
fun PackageCard(
    number: String,
    title: String,
    features: List<String>,
    weekdayPrice: String,
    weekendPrice: String,
    imageRes: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) BorderStroke(3.dp, PrimaryGold) else BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
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

                    Text(
                        text = weekdayPrice,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (weekendPrice.isNotEmpty()) {
                        Text(
                            text = weekendPrice,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}