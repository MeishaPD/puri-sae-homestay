package brawijaya.example.purisaehomestay.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.components.NewsComponent
import brawijaya.example.purisaehomestay.ui.screens.promo.components.HomeScreenPromoCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.theme.Typography
import brawijaya.example.purisaehomestay.ui.viewmodels.HomeUiState
import brawijaya.example.purisaehomestay.ui.viewmodels.HomeViewModel
import brawijaya.example.purisaehomestay.utils.openMapDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                HomeScreenContent(
                    navController = navController,
                    uiState = uiState
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .zIndex(2f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_side_text),
                        contentDescription = "Puri Sae Malang Logo",
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .width(218.dp)
                            .height(72.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {  }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            modifier = Modifier.padding(end = 14.dp),
                            contentDescription = "Notifikasi",
                            tint = PrimaryGold

                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    navController: NavController,
    uiState: HomeUiState
) {

    val facilities: List<String> = listOf(
        "Kolam Renang",
        "Bungalow (4-6 Orang)",
        "24 jam Full Wifi",
        "AC dan Air Panas",
        "Area Makan",
        "Joglo Utama",
        "Mushola",
        "Sarapan (by request)"
    )

    val context = LocalContext.current

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
                Text(
                    "Pesan Sekarang",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = "Promo",
                modifier = Modifier.padding(vertical = 8.dp),
                style = Typography.headlineLarge,
                fontSize = 20.sp
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                items(uiState.promo) { promoList ->
                    HomeScreenPromoCard(
                        promoData = promoList,
                        onClick = { navController.navigate(Screen.Promo.route) }
                    )
                }
            }

            Text(
                text = "Berita",
                modifier = Modifier.padding(bottom = 8.dp),
                style = Typography.headlineLarge,
                fontSize = 20.sp
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGold)
                }
            } else if (uiState.news.isNotEmpty()) {
                Column {
                    uiState.news.map { newsItem ->
                        NewsComponent(news = newsItem)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            } else {
                Text(
                    text = "Belum ada berita",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                )
            }

            Text(
                text = "Tentang Puri Sae Malang",
                modifier = Modifier
                    .padding(vertical = 8.dp),
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

            Text(
                text = "Puri Sae Malang adalah homestay keluarga yang berada di Dau, Sengkaling, Malang. Posisinya strategis karena dekat dengan tempat wisata di Kota Batu maupun Kota Malang. Cocok sebagai rumah singgah, pertemuan keluarga, maupun kegiatan formal dan nonformal lainnya.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    textAlign = TextAlign.Justify
                )
            )

            Text(
                text = "Fasilitas Kami",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            facilities.forEach { facility ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color.Black, CircleShape)
                    )
                    Text(
                        text = facility,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Text(
                text = "Lokasi Kami",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Jl. Jambu No. 21, Sempu, Gadingkulon, Kec. Dau, Kabupaten Malang, Jawa Timur 65151",
                fontSize = 12.sp,
                textAlign = TextAlign.Justify,
            )

            OutlinedButton(
                onClick = {
                    openMapDirection(context, "Puri SAE.kick")
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = PrimaryGold
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PrimaryGold)
            ) {
                Text(
                    text = "Lihat di Google Maps",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Text(
                text = "Hubungi Kami",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            val phoneNumber = "6281334463644"
                            val url = "https://wa.me/$phoneNumber"
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Whatsapp,
                        contentDescription = "Whatsapp",
                        modifier = Modifier.size(12.dp),
                        tint = Color.Black
                    )

                    Text(
                        text = "+6281-3344-63644",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            val uname = "purisaemalang"
                            val url = "https://instagram.com/$uname/"
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.instagram),
                        contentDescription = "Instagram",
                        modifier = Modifier.size(12.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = "@purisaemalang",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}