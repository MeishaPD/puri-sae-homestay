package brawijaya.example.purisaehomestay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import brawijaya.example.purisaehomestay.ui.screens.home.HomeScreen
import brawijaya.example.purisaehomestay.ui.screens.order.OrderScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.ProfileScreen
import brawijaya.example.purisaehomestay.ui.screens.promo.PromoScreen

sealed class Screen(val route: String){
    object Home : Screen("home")
    object Order : Screen("order")
    object Promo : Screen("promo")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Order.route) {
            OrderScreen(navController = navController)
        }
        composable(Screen.Promo.route) {
            PromoScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}