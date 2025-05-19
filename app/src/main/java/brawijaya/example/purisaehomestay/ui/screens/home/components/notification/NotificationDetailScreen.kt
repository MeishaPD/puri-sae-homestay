package brawijaya.example.purisaehomestay.ui.screens.home.components.notification
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
//import androidx.compose.material.icons.filled.CardGiftcard
//import androidx.compose.material.icons.filled.Newspaper
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import brawijaya.example.purisaehomestay.data.model.NotificationData
//import brawijaya.example.purisaehomestay.data.model.NotificationType
//import brawijaya.example.purisaehomestay.data.model.UserNotification
//import brawijaya.example.purisaehomestay.ui.viewmodels.NotificationViewModel
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NotificationDetailScreen(
//    notification: NotificationData,
//    userNotification: UserNotification,
//    viewModel: NotificationViewModel,
//    onBackClick: () -> Unit
//) {
//    // Mark notification as read when opened
//    LaunchedEffect(key1 = userNotification.id) {
//        if (!userNotification.isRead) {
//            viewModel.markNotificationAsRead(userNotification.id)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = when (notification.type) {
//                            NotificationType.PROMO -> "Detail Promo"
//                            NotificationType.NEWS -> "Detail Berita"
//                        }
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
//                            contentDescription = "Back"
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary
//                ),
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Header with icon and title
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(56.dp)
//                        .clip(CircleShape)
//                        .background(
//                            when (notification.type) {
//                                NotificationType.PROMO -> Color(0xFFFFD700) // Gold for promos
//                                NotificationType.NEWS -> Color(0xFF4CAF50) // Green for news
//                            }
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = when (notification.type) {
//                            NotificationType.PROMO -> Icons.Default.CardGiftcard
//                            NotificationType.NEWS -> Icons.Default.Newspaper
//                        },
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(32.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Text(
//                    text = notification.title,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            // Date information
//            Text(
//                text = formatTimestamp(notification.createdAt),
//                fontSize = 14.sp,
//                color = Color.Gray
//            )
//
//            HorizontalDivider()
//
//            // Image if available
//            notification.imageUrl?.let { imageUrl ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    elevation = CardDefaults.cardElevation(2.dp)
//                ) {
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(imageUrl)
//                            .crossfade(true)
//                            .build(),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//
//            // Content
//            Text(
//                text = notification.description,
//                fontSize = 16.sp,
//                lineHeight = 24.sp
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Action buttons based on notification type
//            when (notification.type) {
//                NotificationType.PROMO -> {
//                    Button(
//                        onClick = { /* Navigate to booking with promo */ },
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFFFD700),
//                            contentColor = Color.Black
//                        )
//                    ) {
//                        Text(text = "Gunakan Promo Ini")
//                    }
//                }
//                NotificationType.NEWS -> {
//                    OutlinedButton(
//                        onClick = { /* Navigate to full news page */ },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(text = "Lihat Berita Lengkap")
//                    }
//                }
//            }
//        }
//    }
//}