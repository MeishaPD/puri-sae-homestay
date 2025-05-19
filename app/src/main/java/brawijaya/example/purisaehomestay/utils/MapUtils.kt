package brawijaya.example.purisaehomestay.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun openMapDirection(context: Context, destination: String) {
    val gmmIntentUri = "google.navigation:q=${Uri.encode(destination)}".toUri()
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        val webUri =
            "https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(destination)}".toUri()
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
    }
}