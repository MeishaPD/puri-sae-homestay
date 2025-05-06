package brawijaya.example.purisaehomestay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import brawijaya.example.purisaehomestay.ui.screens.auth.LoginScreen
import brawijaya.example.purisaehomestay.ui.screens.auth.RegisterScreen
import brawijaya.example.purisaehomestay.ui.screens.home.HomeScreen
import brawijaya.example.purisaehomestay.ui.screens.home.components.notification.NotificationScreen
import brawijaya.example.purisaehomestay.ui.screens.order.OrderScreen
import brawijaya.example.purisaehomestay.ui.screens.order.history.ActivityScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.ProfileScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.contactus.ContactUsScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.faq.FAQScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.infoakun.InfoScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepackage.ManagePackageScreen
import brawijaya.example.purisaehomestay.ui.screens.promo.PromoScreen

sealed class Screen(val route: String){
    object Home : Screen("home")
    object Order : Screen("order")
    object Promo : Screen("promo")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object Register : Screen("register")
    object ContactUs : Screen("contact_us")
    object FAQ : Screen("faq")
    object Info : Screen("info")
    object Notification : Screen("notification")
    object Activities : Screen("activities")
    object ManagePackage: Screen("manage_package")
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
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ContactUs.route) {
            ContactUsScreen(navController = navController)
        }
        composable(Screen.FAQ.route) {
            FAQScreen(navController = navController)
        }
        composable(Screen.Info.route) {
            InfoScreen(navController = navController)
        }
        composable(Screen.Activities.route) {
            ActivityScreen(navController = navController)
        }
        composable(Screen.ManagePackage.route) {
            ManagePackageScreen(navController = navController)
        }
//        composable(Screen.Notification.route) {
//            NotificationScreen(navController = navController)
//        }
    }
}