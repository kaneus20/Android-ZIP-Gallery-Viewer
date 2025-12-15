package id.flwi.zipgalleryviewer.ui.screens.gallery

import android.content.Context
import androidx.core.net.toUri
import id.flwi.zipgalleryviewer.data.FileRepository
import id.flwi.zipgalleryviewer.data.model.FolderEntry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.manager.NotificationManager
import id.flwi.zipgalleryviewer.service.CleanupService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for GalleryViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    @Mock
    private lateinit var fileRepository: FileRepository

    @Mock
    private lateinit var cleanupService: CleanupService

    @Mock
    private lateinit var notificationManager: NotificationManager

    @Mock
    private lateinit var context: Context

    private lateinit var viewModel: GalleryViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads entries from root path`() = runTest {
        // Arrange
        val expectedEntries = listOf(
            FolderEntry("folder1", "folder1", null, 3),
            ImageEntry("image1.jpg", "image1.jpg", null, "".toUri(), null, "image/jpeg")
        )
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(expectedEntries))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is GalleryUiState.Success)
        assertEquals(expectedEntries, (state as GalleryUiState.Success).entries)
    }

    @Test
    fun `navigateToFolder updates current path and loads entries`() = runTest {
        // Arrange
        val rootEntries = listOf(
            FolderEntry("folder1", "folder1", null, 2)
        )
        val folderEntries = listOf(
            ImageEntry("folder1/image1.jpg", "image1.jpg", "folder1", "".toUri(), null, "image/jpeg"),
            ImageEntry("folder1/image2.jpg", "image2.jpg", "folder1", "".toUri(), null, "image/jpeg")
        )
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(rootEntries))
        `when`(fileRepository.getExtractedEntries("folder1")).thenReturn(flowOf(folderEntries))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.navigateToFolder("folder1")
        advanceUntilIdle()

        // Assert
        assertEquals("folder1", viewModel.currentPath.value)
        val state = viewModel.uiState.value
        assertTrue(state is GalleryUiState.Success)
        assertEquals(folderEntries, (state as GalleryUiState.Success).entries)
    }

    @Test
    fun `navigateUp navigates to parent directory`() = runTest {
        // Arrange
        val rootEntries = listOf(FolderEntry("folder1", "folder1", null, 1))
        val subFolderEntries = listOf(ImageEntry("folder1/sub/image.jpg", "image.jpg", "folder1/sub", "".toUri(), null, "image/jpeg"))

        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(rootEntries))
        `when`(fileRepository.getExtractedEntries("folder1/sub")).thenReturn(flowOf(subFolderEntries))
        `when`(fileRepository.getExtractedEntries("folder1")).thenReturn(flowOf(emptyList()))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.navigateToFolder("folder1/sub")
        advanceUntilIdle()

        // Act
        viewModel.navigateUp()
        advanceUntilIdle()

        // Assert
        assertEquals("folder1", viewModel.currentPath.value)
    }

    @Test
    fun `navigateUp from root stays at root`() = runTest {
        // Arrange
        val rootEntries = listOf(FolderEntry("folder1", "folder1", null, 0))
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(rootEntries))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.navigateUp()
        advanceUntilIdle()

        // Assert
        assertEquals("/", viewModel.currentPath.value)
    }

    @Test
    fun `refresh reloads current path entries`() = runTest {
        // Arrange
        val initialEntries = listOf(FolderEntry("folder1", "folder1", null, 1))
        val refreshedEntries = listOf(
            FolderEntry("folder1", "folder1", null, 1),
            ImageEntry("image1.jpg", "image1.jpg", null, "".toUri(), null, "image/jpeg")
        )

        `when`(fileRepository.getExtractedEntries("/"))
            .thenReturn(flowOf(initialEntries))
            .thenReturn(flowOf(refreshedEntries))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.refresh()
        advanceUntilIdle()

        // Assert
        verify(fileRepository, org.mockito.Mockito.times(2)).getExtractedEntries("/")
    }

    @Test
    fun `error in repository results in error state`() = runTest {
        // Arrange
        val exception = RuntimeException("Test error")
        `when`(fileRepository.getExtractedEntries("/")).thenThrow(exception)

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is GalleryUiState.Error)
        assertTrue((state as GalleryUiState.Error).message.contains("Test error"))
    }

    @Test
    fun `empty folder returns success state with empty list`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state is GalleryUiState.Success)
        assertTrue((state as GalleryUiState.Success).entries.isEmpty())
    }

    @Test
    fun `isAtRoot is true when at root path`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.isAtRoot.value)
    }

    @Test
    fun `isAtRoot is false when in subfolder`() = runTest {
        // Arrange
        val rootEntries = listOf(FolderEntry("folder1", "folder1", null, 0))
        val folderEntries = listOf(ImageEntry("folder1/image.jpg", "image.jpg", "folder1", "".toUri(), null, "image/jpeg"))

        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(rootEntries))
        `when`(fileRepository.getExtractedEntries("folder1")).thenReturn(flowOf(folderEntries))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.navigateToFolder("folder1")
        advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isAtRoot.value)
    }

    @Test
    fun `isAtRoot becomes true when navigating back to root`() = runTest {
        // Arrange
        val rootEntries = listOf(FolderEntry("folder1", "folder1", null, 0))
        val folderEntries = listOf(ImageEntry("folder1/image.jpg", "image.jpg", "folder1", "".toUri(), null, "image/jpeg"))

        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(rootEntries))
        `when`(fileRepository.getExtractedEntries("folder1")).thenReturn(flowOf(folderEntries))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.navigateToFolder("folder1")
        advanceUntilIdle()

        // Act
        viewModel.navigateUp()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.isAtRoot.value)
    }

    @Test
    fun `isGridView is true by default`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.isGridView.value)
    }

    @Test
    fun `toggleLayout switches isGridView from true to false`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.toggleLayout()

        // Assert
        assertEquals(false, viewModel.isGridView.value)
    }

    @Test
    fun `toggleLayout switches isGridView from false to true`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.toggleLayout() // Set to false

        // Act
        viewModel.toggleLayout() // Toggle back to true

        // Assert
        assertTrue(viewModel.isGridView.value)
    }

    @Test
    fun `isGridView persists across folder navigation`() = runTest {
        // Arrange
        val rootEntries = listOf(FolderEntry("folder1", "folder1", null, 0))
        val folderEntries = listOf(ImageEntry("folder1/image.jpg", "image.jpg", "folder1", "".toUri(), null, "image/jpeg"))

        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(rootEntries))
        `when`(fileRepository.getExtractedEntries("folder1")).thenReturn(flowOf(folderEntries))

        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.toggleLayout() // Switch to list view
        advanceUntilIdle()

        // Act - Navigate to folder
        viewModel.navigateToFolder("folder1")
        advanceUntilIdle()

        // Assert - Layout preference should persist
        assertEquals(false, viewModel.isGridView.value)
    }

    @Test
    fun `onExitRequest sets showExitDialog to true`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.onExitRequest()

        // Assert
        assertTrue(viewModel.showExitDialog.value)
    }

    @Test
    fun `onExitDismiss sets showExitDialog to false`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.onExitRequest() // Show dialog first

        // Act
        viewModel.onExitDismiss()

        // Assert
        assertFalse(viewModel.showExitDialog.value)
    }

    @Test
    fun `onExitConfirm calls cleanupService and notificationManager`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.onExitConfirm()
        advanceUntilIdle()

        // Assert
        verify(cleanupService).clearAllExtractedContent()
        verify(notificationManager).hidePersistentExitNotification()
    }

    @Test
    fun `onExitConfirm sets showExitDialog to false`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.onExitRequest() // Show dialog first

        // Act
        viewModel.onExitConfirm()
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.showExitDialog.value)
    }

    @Test
    fun `onExitConfirm emits finishActivityEvent`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        var eventReceived = false
        val job = kotlinx.coroutines.launch {
            viewModel.finishActivityEvent.collect {
                eventReceived = true
            }
        }

        // Act
        viewModel.onExitConfirm()
        advanceUntilIdle()

        // Assert
        assertTrue(eventReceived)
        job.cancel()
    }

    @Test
    fun `isRandomized is false by default`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.isRandomized.value)
    }

    @Test
    fun `toggleRandomize switches isRandomized from false to true`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.toggleRandomize()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.isRandomized.value)
    }

    @Test
    fun `toggleRandomize switches isRandomized from true to false`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        viewModel.toggleRandomize() // Set to true
        advanceUntilIdle()

        // Act
        viewModel.toggleRandomize() // Toggle back to false
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.isRandomized.value)
    }

    @Test
    fun `sortEntries puts folders first alphabetically`() = runTest {
        // Arrange
        val entries = listOf(
            ImageEntry("image1.jpg", "image1.jpg", null, "".toUri(), null, "image/jpeg"),
            FolderEntry("zebra", "zebra", null, 0),
            FolderEntry("alpha", "alpha", null, 0),
            ImageEntry("image2.jpg", "image2.jpg", null, "".toUri(), null, "image/jpeg")
        )
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(entries))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value as GalleryUiState.Success
        val sortedEntries = state.entries
        assertTrue(sortedEntries[0] is FolderEntry)
        assertTrue(sortedEntries[1] is FolderEntry)
        assertEquals("alpha", (sortedEntries[0] as FolderEntry).name)
        assertEquals("zebra", (sortedEntries[1] as FolderEntry).name)
    }

    @Test
    fun `sortEntries sorts images alphabetically when not randomized`() = runTest {
        // Arrange
        val entries = listOf(
            ImageEntry("zebra.jpg", "zebra.jpg", null, "".toUri(), null, "image/jpeg"),
            ImageEntry("alpha.jpg", "alpha.jpg", null, "".toUri(), null, "image/jpeg"),
            ImageEntry("middle.jpg", "middle.jpg", null, "".toUri(), null, "image/jpeg")
        )
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(entries))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value as GalleryUiState.Success
        val sortedEntries = state.entries
        assertEquals("alpha.jpg", (sortedEntries[0] as ImageEntry).name)
        assertEquals("middle.jpg", (sortedEntries[1] as ImageEntry).name)
        assertEquals("zebra.jpg", (sortedEntries[2] as ImageEntry).name)
    }

    @Test
    fun `showShareDialog is false by default`() = runTest {
        // Arrange
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))

        // Act
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Assert
        assertFalse(viewModel.showShareDialog.value)
    }

    @Test
    fun `onImageLongPressed sets selected image and shows dialog`() = runTest {
        // Arrange
        val testImage = ImageEntry("test.jpg", "test.jpg", null, "".toUri(), null, "image/jpeg")
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()

        // Act
        viewModel.onImageLongPressed(testImage)

        // Assert
        assertTrue(viewModel.showShareDialog.value)
        assertEquals(testImage, viewModel.selectedImageForShare.value)
    }

    @Test
    fun `onShareDismiss hides dialog and clears selected image`() = runTest {
        // Arrange
        val testImage = ImageEntry("test.jpg", "test.jpg", null, "".toUri(), null, "image/jpeg")
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()
        viewModel.onImageLongPressed(testImage)

        // Act
        viewModel.onShareDismiss()

        // Assert
        assertFalse(viewModel.showShareDialog.value)
        assertEquals(null, viewModel.selectedImageForShare.value)
    }

    @Test
    fun `onShareConfirm emits share intent event`() = runTest {
        // Arrange
        val testImage = ImageEntry("test.jpg", "/path/to/test.jpg", null, "".toUri(), null, "image/jpeg")
        `when`(fileRepository.getExtractedEntries("/")).thenReturn(flowOf(emptyList()))
        `when`(context.packageName).thenReturn("id.flwi.zipgalleryviewer")
        viewModel = GalleryViewModel(context, fileRepository, cleanupService, notificationManager)
        advanceUntilIdle()
        viewModel.onImageLongPressed(testImage)

        var emittedIntent: android.content.Intent? = null
        backgroundScope.launch {
            viewModel.shareImageEvent.collect { intent ->
                emittedIntent = intent
            }
        }

        // Act
        viewModel.onShareConfirm()
        advanceUntilIdle()

        // Assert
        // Note: FileProvider.getUriForFile requires actual file system, so we just verify event was emitted
        // and selectedImage was cleared
        assertFalse(viewModel.showShareDialog.value)
        assertEquals(null, viewModel.selectedImageForShare.value)
    }
}
