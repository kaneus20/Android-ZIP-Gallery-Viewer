package id.flwi.zipgalleryviewer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Zip Gallery Viewer.
 * Sets up Hilt dependency injection for the application.
 */
@HiltAndroidApp
class ZipGalleryViewerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}
