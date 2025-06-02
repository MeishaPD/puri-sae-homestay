package brawijaya.example.purisaehomestay.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rate_limit_prefs")

data class RateLimitInfo(
    val isBlocked: Boolean = false,
    val remainingTime: Long = 0L,
    val attemptsCount: Int = 0
)

@Singleton
class RateLimitManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val FAILED_ATTEMPTS_KEY = intPreferencesKey("failed_attempts")
        private val FIRST_ATTEMPT_TIME_KEY = longPreferencesKey("first_attempt_time")
        private val BLOCK_UNTIL_KEY = longPreferencesKey("block_until")

        // Rate limiting configuration
        private const val MAX_ATTEMPTS_BEFORE_BLOCK = 5 // Block after 5 failed attempts
        private const val BLOCK_DURATION_MS = 15 * 60 * 1000L // 15 minutes
        private const val ATTEMPTS_RESET_DURATION_MS = 30 * 60 * 1000L // Reset attempts after 30 minutes
    }

    suspend fun checkRateLimit(): RateLimitInfo {
        val preferences = dataStore.data.first()
        val currentTime = System.currentTimeMillis()

        val failedAttempts = preferences[FAILED_ATTEMPTS_KEY] ?: 0
        val firstAttemptTime = preferences[FIRST_ATTEMPT_TIME_KEY] ?: 0L
        val blockUntil = preferences[BLOCK_UNTIL_KEY] ?: 0L

        // Check if user is currently blocked
        if (blockUntil > currentTime) {
            return RateLimitInfo(
                isBlocked = true,
                remainingTime = blockUntil - currentTime,
                attemptsCount = failedAttempts
            )
        }

        // Reset attempts if enough time has passed since first attempt
        if (firstAttemptTime > 0 && (currentTime - firstAttemptTime) > ATTEMPTS_RESET_DURATION_MS) {
            resetAttempts()
            return RateLimitInfo(isBlocked = false, remainingTime = 0L, attemptsCount = 0)
        }

        return RateLimitInfo(
            isBlocked = false,
            remainingTime = 0L,
            attemptsCount = failedAttempts
        )
    }

    suspend fun recordFailedAttempt(): RateLimitInfo {
        val currentTime = System.currentTimeMillis()
        val preferences = dataStore.data.first()

        val currentAttempts = preferences[FAILED_ATTEMPTS_KEY] ?: 0
        val firstAttemptTime = preferences[FIRST_ATTEMPT_TIME_KEY] ?: 0L

        val newAttempts = currentAttempts + 1
        val newFirstAttemptTime = if (firstAttemptTime == 0L) currentTime else firstAttemptTime

        dataStore.edit { prefs ->
            prefs[FAILED_ATTEMPTS_KEY] = newAttempts
            prefs[FIRST_ATTEMPT_TIME_KEY] = newFirstAttemptTime

            // Block user if max attempts reached
            if (newAttempts >= MAX_ATTEMPTS_BEFORE_BLOCK) {
                prefs[BLOCK_UNTIL_KEY] = currentTime + BLOCK_DURATION_MS
            }
        }

        val blockUntil = if (newAttempts >= MAX_ATTEMPTS_BEFORE_BLOCK) {
            currentTime + BLOCK_DURATION_MS
        } else 0L

        return RateLimitInfo(
            isBlocked = blockUntil > currentTime,
            remainingTime = if (blockUntil > currentTime) blockUntil - currentTime else 0L,
            attemptsCount = newAttempts
        )
    }

    suspend fun recordSuccessfulLogin() {
        resetAttempts()
    }

    private suspend fun resetAttempts() {
        dataStore.edit { prefs ->
            prefs.remove(FAILED_ATTEMPTS_KEY)
            prefs.remove(FIRST_ATTEMPT_TIME_KEY)
            prefs.remove(BLOCK_UNTIL_KEY)
        }
    }

    fun formatRemainingTime(remainingTimeMs: Long): String {
        val totalSeconds = remainingTimeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }
}