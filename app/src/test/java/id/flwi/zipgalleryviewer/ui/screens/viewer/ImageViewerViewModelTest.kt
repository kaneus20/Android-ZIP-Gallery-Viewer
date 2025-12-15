package id.flwi.zipgalleryviewer.ui.screens.viewer

import android.content.Context
import androidx.core.net.toUri
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for ImageViewerViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewerViewModelTest {

    @Mock
    private lateinit var context: Context

    private lateinit var viewModel: ImageViewerViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testImages = listOf(
        ImageEntry("img1.jpg", "img1.jpg", null, "file://img1.jpg".toUri(), null, "image/jpeg"),
        ImageEntry("img2.jpg", "img2.jpg", null, "file://img2.jpg".toUri(), null, "image/jpeg"),
        ImageEntry("img3.jpg", "img3.jpg", null, "file://img3.jpg".toUri(), null, "image/jpeg")
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ImageViewerViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize sets images and current index`() = runTest {
        // Act
        viewModel.initialize(testImages, 1)
        advanceUntilIdle()

        // Assert
        assertEquals(testImages, viewModel.images.value)
        assertEquals(1, viewModel.currentIndex.value)
    }

    @Test
    fun `initialize with out of bounds index coerces to valid range`() = runTest {
        // Act - index too high
        viewModel.initialize(testImages, 10)
        advanceUntilIdle()

        // Assert
        assertEquals(2, viewModel.currentIndex.value) // Last valid index
    }

    @Test
    fun `initialize with negative index coerces to zero`() = runTest {
        // Act
        viewModel.initialize(testImages, -1)
        advanceUntilIdle()

        // Assert
        assertEquals(0, viewModel.currentIndex.value)
    }

    @Test
    fun `updateCurrentIndex changes the current index`() = runTest {
        // Arrange
        viewModel.initialize(testImages, 0)
        advanceUntilIdle()

        // Act
        viewModel.updateCurrentIndex(2)
        advanceUntilIdle()

        // Assert
        assertEquals(2, viewModel.currentIndex.value)
    }

    @Test
    fun `updateCurrentIndex with same index does not change state`() = runTest {
        // Arrange
        viewModel.initialize(testImages, 1)
        advanceUntilIdle()
        val initialIndex = viewModel.currentIndex.value

        // Act
        viewModel.updateCurrentIndex(1)
        advanceUntilIdle()

        // Assert
        assertEquals(initialIndex, viewModel.currentIndex.value)
    }

    @Test
    fun `updateCurrentIndex with out of bounds index is ignored`() = runTest {
        // Arrange
        viewModel.initialize(testImages, 1)
        advanceUntilIdle()

        // Act
        viewModel.updateCurrentIndex(10)
        advanceUntilIdle()

        // Assert
        assertEquals(1, viewModel.currentIndex.value) // Unchanged
    }

    @Test
    fun `updateCurrentIndex with negative index is ignored`() = runTest {
        // Arrange
        viewModel.initialize(testImages, 1)
        advanceUntilIdle()

        // Act
        viewModel.updateCurrentIndex(-1)
        advanceUntilIdle()

        // Assert
        assertEquals(1, viewModel.currentIndex.value) // Unchanged
    }

    @Test
    fun `images list remains unchanged after initialization`() = runTest {
        // Arrange
        viewModel.initialize(testImages, 0)
        advanceUntilIdle()

        // Act
        viewModel.updateCurrentIndex(2)
        advanceUntilIdle()

        // Assert
        assertEquals(testImages, viewModel.images.value)
    }
}
