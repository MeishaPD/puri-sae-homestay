package brawijaya.example.purisaehomestay.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Discount
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import brawijaya.example.purisaehomestay.ui.navigation.Screen
import brawijaya.example.purisaehomestay.ui.theme.LightGreyBg
import brawijaya.example.purisaehomestay.ui.theme.PrimaryDarkGreen
import brawijaya.example.purisaehomestay.ui.theme.PuriSaeHomestayTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val blurRadius = with(density) { 1.dp.toPx() }

    BottomAppBar(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .zIndex(1f)
            .background(Color.Transparent),
        containerColor = Color.Transparent,
        contentColor = Color.Black,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp),
    ) {
        NavSurfaceContent(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            blurRadius = blurRadius
        )
    }
}

@Composable
private fun RowScope.NavSurfaceContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    blurRadius: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    frameworkPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
                    frameworkPaint.color = LightGreyBg.copy(alpha = 0.97f).toArgb()
                    canvas.drawRect(0f, 0f, size.width, size.height, paint)
                }
            },
        color = Color.Transparent,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Outlined.Home,
                filledIcon = Icons.Rounded.Home,
                label = "Beranda",
                isSelected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) }
            )

            BottomNavItem(
                icon = Icons.AutoMirrored.Outlined.Article,
                filledIcon = Icons.AutoMirrored.Rounded.Article,
                label = "Pemesanan",
                isSelected = currentRoute == Screen.Order.route,
                onClick = { onNavigate(Screen.Order.route) }
            )

            BottomNavItem(
                icon = Icons.Outlined.Discount,
                filledIcon = Icons.Rounded.Discount,
                label = "Promo",
                isSelected = currentRoute == Screen.Promo.route,
                onClick = { onNavigate(Screen.Promo.route) },
            )

            BottomNavItem(
                icon = Icons.Outlined.Person,
                filledIcon = Icons.Rounded.Person,
                label = "Profile",
                isSelected = currentRoute == Screen.Profile.route,
                onClick = { onNavigate(Screen.Profile.route) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    filledIcon: ImageVector? = null
) {
    val itemColor = if (isSelected) PrimaryDarkGreen else Color.Black
    val displayIcon = if (isSelected && filledIcon != null) filledIcon else icon

    Box(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = displayIcon,
                contentDescription = label,
                tint = if (isSelected) PrimaryDarkGreen else itemColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SetupTransparentSystemBars() {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController) {
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = true
        )

        onDispose {}
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavItemPreview() {
    PuriSaeHomestayTheme {
        BottomNavItem(
            icon = Icons.AutoMirrored.Outlined.Article,
            filledIcon = Icons.AutoMirrored.Outlined.Article,
            label = "Pemesanan",
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF004936)
@Composable
fun BottomNavigationPreview() {
    PuriSaeHomestayTheme {
        BottomNavigation(
            currentRoute = Screen.Home.route,
            onNavigate = {},
        )
    }
}