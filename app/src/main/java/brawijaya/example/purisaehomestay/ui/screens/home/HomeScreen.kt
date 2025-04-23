package brawijaya.example.purisaehomestay.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BottomNavigation(
                    currentRoute = Screen.Home.route,
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
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(182.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to PrimaryDarkGreen,
                                0.6f to PrimaryDarkGreen,
                                1.0f to Color.Transparent
                            )
                        )
                    )
                    .zIndex(1f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 64.dp)
                    .padding(innerPadding)
            ) {
                HomeScreenContent(navController)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .zIndex(2f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_side_text),
                    contentDescription = "Puri Sae Malang Logo",
                    modifier = Modifier.padding(start = 2.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    navController: NavController
) {
    Column {
        Image(
            painter = painterResource(id = R.drawable.homestay_image),
            contentDescription = "Homestay Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(PrimaryGold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    navController.navigate(Screen.Order.route)
                }
            ) {
                Text("Pesan Sekarang")
            }

            Text(
                text = "Promo",
                modifier = Modifier.padding(vertical = 8.dp),
                style = Typography.headlineLarge,
                fontSize = 20.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp)
                    .background(Color.LightGray)
            )

            Text(
                text = "Ulasan",
                modifier = Modifier.padding(bottom = 8.dp),
                style = Typography.headlineLarge,
                fontSize = 20.sp
            )

            repeat(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 4.dp)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.padding(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.padding(8.dp))
            }

            Text(
                text = "Tentang Puri Sae Malang",
                modifier = Modifier
                    .padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )

            Image(
                painter = painterResource(id = R.drawable.logo_under_text),
                modifier = Modifier
                    .fillMaxSize(),
                contentDescription = "Logo Ulasan",
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp)
                    .background(Color.LightGray)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray)
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen(navController = null)
//}