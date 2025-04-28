package brawijaya.example.purisaehomestay.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.PermContactCalendar
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.components.GeneralDialog
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.profile.components.MenuItemWithIcon
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.loadUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Profile",
                        color = PrimaryGold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .padding(start = 2.dp)
                    )
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BottomNavigation(
                    currentRoute = Screen.Profile.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGold)
                }
            } else {
                ProfileContent(
                    navController = navController,
                    uiState = uiState,
                    onSignOut = { viewModel.signOut() }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    navController: NavController,
    uiState: ProfileUiState,
    onSignOut: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isLoggedIn = uiState.isLoggedIn
    val isAdmin = uiState.userData?.role == "Admin"
    val context = LocalContext.current

    Column(modifier = Modifier.padding(32.dp)) {
        if (isLoggedIn && uiState.userData != null) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.userData.photoUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.userData.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(color = Color.Transparent),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryGold)
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(color = Color(0xFF858585))
                            )
                        }
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(color = Color(0xFF858585))
                    )
                }

                Text(
                    text = uiState.userData.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.padding(top = 24.dp)
                )
                Text(
                    text = uiState.userData.role,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isAdmin) {
                    MenuItemWithIcon(
                        painter = painterResource(id = R.drawable.folder_managed),
                        title = "Kelola Paket",
                        onClick = {}
                    )
                    MenuItemWithIcon(
                        icon = Icons.Rounded.CreditCard,
                        title = "Kelola Pembayaran",
                        onClick = {}
                    )
                    MenuItemWithIcon(
                        icon = Icons.Rounded.Newspaper,
                        title = "Kelola Berita",
                        onClick = {}
                    )
                }

                MenuItemWithIcon(
                    painter = painterResource(id = R.drawable.overview),
                    title = "Aktivitas Saya",
                    onClick = {}
                )
                MenuItemWithIcon(
                    icon = Icons.Rounded.QuestionAnswer,
                    title = "FAQ",
                    onClick = {
                        navController.navigate(Screen.FAQ.route)
                    }
                )
                MenuItemWithIcon(
                    icon = Icons.Rounded.PermContactCalendar,
                    title = "Hubungi Kami",
                    onClick = {
                        navController.navigate(Screen.ContactUs.route)
                    }
                )

                Text(
                    text = "Akun",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.Start),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                MenuItemWithIcon(
                    icon = Icons.Rounded.Info,
                    title = "Informasi Akun",
                    onClick = {}
                )
                MenuItemWithIcon(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = "Keluar",
                    onClick = { showLogoutDialog = true }
                )

                if (showLogoutDialog) {
                    GeneralDialog(
                        message = "Apakah Anda yakin untuk keluar?",
                        onDismiss = {
                            showLogoutDialog = false
                        },
                        onConfirm = {
                            onSignOut()
                            showLogoutDialog = false
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(PrimaryGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        navController.navigate(Screen.Login.route)
                    },
                ) {
                    Text(
                        text = "Masuk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Belum punya akun?",
                        fontSize = 12.sp
                    )
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.Register.route)
                        }
                    ) {
                        Text(
                            text = "Daftar di sini",
                            color = PrimaryGold,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                MenuItemWithIcon(
                    icon = Icons.Rounded.QuestionAnswer,
                    title = "FAQ",
                    onClick = {
                        navController.navigate(Screen.FAQ.route)
                    }
                )
                MenuItemWithIcon(
                    icon = Icons.Rounded.PermContactCalendar,
                    title = "Hubungi Kami",
                    onClick = {
                        navController.navigate(Screen.ContactUs.route)
                    }
                )
            }
        }
    }
}