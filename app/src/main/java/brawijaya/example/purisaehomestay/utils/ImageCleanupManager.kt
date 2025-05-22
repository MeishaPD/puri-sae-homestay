package brawijaya.example.purisaehomestay.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import brawijaya.example.purisaehomestay.data.repository.CloudinaryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class ImageCleanupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudinaryRepository: CloudinaryRepository
) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "image_cleanup_prefs",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val PENDING_IMAGES_KEY = "pending_images"
        private const val TAG = "ImageCleanupManager"
    }

    fun addPendingImage(imageUrl: String) {
        val currentList = getPendingImages().toMutableList()
        if (!currentList.contains(imageUrl)) {
            currentList.add(imageUrl)
            savePendingImages(currentList)
            Log.d(TAG, "Added pending image: $imageUrl")
        }
    }

    fun removePendingImage(imageUrl: String) {
        val currentList = getPendingImages().toMutableList()
        if (currentList.remove(imageUrl)) {
            savePendingImages(currentList)
            Log.d(TAG, "Removed pending image: $imageUrl")
        }
    }

    fun cleanupImage(imageUrl: String) {
        scope.launch {
            try {
                val success = cloudinaryRepository.deleteImage(imageUrl)
                if (success) {
                    removePendingImage(imageUrl)
                    Log.d(TAG, "Successfully cleaned up image: $imageUrl")
                } else {
                    Log.w(TAG, "Failed to cleanup image: $imageUrl")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up image: $imageUrl", e)
            }
        }
    }

    fun cleanupAllPendingImages() {
        scope.launch {
            val pendingImages = getPendingImages()
            Log.d(TAG, "Starting cleanup of ${pendingImages.size} pending images")

            pendingImages.forEach { imageUrl ->
                try {
                    val success = cloudinaryRepository.deleteImage(imageUrl)
                    if (success) {
                        Log.d(TAG, "Successfully cleaned up pending image: $imageUrl")
                    } else {
                        Log.w(TAG, "Failed to cleanup pending image: $imageUrl")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error cleaning up pending image: $imageUrl", e)
                }
            }

            clearAllPendingImages()
        }
    }

    fun cleanupOldPendingImages(maxAgeMillis: Long = 24 * 60 * 60 * 1000) { // Default 24 jam
        scope.launch {
            cleanupAllPendingImages()
        }
    }

    private fun getPendingImages(): List<String> {
        val json = sharedPrefs.getString(PENDING_IMAGES_KEY, "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing pending images JSON", e)
            emptyList()
        }
    }

    private fun savePendingImages(images: List<String>) {
        val json = gson.toJson(images)
        sharedPrefs.edit { putString(PENDING_IMAGES_KEY, json) }
    }

    private fun clearAllPendingImages() {
        sharedPrefs.edit { remove(PENDING_IMAGES_KEY) }
        Log.d(TAG, "Cleared all pending images from storage")
    }

    fun getPendingImageCount(): Int {
        return getPendingImages().size
    }
}