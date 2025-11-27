package id.flwi.zipgalleryviewer.ui.screens.gallery

import androidx.core.net.toUri
import id.flwi.zipgalleryviewer.data.FileRepository
import id.flwi.zipgalleryviewer.data.model.FolderEntry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
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
        viewModel = GalleryViewModel(fileRepository)
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

        viewModel = GalleryViewModel(fileRepository)
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

        viewModel = GalleryViewModel(fileRepository)
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

        viewModel = GalleryViewModel(fileRepository)
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

        viewModel = GalleryViewModel(fileRepository)
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
        viewModel = GalleryViewModel(fileRepository)
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
        viewModel = GalleryViewModel(fileRepository)
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
        viewModel = GalleryViewModel(fileRepository)
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

        viewModel = GalleryViewModel(fileRepository)
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

        viewModel = GalleryViewModel(fileRepository)
        advanceUntilIdle()

        viewModel.navigateToFolder("folder1")
        advanceUntilIdle()

        // Act
        viewModel.navigateUp()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.isAtRoot.value)
    }
}
