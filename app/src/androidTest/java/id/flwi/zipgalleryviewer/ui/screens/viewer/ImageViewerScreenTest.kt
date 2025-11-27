package id.flwi.zipgalleryviewer.ui.screens.viewer

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for ImageViewerScreen UI.
 */
@RunWith(AndroidJUnit4::class)
class ImageViewerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var viewModel: ImageViewerViewModel

    private val testImages = listOf(
        ImageEntry("img1.jpg", "Image 1", null, "".toUri(), null, "image/jpeg"),
        ImageEntry("img2.jpg", "Image 2", null, "".toUri(), null, "image/jpeg"),
        ImageEntry("img3.jpg", "Image 3", null, "".toUri(), null, "image/jpeg")
    )

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = ImageViewerViewModel(context)
    }

    @Test
    fun imageViewerScreen_displaysInitialImage() {
        // Arrange
        viewModel.initialize(testImages, 0)
        var backPressed = false

        // Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ImageViewerScreen(
                    viewModel = viewModel,
                    onBackPressed = { backPressed = true }
                )
            }
        }

        // Assert - Initial image should be displayed
        composeTestRule.onNodeWithContentDescription("Image 1")
            .assertIsDisplayed()
    }

    @Test
    fun imageViewerScreen_swipeLeft_showsNextImage() {
        // Arrange
        viewModel.initialize(testImages, 0)

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ImageViewerScreen(
                    viewModel = viewModel,
                    onBackPressed = {}
                )
            }
        }

        // Act - Swipe left to go to next image
        composeTestRule.onNodeWithContentDescription("Image 1")
            .performTouchInput { swipeLeft() }

        // Allow pager animation to complete
        composeTestRule.waitForIdle()

        // Assert - Should show second image
        composeTestRule.onNodeWithContentDescription("Image 2")
            .assertIsDisplayed()
    }

    @Test
    fun imageViewerScreen_swipeRight_showsPreviousImage() {
        // Arrange - Start at second image
        viewModel.initialize(testImages, 1)

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ImageViewerScreen(
                    viewModel = viewModel,
                    onBackPressed = {}
                )
            }
        }

        // Act - Swipe right to go to previous image
        composeTestRule.onNodeWithContentDescription("Image 2")
            .performTouchInput { swipeRight() }

        // Allow pager animation to complete
        composeTestRule.waitForIdle()

        // Assert - Should show first image
        composeTestRule.onNodeWithContentDescription("Image 1")
            .assertIsDisplayed()
    }

    @Test
    fun imageViewerScreen_atFirstImage_cannotSwipeRight() {
        // Arrange - Start at first image
        viewModel.initialize(testImages, 0)

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ImageViewerScreen(
                    viewModel = viewModel,
                    onBackPressed = {}
                )
            }
        }

        // Act - Try to swipe right (should stay at first image)
        composeTestRule.onNodeWithContentDescription("Image 1")
            .performTouchInput { swipeRight() }

        composeTestRule.waitForIdle()

        // Assert - Should still show first image
        composeTestRule.onNodeWithContentDescription("Image 1")
            .assertIsDisplayed()
    }

    @Test
    fun imageViewerScreen_atLastImage_cannotSwipeLeft() {
        // Arrange - Start at last image
        viewModel.initialize(testImages, 2)

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ImageViewerScreen(
                    viewModel = viewModel,
                    onBackPressed = {}
                )
            }
        }

        // Act - Try to swipe left (should stay at last image)
        composeTestRule.onNodeWithContentDescription("Image 3")
            .performTouchInput { swipeLeft() }

        composeTestRule.waitForIdle()

        // Assert - Should still show last image
        composeTestRule.onNodeWithContentDescription("Image 3")
            .assertIsDisplayed()
    }
}
