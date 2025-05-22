package brawijaya.example.purisaehomestay.di

import android.content.Context
import brawijaya.example.purisaehomestay.data.repository.CloudinaryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideCloudinaryRepository(
        @ApplicationContext context: Context
    ): CloudinaryRepository {
        return CloudinaryRepository(context)
    }
}