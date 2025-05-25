package brawijaya.example.purisaehomestay.data.repository

import brawijaya.example.purisaehomestay.data.model.NewsData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val _news = MutableStateFlow<List<NewsData>>(emptyList())
    val news: Flow<List<NewsData>> = _news.asStateFlow()

    private val newsCollection = firestore.collection("news")

    suspend fun fetchAllNews(): List<NewsData> {
        return try {
            val result = newsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val newsList = result.toObjects(NewsData::class.java)
            _news.value = newsList
            newsList
        } catch (e: Exception) {
            throw Exception("Failed to fetch news: ${e.message}")
        }
    }

    suspend fun getNewsById(id: String): NewsData? {
        return try {
            val document = newsCollection.document(id).get().await()
            document.toObject(NewsData::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to get news by ID: ${e.message}")
        }
    }

    suspend fun createNews(news: NewsData): String {
        return try {
            val docRef = newsCollection.document()
            val newsWithId = news.copy(
                id = docRef.id,
                createdAt = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )
            docRef.set(newsWithId).await()

            val currentNews = _news.value.toMutableList()
            currentNews.add(0, newsWithId)
            _news.value = currentNews

            docRef.id
        } catch (e: Exception) {
            throw Exception("Failed to create news: ${e.message}")
        }
    }

    suspend fun updateNews(news: NewsData) {
        try {
            val updatedNews = news.copy(updatedAt = com.google.firebase.Timestamp.now())
            newsCollection.document(news.id).set(updatedNews).await()

            val currentNews = _news.value.toMutableList()
            val index = currentNews.indexOfFirst { it.id == news.id }
            if (index != -1) {
                currentNews[index] = updatedNews
                _news.value = currentNews
            }
        } catch (e: Exception) {
            throw Exception("Failed to update news: ${e.message}")
        }
    }

    suspend fun deleteNews(id: String) {
        try {
            newsCollection.document(id).delete().await()

            val currentNews = _news.value.toMutableList()
            currentNews.removeIf { it.id == id }
            _news.value = currentNews
        } catch (e: Exception) {
            throw Exception("Failed to delete news: ${e.message}")
        }
    }

    fun getNewsSortedByLatestDate(): List<NewsData> {
        return _news.value.sortedByDescending { it.createdAt.seconds }
    }
}