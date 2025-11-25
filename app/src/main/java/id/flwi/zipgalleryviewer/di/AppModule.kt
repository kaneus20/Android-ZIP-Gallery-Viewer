package id.flwi.zipgalleryviewer.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.flwi.zipgalleryviewer.manager.FileSelectionModule
import id.flwi.zipgalleryviewer.service.CleanupService
import javax.inject.Singleton

/**
 * Hilt module providing app-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCleanupService(
        @ApplicationContext context: Context
    ): CleanupService {
        return CleanupService(context)
    }

    @Provides
    @Singleton
    fun provideFileSelectionModule(): FileSelectionModule {
        return FileSelectionModule()
    }
}
