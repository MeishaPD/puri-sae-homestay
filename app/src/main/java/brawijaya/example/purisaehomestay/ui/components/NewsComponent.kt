package brawijaya.example.purisaehomestay.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import brawijaya.example.purisaehomestay.data.model.NewsData

@Composable
fun NewsComponent(
    news: NewsData,
) {
    var showImagePreview by remember { mutableStateOf(false) }
    var initialImageIndex by remember { mutableIntStateOf(0) }

    Column {
        Text(
            text = news.date,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            color = Color.Gray,
        )
        Text(
            text = news.description,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            ),
        )

        if (news.imageUrl.size == 4) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = news.imageUrl[0]),
                        contentDescription = "Image 1",
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                initialImageIndex = 0
                                showImagePreview = true
                            },
                        contentScale = ContentScale.FillWidth
                    )
                    Image(
                        painter = painterResource(id = news.imageUrl[1]),
                        contentDescription = "Image 2",
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                initialImageIndex = 1
                                showImagePreview = true
                            },
                        contentScale = ContentScale.FillWidth
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = news.imageUrl[2]),
                        contentDescription = "Image 3",
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                initialImageIndex = 2
                                showImagePreview = true
                            },
                        contentScale = ContentScale.FillWidth
                    )
                    Image(
                        painter = painterResource(id = news.imageUrl[3]),
                        contentDescription = "Image 4",
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                initialImageIndex = 3
                                showImagePreview = true
                            },
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        } else if (news.imageUrl.size == 3) {
            Image(
                painter = painterResource(id = news.imageUrl[0]),
                contentDescription = "Image 1",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(215.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        initialImageIndex = 0
                        showImagePreview = true
                    },
                contentScale = ContentScale.FillWidth
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = news.imageUrl[1]),
                    contentDescription = "Image 2",
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            initialImageIndex = 1
                            showImagePreview = true
                        },
                    contentScale = ContentScale.FillWidth
                )
                Image(
                    painter = painterResource(id = news.imageUrl[2]),
                    contentDescription = "Image 3",
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            initialImageIndex = 2
                            showImagePreview = true
                        },
                    contentScale = ContentScale.FillWidth
                )
            }
        } else if (news.imageUrl.size == 2) {
            Image(
                painter = painterResource(id = news.imageUrl[0]),
                contentDescription = "Image 1",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        initialImageIndex = 0
                        showImagePreview = true
                    },
                contentScale = ContentScale.FillWidth
            )
            Image(
                painter = painterResource(id = news.imageUrl[1]),
                contentDescription = "Image 2",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        initialImageIndex = 1
                        showImagePreview = true
                    },
                contentScale = ContentScale.FillWidth
            )
        } else if (news.imageUrl.size == 1) {
            Image(
                painter = painterResource(id = news.imageUrl[0]),
                contentDescription = "Image 1",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        initialImageIndex = 0
                        showImagePreview = true
                    },
                contentScale = ContentScale.FillWidth
            )
        }
    }

    if (showImagePreview) {
        ImagePreviewDialog(
            imageResources = news.imageUrl,
            initialImageIndex = initialImageIndex,
            onDismiss = { showImagePreview = false }
        )
    }
}