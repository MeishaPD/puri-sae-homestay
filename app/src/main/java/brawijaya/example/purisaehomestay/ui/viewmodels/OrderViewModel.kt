package brawijaya.example.purisaehomestay.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.Paket
import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedPaket: Paket? = null,
    val packageList: List<Paket> = emptyList()
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: PackageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        observePackageList()
    }

    private fun observePackageList() {
        viewModelScope.launch {
            repository.packages.collect { paket ->
                _uiState.update { it.copy(packageList = paket) }
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

    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updateErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun resetSelectedPaket() {
        _uiState.update { it.copy(selectedPaket = null) }
    }
}