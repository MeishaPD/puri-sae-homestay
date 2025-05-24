package brawijaya.example.purisaehomestay.data.repository

import android.util.Log
import brawijaya.example.purisaehomestay.data.model.Paket
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk mengelola paket-paket penginapan
 * Menggunakan Firebase Firestore sebagai sumber data
 */
@Singleton
class PackageRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val packageCollection = db.collection("package")

    /**
     * Mendapatkan semua paket sebagai Flow
     */
    val packages: Flow<List<Paket>> = callbackFlow {
        val listener = packageCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val packageList = snapshot?.documents?.mapNotNull { document ->
                try {
                    document.toObject<Paket>()?.copy(id = document.id.hashCode())
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            Log.d("REPOSITORY", "Package list: ${packageList}")

            trySend(packageList)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Membuat paket baru
     */
    suspend fun createPackage(paket: Paket) {
        try {
            packageCollection.add(paket).await()
        } catch (e: Exception) {
            throw Exception("Failed to create package: ${e.message}")
        }
    }

    /**
     * Mengambil paket berdasarkan ID
     */
    suspend fun getPackageById(id: Int): Paket? {
        return try {
            val snapshot = packageCollection.get().await()
            snapshot.documents.find { it.id.hashCode() == id }?.toObject<Paket>()?.copy(id = id)
        } catch (e: Exception) {
            throw Exception("Failed to get package: ${e.message}")
        }
    }

    /**
     * Mengambil semua paket
     */
    suspend fun getAllPackages(): List<Paket> {
        return try {
            val snapshot = packageCollection.get().await()
            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<Paket>()?.copy(id = document.id.hashCode())
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to get packages: ${e.message}")
        }
    }

    /**
     * Mengubah data paket
     */
    suspend fun updatePackage(paket: Paket) {
        try {
            val snapshot = packageCollection.get().await()
            val document = snapshot.documents.find { it.id.hashCode() == paket.id }

            document?.let {
                packageCollection.document(it.id).set(paket).await()
            } ?: throw Exception("Package not found")
        } catch (e: Exception) {
            throw Exception("Failed to update package: ${e.message}")
        }
    }

    /**
     * Menghapus paket berdasarkan ID
     */
    suspend fun deletePackage(id: Int) {
        try {
            val snapshot = packageCollection.get().await()
            val document = snapshot.documents.find { it.id.hashCode() == id }

            document?.let {
                packageCollection.document(it.id).delete().await()
            } ?: throw Exception("Package not found")
        } catch (e: Exception) {
            throw Exception("Failed to delete package: ${e.message}")
        }
    }

    /**
     * Menghapus paket berdasarkan document ID
     */
    suspend fun deletePackageByDocumentId(documentId: String) {
        try {
            packageCollection.document(documentId).delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete package: ${e.message}")
        }
    }
}