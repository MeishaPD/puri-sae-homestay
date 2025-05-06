package brawijaya.example.purisaehomestay.data.repositories

import brawijaya.example.purisaehomestay.data.model.NewsData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java

@Singleton
class NewsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getNews(): List<NewsData> {
        return try {
            firestore.collection("news")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(NewsData::class.java)
        } catch (e: Exception) {
            throw e
        }
    }
}