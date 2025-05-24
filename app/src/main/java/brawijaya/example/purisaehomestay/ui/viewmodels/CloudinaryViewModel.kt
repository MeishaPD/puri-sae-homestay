package brawijaya.example.purisaehomestay.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.repository.CloudinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudinaryUiState(
    val isUploading: Boolean = false,
    val imageUrl: String? = null,
    val errorMessage: String? = null,
    val uploadProgress: Float = 0f
)

@HiltViewModel
class CloudinaryViewModel @Inject constructor(
    private val cloudinaryRepository: CloudinaryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloudinaryUiState())
    val uiState: StateFlow<CloudinaryUiState> = _uiState.asStateFlow()

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    errorMessage = null,
                    uploadProgress = 0f
                )
            }

            try {
                _uiState.update { it.copy(uploadProgress = 0.5f) }

                val cloudinaryUrl = cloudinaryRepository.uploadImage(context, uri)

                _uiState.update {
                    it.copy(
                        imageUrl = cloudinaryUrl,
                        isUploading = false,
                        uploadProgress = 1f
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengunggah gambar: ${e.message}",
                        isUploading = false,
                        uploadProgress = 0f
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update {
            CloudinaryUiState()
        }
    }

    fun resetErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun resetImageUrl() {
        _uiState.update {
            it.copy(imageUrl = null)
        }
    }
}