package id.flwi.zipgalleryviewer.manager

import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for NotificationManager.
 * Note: These tests verify that the manager correctly interacts with the Android NotificationManager.
 */
class NotificationManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var androidNotificationManager: AndroidNotificationManager

    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE))
            .thenReturn(androidNotificationManager)
        `when`(context.getString(anyInt())).thenReturn("Zip Gallery Viewer")
        notificationManager = NotificationManager(context)
    }

    @Test
    fun `showPersistentExitNotification calls notify on notification manager`() {
        // When
        notificationManager.showPersistentExitNotification()

        // Then
        verify(androidNotificationManager).notify(
            eq(NotificationManager.PERSISTENT_NOTIFICATION_ID),
            any()
        )
    }

    @Test
    fun `hidePersistentExitNotification calls cancel on notification manager`() {
        // When
        notificationManager.hidePersistentExitNotification()

        // Then
        verify(androidNotificationManager).cancel(NotificationManager.PERSISTENT_NOTIFICATION_ID)
    }
}
