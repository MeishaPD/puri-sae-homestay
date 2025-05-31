package brawijaya.example.purisaehomestay.di

import brawijaya.example.purisaehomestay.data.repository.FCMTokenManager
import brawijaya.example.purisaehomestay.data.repository.NotificationManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationManager(): NotificationManager {
        return NotificationManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideFCMTokenManager(): FCMTokenManager {
        return FCMTokenManager()
    }
}