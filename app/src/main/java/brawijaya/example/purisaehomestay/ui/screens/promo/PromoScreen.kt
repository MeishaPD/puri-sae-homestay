package brawijaya.example.purisaehomestay.ui.screens.promo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.ui.components.BottomNavigation
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.screens.promo.components.PromoCard
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PrimaryGold
import brawijaya.example.purisaehomestay.ui.viewmodels.ProfileViewModel
import brawijaya.example.purisaehomestay.ui.viewmodels.PromoUiState
import brawijaya.example.purisaehomestay.ui.viewmodels.PromoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoScreen(
    navController: NavController,
    viewModel: PromoViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()

    val isAdmin = profileUiState.isAdmin

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen
                ),
                title = {
                    Text(
                        text = "Promo",
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
                    currentRoute = Screen.Promo.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (isAdmin) {
                IconButton(
                    onClick = {
                        navController.navigate(Screen.EditPromo.createRoute())
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = PrimaryGold,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Promo"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            PromoScreenContent(
                uiState = uiState,
                onEditClick = { promo ->
                    if (isAdmin) {
                        navController.navigate(Screen.EditPromo.createRoute(promo.id))
                    } else {
                        null
                    }
                }
            )
        }

    }
}

@Composable
fun PromoScreenContent(
    uiState: PromoUiState,
    onEditClick: ((PromoData) -> Unit)? = null
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        uiState.promoList.forEach { promo ->
            PromoCard(
                promoData = promo,
                onEditClick = onEditClick
            )
        }
    }
}