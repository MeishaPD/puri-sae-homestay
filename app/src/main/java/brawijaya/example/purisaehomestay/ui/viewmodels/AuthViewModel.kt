package brawijaya.example.purisaehomestay.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.repository.AuthRepository
import brawijaya.example.purisaehomestay.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String = "",
    val isPasswordResetSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _uiState.update { state ->
            state.copy(isLoggedIn = authRepository.isUserLoggedIn)
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Email dan password tidak boleh kosong")
            }
            return
        }

        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = ""
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun signUp(email: String, password: String, name: String, phoneNumber: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || phoneNumber.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Semua field harus diisi")
            }
            return
        }

        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            when (val result = authRepository.signUp(email, password, name, phoneNumber)) {
                is AuthResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = ""
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Email tidak boleh kosong")
            }
            return
        }

        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            when (val result = authRepository.resetPassword(email)) {
                is AuthResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "",
                            isPasswordResetSent = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessage = "")
        }
    }
}