package brawijaya.example.purisaehomestay.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.data.repository.CloudinaryRepository
import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedPaket: Paket? = null,
    val packageList: List<Paket> = emptyList(),
    val uploadingImage: Boolean = false,
    val imageUrl: String? = null
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: PackageRepository,
    private val cloudinaryRepository: CloudinaryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        observePackageList()
    }

    private fun observePackageList() {
        viewModelScope.launch {
            repository.packages
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            errorMessage = "Gagal memuat daftar paket: ${e.message}",
                            isLoading = false
                        )
                    }
                }
                .collect { paket ->
                    _uiState.update { it.copy(packageList = paket, isLoading = false) }
                }
        }
    }

    fun getPaketById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val paket = repository.getPackageById(id)
                _uiState.update { it.copy(selectedPaket = paket, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengambil data paket: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createPackage(paket: Paket) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.createPackage(paket)
                _uiState.update { it.copy(errorMessage = null, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal menambahkan paket: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updatePackage(paket: Paket) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.updatePackage(paket)
                _uiState.update { it.copy(errorMessage = null, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal memperbarui paket: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deletePackage(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.deletePackage(id)
                _uiState.update { it.copy(errorMessage = null, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal menghapus paket: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(uploadingImage = true) }
            try {
                val cloudinaryUrl = cloudinaryRepository.uploadImage(context, uri)
                _uiState.update { it.copy(imageUrl = cloudinaryUrl, uploadingImage = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengunggah gambar: ${e.message}",
                        uploadingImage = false
                    )
                }
            }
        }
    }

    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updateErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun resetSelectedPaket() {
        _uiState.update { it.copy(selectedPaket = null) }
    }

    fun resetImageUrl() {
        _uiState.update { it.copy(imageUrl = null) }
    }
}