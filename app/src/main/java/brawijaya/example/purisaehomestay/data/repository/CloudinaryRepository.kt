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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.TreeMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isInitialized = false
    private val httpClient = OkHttpClient()

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
                val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                val requestId = MediaManager.get().upload(tempFile.absolutePath)
                    .unsigned("ml_default")
                    .option("resource_type", "image")
                    .option("folder", "PuriSaeHomestay")
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

    suspend fun deleteImage(imageUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val publicId = extractPublicIdFromUrl(imageUrl)
                if (publicId.isNullOrEmpty()) {
                    Log.e("CloudinaryRepository", "Could not extract public_id from URL: $imageUrl")
                    return@withContext false
                }

                Log.d("CloudinaryRepository", "Extracted public_id: $publicId")
                deleteImageByPublicId(publicId)
            } catch (e: Exception) {
                Log.e("CloudinaryRepository", "Error deleting image: ${e.message}", e)
                false
            }
        }
    }

    private fun extractPublicIdFromUrl(url: String): String? {
        return try {
            val regex = """https://res\.cloudinary\.com/[^/]+/image/upload/(?:v\d+/)?(.+)$""".toRegex()
            val matchResult = regex.find(url)
            val publicIdWithExtension = matchResult?.groupValues?.get(1)

            if (publicIdWithExtension != null) {
                val decodedPublicId = URLDecoder.decode(publicIdWithExtension, "UTF-8")

                val publicId = decodedPublicId.substringBeforeLast(".")

                Log.d("CloudinaryRepository", "Original URL part: $publicIdWithExtension")
                Log.d("CloudinaryRepository", "Decoded: $decodedPublicId")
                Log.d("CloudinaryRepository", "Final public_id: $publicId")

                return publicId
            }

            null
        } catch (e: Exception) {
            Log.e("CloudinaryRepository", "Error extracting public_id: ${e.message}")
            null
        }
    }

    private suspend fun deleteImageByPublicId(publicId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
                val apiKey = BuildConfig.CLOUDINARY_API_KEY
                val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

                val timestamp = (System.currentTimeMillis() / 1000).toString()

                val paramsForSignature = TreeMap<String, String>().apply {
                    put("public_id", publicId)
                    put("timestamp", timestamp)
                }

                val signature = generateSignature(paramsForSignature, apiSecret)

                Log.d("CloudinaryRepository", "Delete params - public_id: $publicId, timestamp: $timestamp")
                Log.d("CloudinaryRepository", "Generated signature: $signature")

                val formBody = okhttp3.FormBody.Builder()
                    .add("public_id", publicId)
                    .add("timestamp", timestamp)
                    .add("api_key", apiKey)
                    .add("signature", signature)
                    .build()

                Log.d("CloudinaryRepository", "Using FormBody with public_id: $publicId")

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/destroy")
                    .post(formBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d("CloudinaryRepository", "Delete response: $responseBody")

                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        val result = jsonResponse.optString("result", "")

                        if (result == "ok") {
                            Log.d("CloudinaryRepository", "Image deleted successfully: $publicId")
                            continuation.resume(true)
                        } else {
                            Log.e("CloudinaryRepository", "Failed to delete image: $result")
                            continuation.resume(false)
                        }
                    } else {
                        Log.e("CloudinaryRepository", "HTTP error: ${response.code} - ${response.message}")
                        Log.e("CloudinaryRepository", "Response body: $responseBody")
                        continuation.resume(false)
                    }
                }

            } catch (e: Exception) {
                Log.e("CloudinaryRepository", "Error in deleteImageByPublicId: ${e.message}", e)
                continuation.resumeWithException(e)
            }
        }
    }

    private fun generateSignature(params: Map<String, String>, apiSecret: String): String {
        val sortedParams = params.toSortedMap()
        val paramString = sortedParams.map { "${it.key}=${it.value}" }.joinToString("&")

        Log.d("CloudinaryRepository", "String to sign: '$paramString'")

        val mac = Mac.getInstance("HmacSHA1")
        val secretKeySpec = SecretKeySpec(apiSecret.toByteArray(Charsets.UTF_8), "HmacSHA1")
        mac.init(secretKeySpec)

        val hash = mac.doFinal(paramString.toByteArray(Charsets.UTF_8))

        return hash.joinToString("") {
            ((it.toInt() and 0xff) + 0x100).toString(16).substring(1)
        }
    }
}