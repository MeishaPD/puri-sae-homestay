package brawijaya.example.purisaehomestay.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val newsList: List<NewsData> = emptyList(),
    val selectedNews: NewsData? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        observeNewsList()
    }

    private fun observeNewsList() {
        viewModelScope.launch {
            newsRepository.news.collect { news ->
                _uiState.update { it.copy(newsList = news) }
            }
        }
    }

    fun fetchAllNews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val news = newsRepository.fetchAllNews()
                _uiState.update {
                    it.copy(
                        newsList = news,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengambil data berita: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getNewsById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val news = newsRepository.getNewsById(id)
                _uiState.update {
                    it.copy(
                        selectedNews = news,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal mengambil data berita: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createNews(news: NewsData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            try {
                newsRepository.createNews(news)
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal menambahkan berita: ${e.message}",
                        isCreating = false
                    )
                }
            }
        }
    }

    fun updateNews(news: NewsData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            try {
                newsRepository.updateNews(news)
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal memperbarui berita: ${e.message}",
                        isUpdating = false
                    )
                }
            }
        }
    }

    fun deleteNews(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                newsRepository.deleteNews(id)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Gagal menghapus berita: ${e.message}",
                        isDeleting = false
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

    fun resetSelectedNews() {
        _uiState.update { it.copy(selectedNews = null) }
    }

    fun refreshNews() {
        fetchAllNews()
    }
}