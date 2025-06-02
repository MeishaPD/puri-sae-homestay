package brawijaya.example.purisaehomestay

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class PuriSaeHomestayApp : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        val db = FirebaseFirestore.getInstance()
        val configRef = db.collection("config").document("onesignal")

        configRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("api_key")) {
                    val apiKey = document.getString("api_key")
                    if (!apiKey.isNullOrEmpty()) {
                        OneSignal.initWithContext(this, apiKey)

                        Log.d("PuriSaeApp", "Successfully initialize OneSignal")
                        CoroutineScope(Dispatchers.IO).launch {
                            OneSignal.Notifications.requestPermission(true)
                            Log.d("PuriSaeApp", "Successfully subscribe to OneSignal notification")
                        }
                    } else {
                        Log.e("PuriSaeApp", "OneSignal API Key is empty")
                    }
                } else {
                    Log.e("PuriSaeApp", "OneSignal config not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("PuriSaeApp", "Failed to get OneSignal config", e)
            }
    }
}