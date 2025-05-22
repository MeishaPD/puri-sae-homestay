package brawijaya.example.purisaehomestay.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import brawijaya.example.purisaehomestay.BuildConfig
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isInitialized = false

    private fun initializeCloudinary() {
        if (!isInitialized) {
            try {
                val config = mapOf(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
                )


                MediaManager.init(context, config)
                isInitialized = true
            } catch (e: Exception) {
                Log.e("CloudinaryRepository", "Error initializing Cloudinary: ${e.message}", e)
            }
        }
    }

    suspend fun uploadImage(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            initializeCloudinary()

            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use {
                uploadToCloudinary(it)
            } ?: throw IllegalArgumentException("Could not open input stream for URI: $uri")
        }
    }

    private suspend fun uploadToCloudinary(inputStream: InputStream): String {
        return suspendCancellableCoroutine { continuation ->
            try {
                val tempFile = createTempFile(suffix = ".jpg")
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val requestId = MediaManager.get().upload(tempFile.absolutePath)
                    .unsigned("ml_default")
                    .option("resource_type", "image")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d("CloudinaryRepository", "Upload started: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100) / totalBytes
                            Log.d("CloudinaryRepository", "Upload progress: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            Log.d("CloudinaryRepository", "Upload success: $requestId")
                            val secureUrl = resultData["secure_url"] as? String
                            Log.d("Secure Url", "Upload success: $secureUrl")
                            if (secureUrl != null) {
                                continuation.resume(secureUrl)
                            } else {
                                continuation.resumeWithException(
                                    RuntimeException("Upload succeeded but URL was null")
                                )
                            }
                            tempFile.delete()
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e("CloudinaryRepository", "Upload error: ${error.description}")
                            continuation.resumeWithException(
                                RuntimeException("Upload failed: ${error.description}")
                            )
                            tempFile.delete()
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.d("CloudinaryRepository", "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()

                continuation.invokeOnCancellation {
                    MediaManager.get().cancelRequest(requestId)
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.e("CloudinaryRepository", "Error during upload preparation: ${e.message}", e)
                continuation.resumeWithException(e)
            }
        }
    }
}