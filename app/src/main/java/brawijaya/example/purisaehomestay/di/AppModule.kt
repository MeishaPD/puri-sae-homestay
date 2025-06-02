package brawijaya.example.purisaehomestay.di

import android.content.Context
import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import brawijaya.example.purisaehomestay.data.repository.AuthRepository
import brawijaya.example.purisaehomestay.data.repository.AuthRepositoryImpl
import brawijaya.example.purisaehomestay.data.repository.UserRepository
import brawijaya.example.purisaehomestay.utils.RateLimitManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideRateLimitManager(@ApplicationContext context: Context): RateLimitManager {
        return RateLimitManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        rateLimitManager: RateLimitManager
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore, rateLimitManager)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository {
        return UserRepository(auth, firestore)
    }

    @Provides
    @Singleton
    fun providePackageRepository(
        firestore: FirebaseFirestore
    ): PackageRepository {
        return PackageRepository(firestore)
    }
}