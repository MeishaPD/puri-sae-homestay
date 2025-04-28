package brawijaya.example.purisaehomestay.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.UserData
import brawijaya.example.purisaehomestay.data.repository.AuthRepository
import brawijaya.example.purisaehomestay.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userData: UserData? = null,
    val error: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
        if (authRepository.isUserLoggedIn) {
            loadUserData()
        }
    }

    private fun checkAuthState() {
        _uiState.update { state ->
            state.copy(isLoggedIn = authRepository.isUserLoggedIn)
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val userData = userRepository.getCurrentUserData()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userData = userData,
                        isLoggedIn = userData != null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load user data: ${e.message}"
                    )
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update { state ->
            state.copy(isLoggedIn = false, userData = null)
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(error = "")
        }
    }
}