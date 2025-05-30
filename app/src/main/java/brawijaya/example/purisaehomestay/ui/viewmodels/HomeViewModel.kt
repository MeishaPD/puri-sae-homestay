package brawijaya.example.purisaehomestay.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.data.model.PromoData
import brawijaya.example.purisaehomestay.data.repository.NewsRepository
import brawijaya.example.purisaehomestay.data.repository.PromoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val news: List<NewsData> = emptyList(),
    val promo: List<PromoData> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val promoRepository: PromoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchPromo()
        fetchNews()
        observePromoList()
        observeNewsList()
    }

    private fun fetchNews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                newsRepository.fetchAllNews()
                Log.d("HOME", "News fetched successfully")
            } catch (e: Exception) {
                Log.e("HOME", "Failed to fetch news: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    private fun fetchPromo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                promoRepository.fetchAllPromo()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    private fun observePromoList() {
        viewModelScope.launch {
            promoRepository.promo.collect { promoList ->
                val latestPromo = promoList.sortedByDescending { it.startDate.seconds }
                _uiState.update {
                    it.copy(
                        promo = latestPromo,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun observeNewsList() {
        viewModelScope.launch {
            newsRepository.news.collect { newsList ->
                val latestNews = newsList.sortedByDescending { it.createdAt.seconds }.take(2)
                Log.d("HOME", "Latest news: $latestNews")
                _uiState.update {
                    it.copy(
                        news = latestNews,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun refreshNews() {
        fetchNews()
    }
}