package brawijaya.example.purisaehomestay.data.repository

import brawijaya.example.purisaehomestay.R
import brawijaya.example.purisaehomestay.data.model.NewsData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val _news = MutableStateFlow<List<NewsData>>(getInitialNews())
    val news: Flow<List<NewsData>> = _news.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun getInitialNews(): List<NewsData> {
        return listOf(
            NewsData(
                id = 1,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                date = "09/05/2025",
                imageUrl = listOf(
                    R.drawable.bungalow_group,
                    R.drawable.bungalow_single,
                    R.drawable.landscape_view,
                    R.drawable.wedding_venue
                )
            ),
            NewsData(
                id = 2,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                date = "05/09/2025",
                imageUrl = listOf(
                    R.drawable.bungalow_group,
                    R.drawable.landscape_view
                )
            ),
            NewsData(
                id = 3,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                date = "12/12/2024",
                imageUrl = listOf(
                    R.drawable.bungalow_group,
                )
            )
        )
    }

    fun getNewsSortedByLatestDate(): List<NewsData> {
        return _news.value.sortedByDescending { parseDate(it.date) }
    }

    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date(0)
        } catch (e: Exception) {
            Date(0)
        }
    }

    fun createNews(news: NewsData) {
        val currentNews = _news.value.toMutableList()

        if (currentNews.any { it.id == news.id }) return

        currentNews.add(news)
        _news.value = currentNews
    }

    fun getNewsById(id: Int): NewsData? {
        return _news.value.find { it.id == id }
    }

    fun getAllNews(): List<NewsData> {
        return _news.value
    }

    fun getAllNewsSortedByDate(): List<NewsData> {
        return getNewsSortedByLatestDate()
    }

    fun updateNews(news: NewsData) {
        val currentNews = _news.value.toMutableList()
        val index = currentNews.indexOfFirst { it.id == news.id }

        if (index != -1) {
            currentNews[index] = news
            _news.value = currentNews
        }
    }

    fun deleteNews(id: Int) {
        val currentNews = _news.value.toMutableList()
        currentNews.removeIf { it.id == id }
        _news.value = currentNews
    }
}