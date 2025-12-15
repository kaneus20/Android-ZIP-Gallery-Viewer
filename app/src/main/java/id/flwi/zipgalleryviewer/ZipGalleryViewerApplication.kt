package id.flwi.zipgalleryviewer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Zip Gallery Viewer.
 * Sets up Hilt dependency injection for the application.
 */
@HiltAndroidApp
class ZipGalleryViewerApplication : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "zip_gallery_viewer_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Zip Gallery Viewer"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for the application.
     * Required for Android O (API 26) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Zip Gallery Viewer"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
