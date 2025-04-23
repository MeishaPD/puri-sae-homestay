package brawijaya.example.purisaehomestay.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PermContactCalendar
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.profile.components.MenuItemWithIcon
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
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
//                    onNavigate = {}
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
            ProfileContent(navController)
        }

    }
}

@Composable
fun ProfileContent(
    navController: NavController
) {

    val isLoggedIn = false

    Column(modifier = Modifier.padding(32.dp)) {
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
                onClick = {navController.navigate(Screen.Login.route)},
            ) {
                Text(
                    text = "Masuk",
                    fontWeight = FontWeight.Medium,
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
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                if (isLoggedIn) {
                    MenuItemWithIcon(
                        painter = painterResource(id = R.drawable.overview),
                        title = "Aktivitas Saya",
                        onClick = {}
                    )
                    MenuItemWithIcon(
                        icon = Icons.Rounded.QuestionAnswer,
                        title = "FAQ",
                        onClick = {}
                    )
                    MenuItemWithIcon(
                        icon = Icons.Rounded.PermContactCalendar,
                        title = "Hubungi Kami",
                        onClick = {}
                    )

                    Text(
                        text = "Akun",
                        modifier = Modifier.padding(top = 16.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    MenuItemWithIcon(
                        icon = Icons.Rounded.Info,
                        title = "Hubungi Kami",
                        onClick = {}
                    )
                    MenuItemWithIcon(
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        title = "Keluar",
                        onClick = {}
                    )
                } else {
                    MenuItemWithIcon(
                        icon = Icons.Rounded.QuestionAnswer,
                        title = "FAQ",
                        onClick = {}
                    )
                    MenuItemWithIcon(
                        icon = Icons.Rounded.PermContactCalendar,
                        title = "Hubungi Kami",
                        onClick = {}
                    )
                }
            }
        }
    }
}