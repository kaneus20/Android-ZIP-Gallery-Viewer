package id.flwi.zipgalleryviewer.manager

import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import id.flwi.zipgalleryviewer.R
import id.flwi.zipgalleryviewer.ZipGalleryViewerApplication
import id.flwi.zipgalleryviewer.ui.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notifications for the Zip Gallery Viewer application.
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val PERSISTENT_NOTIFICATION_ID = 1
        const val ACTION_EXIT_APP = "id.flwi.zipgalleryviewer.ACTION_EXIT_APP"
    }

    private val notificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    /**
     * Displays a persistent notification that allows the user to close the app.
     */
    fun showPersistentExitNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_EXIT_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            ZipGalleryViewerApplication.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Tap to close application")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(PERSISTENT_NOTIFICATION_ID, notification)
    }

    /**
     * Dismisses the persistent exit notification.
     */
    fun hidePersistentExitNotification() {
        notificationManager.cancel(PERSISTENT_NOTIFICATION_ID)
    }
}
