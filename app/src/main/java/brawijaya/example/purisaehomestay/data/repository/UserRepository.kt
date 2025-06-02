package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    suspend fun getCurrentUserData(): UserData? {
        val firebaseUser = auth.currentUser ?: return null

        try {
            val name = firebaseUser.displayName ?: ""
            val email = firebaseUser.email ?: ""
            val photoUrl = firebaseUser.photoUrl?.toString()

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val role = userDoc.getString("role") ?: "Penyewa"
            val phoneNumber = userDoc.getString("phoneNumber") ?: ""

            val userData = UserData(
                id = firebaseUser.uid,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                role = role,
                photoUrl = photoUrl
            )

            _userData.value = userData
            return userData
        } catch (e: Exception) {
            return UserData(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                role = "Penyewa"
            )
            Log.w(e.toString(), "Error getting current user data:")
        }
    }

    fun refreshUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _userData.value = null
        }
    }

    fun isUserAdmin(): Boolean {
        return _userData.value?.role == "Admin"
    }

    fun getCurrentUserRef(): String? {
        val userId = auth.currentUser?.uid
        return if (userId != null) "/users/$userId" else null
    }
}