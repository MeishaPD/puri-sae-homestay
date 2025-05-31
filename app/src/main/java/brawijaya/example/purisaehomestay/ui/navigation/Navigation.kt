package brawijaya.example.purisaehomestay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import brawijaya.example.purisaehomestay.ui.screens.auth.LoginScreen
import brawijaya.example.purisaehomestay.ui.screens.auth.RegisterScreen
import brawijaya.example.purisaehomestay.ui.screens.home.HomeScreen
//import brawijaya.example.purisaehomestay.ui.screens.home.components.notification.NotificationScreen
import brawijaya.example.purisaehomestay.ui.screens.order.OrderScreen
import brawijaya.example.purisaehomestay.ui.screens.order.history.ActivityScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.ProfileScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.contactus.ContactUsScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.faq.FAQScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.infoakun.InfoScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managenews.EditNewsScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managenews.ManageNewsScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepackage.EditPackageScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepackage.ManagePackageScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.managepayment.ManagePaymentScreen
import brawijaya.example.purisaehomestay.ui.screens.profile.menus.monthlyreport.MonthlyReportScreen
import brawijaya.example.purisaehomestay.ui.screens.promo.EditPromoScreen
import brawijaya.example.purisaehomestay.ui.screens.promo.PromoScreen
import brawijaya.example.purisaehomestay.ui.screens.upload.UploadScreen
import brawijaya.example.purisaehomestay.ui.viewmodels.OrderViewModel
import kotlin.text.isNullOrEmpty

sealed class Screen(val route: String) {
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
    object ManagePackage : Screen("manage_package")
    object ManageNews : Screen("manage_news")
    object ManagePayment : Screen("manage_payment")
    object MonthlyReport : Screen("monthly_report")
    object EditPackage : Screen("edit_package?paketId={paketId}") {
        fun createRoute(paketId: Int? = null): String {
            return if (paketId != null) {
                "edit_package?paketId=$paketId"
            } else {
                "edit_package?paketId=-1"
            }
        }
    }

    object EditNews : Screen("edit_news?newsId={newsId}") {
        fun createRoute(newsId: String? = null): String {
            return if (!newsId.isNullOrEmpty()) {
                "edit_news?newsId=$newsId"
            } else {
                "edit_news"
            }
        }
    }

    object EditPromo : Screen("edit_promo?promoId={promoId}") {
        fun createRoute(promoId: String? = null): String {
            return if (!promoId.isNullOrEmpty()) {
                "edit_promo?promoId=$promoId"
            } else {
                "edit_promo"
            }
        }
    }

    object UploadPayment : Screen("upload_payment?orderId={orderId}&source={source}") {
        fun createRoute(orderId: String? = null, source: String = "order"): String {
            return if (!orderId.isNullOrEmpty()) {
                "upload_payment?orderId=$orderId&source=$source"
            } else {
                "upload_payment?source=$source"
            }
        }
    }
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
        composable(Screen.ManageNews.route) {
            ManageNewsScreen(navController = navController)
        }
        composable(Screen.ManagePayment.route) {
            ManagePaymentScreen(navController = navController)
        }
        composable(Screen.MonthlyReport.route) {
            MonthlyReportScreen(navController = navController)
        }
        composable(
            route = Screen.EditPackage.route,
            arguments = listOf(
                navArgument("paketId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val paketId = backStackEntry.arguments?.getInt("paketId")
            val validPaketId = if (paketId != null && paketId != 0) paketId else null
            EditPackageScreen(
                navController = navController,
                paketId = validPaketId
            )
        }

        composable(
            route = Screen.EditNews.route,
            arguments = listOf(
                navArgument("newsId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId")
            EditNewsScreen(
                navController = navController,
                newsId = newsId
            )
        }
        composable(
            route = Screen.EditPromo.route,
            arguments = listOf(
                navArgument("promoId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val promoId = backStackEntry.arguments?.getString("promoId")
            EditPromoScreen(
                navController = navController,
                promoId = promoId
            )
        }

        composable(
            route = Screen.UploadPayment.route,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("source") {
                    type = NavType.StringType
                    defaultValue = "order"
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            val source = backStackEntry.arguments?.getString("source") ?: "order"

            val orderViewModel: OrderViewModel = when (source) {
                "order" -> {
                    val previousEntry = navController.previousBackStackEntry
                    if (previousEntry?.destination?.route == Screen.Order.route) {
                        hiltViewModel(previousEntry)
                    } else {
                        hiltViewModel()
                    }
                }
                "activity" -> hiltViewModel()
                else -> hiltViewModel()
            }

            UploadScreen(
                navController = navController,
                orderViewModel = orderViewModel,
                source = source,
                onImageUploaded = { imageUrl ->
                    when (source) {
                        "order" -> {
                            if (!orderId.isNullOrEmpty()) {
                                orderViewModel.setCurrentOrderId(orderId)
                            }
                            orderViewModel.handlePaymentProofUpload(imageUrl)
                        }
                        "activity" -> {
                            if (!orderId.isNullOrEmpty()) {
                                orderViewModel.setCurrentOrderId(orderId)
                                orderViewModel.handleDPPaymentUpload(imageUrl)
                            }
                            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_data", true)
                            navController.navigate(Screen.Profile.route)
                        }
                    }
                }
            )
        }
//        composable(Screen.Notification.route) {
//            NotificationScreen(navController = navController)
//        }
    }
}