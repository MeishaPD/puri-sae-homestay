package brawijaya.example.purisaehomestay.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brawijaya.example.purisaehomestay.data.model.NewsData
import brawijaya.example.purisaehomestay.data.repositories.NewsRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _newsState = MutableStateFlow<NewsState>(NewsState.Loading)
    val newsState: StateFlow<NewsState> = _newsState.asStateFlow()

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _newsState.value = NewsState.Loading
            try {
                val news = newsRepository.getNews()
                _newsState.value = NewsState.Success(news)
            } catch (e: FirebaseFirestoreException) {
                _newsState.value = NewsState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    sealed class NewsState {
        object Loading : NewsState()
        data class Success(val news: List<NewsData>) : NewsState()
        data class Error(val message: String) : NewsState()
    }
}