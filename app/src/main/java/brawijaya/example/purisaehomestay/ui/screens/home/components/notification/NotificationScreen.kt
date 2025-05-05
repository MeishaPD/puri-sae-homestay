package brawijaya.example.purisaehomestay.ui.screens.home.components.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.NotificationData
import brawijaya.example.purisaehomestay.data.model.NotificationType
import brawijaya.example.purisaehomestay.data.model.UserNotification
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel(),
    userId: String,
    onNotificationClick: (NotificationData, UserNotification) -> Unit
) {
    val notifications by viewModel.notifications.observeAsState(emptyList())
    val unreadCount by viewModel.unreadCount.observeAsState(0)
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState(null)

    LaunchedEffect(key1 = userId) {
        viewModel.getUserNotifications(userId)
        viewModel.getUnreadNotificationsCount(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Notifikasi")
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                error?.let { currentError ->
                    ErrorView(
                        error = currentError,
                        onRetry = {
                            viewModel.clearError()
                            viewModel.getUserNotifications(userId)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } ?: run {
                    if (notifications.isEmpty()) {
                        EmptyNotificationView(Modifier.align(Alignment.Center))
                    } else {
                        NotificationList(
                            notifications = notifications,
                            onNotificationClick = onNotificationClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationList(
    notifications: List<Pair<NotificationData, UserNotification>>,
    onNotificationClick: (NotificationData, UserNotification) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notifications) { (notification, userNotification) ->
            NotificationItem(
                notification = notification,
                userNotification = userNotification,
                onClick = { onNotificationClick(notification, userNotification) }
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationData,
    userNotification: UserNotification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (userNotification.isRead) Color.White else Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on notification type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            NotificationType.PROMO -> Color(0xFFFFD700) // Gold for promos
                            NotificationType.NEWS -> Color(0xFF4CAF50) // Green for news
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.PROMO -> Icons.Default.CardGiftcard
                        NotificationType.NEWS -> Icons.AutoMirrored.Filled.Announcement
                    },
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.description,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(notification.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (!userNotification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Blue)
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tidak ada notifikasi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}

@Composable
fun ErrorView(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: $error",
            fontSize = 16.sp,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry
        ) {
            Text(text = "Coba Lagi")
        }
    }
}

@Composable
fun NotificationBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
    val currentYear = calendar.get(Calendar.YEAR)

    calendar.time = date
    val messageDay = calendar.get(Calendar.DAY_OF_YEAR)
    val messageYear = calendar.get(Calendar.YEAR)

    return when {
        currentYear != messageYear -> {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
        }
        currentDay == messageDay -> {
            "Hari ini, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        }
        currentDay - messageDay == 1 -> {
            "Kemarin, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        }
        currentDay - messageDay < 7 -> {
            SimpleDateFormat("EEEE, HH:mm", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(date)
        }
    }
}