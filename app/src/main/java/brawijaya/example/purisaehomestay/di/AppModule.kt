package brawijaya.example.purisaehomestay.di

import brawijaya.example.purisaehomestay.data.repository.PackageRepository
import brawijaya.example.purisaehomestay.data.repository.AuthRepository
import brawijaya.example.purisaehomestay.data.repository.AuthRepositoryImpl
import brawijaya.example.purisaehomestay.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore)
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