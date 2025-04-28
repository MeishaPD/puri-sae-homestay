package brawijaya.example.purisaehomestay.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri
import brawijaya.example.purisaehomestay.data.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    val currentUser: FirebaseUser?
    val isUserLoggedIn: Boolean

    suspend fun signUp(email: String, password: String, name: String, phoneNumber: String? = null): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun resetPassword(email: String): AuthResult
    fun signOut()
}

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun signUp(email: String, password: String, name: String, phoneNumber: String?): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            val photoUri = "https://firebasestorage.googleapis.com/v0/b/fitly-test-app.appspot.com/o/avatars%2F7.png?alt=media&token=7f31d3b4-55ec-46d5-b463-17e4c2da6929".toUri()

            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(photoUri)
                    .build()
                user.updateProfile(profileUpdates).await()



                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to (phoneNumber ?: ""),
                    "role" to "Penyewa",
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()

                AuthResult.Success
            } else {
                AuthResult.Error("Pendaftaran gagal, silakan coba lagi")
            }
        } catch (e: Exception) {
            when {
                e.message?.contains("email address is already in use") == true -> {
                    AuthResult.Error("Email sudah terdaftar")
                }
                e.message?.contains("password is invalid") == true -> {
                    AuthResult.Error("Password minimal 6 karakter")
                }
                else -> {
                    AuthResult.Error("Pendaftaran gagal: ${e.message}")
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record") == true -> {
                    AuthResult.Error("Email tidak terdaftar")
                }
                e.message?.contains("password is invalid") == true -> {
                    AuthResult.Error("Password salah")
                }
                else -> {
                    AuthResult.Error("Login gagal: ${e.message}")
                }
            }
        }
    }

    override suspend fun resetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (e: Exception) {
            when {
                e.message?.contains("no user record") == true -> {
                    AuthResult.Error("Email tidak terdaftar")
                }
                else -> {
                    AuthResult.Error("Gagal mengirim email reset password")
                }
            }
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}

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
}