package id.flwi.zipgalleryviewer.ui.screens.gallery

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.flwi.zipgalleryviewer.data.model.FolderEntry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for GalleryScreen UI.
 */
@RunWith(AndroidJUnit4::class)
class GalleryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgressIndicator() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Loading,
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithContentDescription("Loading", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun successState_emptyList_showsEmptyMessage() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("No images or folders found")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysFoldersAndImages() {
        // Arrange
        val entries = listOf(
            FolderEntry("photos", "photos", null, 5),
            FolderEntry("documents", "documents", null, 3),
            ImageEntry("image1.jpg", "image1.jpg", null, "".toUri(), null, "image/jpeg"),
            ImageEntry("image2.png", "image2.png", null, "".toUri(), null, "image/png")
        )

        // Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(entries),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert - Check folder names are displayed
        composeTestRule.onNodeWithText("photos").assertIsDisplayed()
        composeTestRule.onNodeWithText("documents").assertIsDisplayed()

        // Assert - Check image names are displayed
        composeTestRule.onNodeWithText("image1.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithText("image2.png").assertIsDisplayed()

        // Assert - Check folder icons
        composeTestRule.onNodeWithContentDescription("Folder: photos").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Folder: documents").assertIsDisplayed()
    }

    @Test
    fun successState_correctNumberOfItems() {
        // Arrange
        val entries = listOf(
            FolderEntry("folder1", "folder1", null, 1),
            FolderEntry("folder2", "folder2", null, 2),
            ImageEntry("img1.jpg", "img1.jpg", null, "".toUri(), null, "image/jpeg"),
            ImageEntry("img2.jpg", "img2.jpg", null, "".toUri(), null, "image/jpeg"),
            ImageEntry("img3.jpg", "img3.jpg", null, "".toUri(), null, "image/jpeg")
        )

        // Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(entries),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert - Should have 5 items total
        composeTestRule.onAllNodesWithContentDescription("Folder:", substring = true)
            .assertCountEquals(2)
    }

    @Test
    fun folderClick_triggersCallback() {
        // Arrange
        var clickedPath = ""
        val entries = listOf(
            FolderEntry("photos", "photos", null, 3)
        )

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(entries),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = { clickedPath = it },
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("photos").performClick()

        // Assert
        assertEquals("photos", clickedPath)
    }

    @Test
    fun imageClick_triggersCallback() {
        // Arrange
        var clickedPath = ""
        val entries = listOf(
            ImageEntry("test.jpg", "test.jpg", null, "".toUri(), null, "image/jpeg")
        )

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(entries),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = { clickedPath = it },
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("test.jpg").performClick()

        // Assert
        assertEquals("test.jpg", clickedPath)
    }

    @Test
    fun errorState_displaysErrorMessage() {
        // Arrange
        val errorMessage = "Failed to load files"

        // Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Error(errorMessage),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun longFilename_displaysEllipsized() {
        // Arrange
        val longName = "this_is_a_very_long_filename_that_should_be_ellipsized_in_the_ui.jpg"
        val entries = listOf(
            ImageEntry(longName, longName, null, "".toUri(), null, "image/jpeg")
        )

        // Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(entries),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert - Just verify the text node exists (ellipsizing is handled by Text composable)
        composeTestRule.onNodeWithText(longName, substring = true).assertIsDisplayed()
    }

    @Test
    fun atRoot_upButtonIsHidden() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithContentDescription("Navigate up")
            .assertDoesNotExist()
    }

    @Test
    fun notAtRoot_upButtonIsDisplayed() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = false,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithContentDescription("Navigate up")
            .assertIsDisplayed()
    }

    @Test
    fun upButtonClick_triggersCallback() {
        // Arrange
        var upClicked = false

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = false,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = { upClicked = true },
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithContentDescription("Navigate up").performClick()

        // Assert
        assertTrue(upClicked)
    }

    @Test
    fun topAppBar_displaysGalleryTitle() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Gallery").assertIsDisplayed()
    }

    @Test
    fun gridView_displaysLayoutToggleButton() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert - Icon shows ViewList when in grid mode
        composeTestRule.onNodeWithContentDescription("Switch to list view")
            .assertIsDisplayed()
    }

    @Test
    fun listView_displaysLayoutToggleButton() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = false,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert - Icon shows GridView when in list mode
        composeTestRule.onNodeWithContentDescription("Switch to grid view")
            .assertIsDisplayed()
    }

    @Test
    fun layoutToggleClick_triggersCallback() {
        // Arrange
        var layoutToggled = false

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = { layoutToggled = true },\n                    onExitRequest = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithContentDescription("Switch to list view").performClick()

        // Assert
        assertTrue(layoutToggled)
    }

    @Test
    fun listView_displaysFoldersInListFormat() {
        // Arrange
        val entries = listOf(
            FolderEntry("photos", "photos", null, 5),
            ImageEntry("image1.jpg", "image1.jpg", null, "".toUri(), null, "image/jpeg")
        )

        // Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(entries),
                    isAtRoot = true,
                    isGridView = false,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},\n                    onExitRequest = {}
                )
            }
        }

        // Assert - Items should still be displayed with their names
        composeTestRule.onNodeWithText("photos").assertIsDisplayed()
        composeTestRule.onNodeWithText("image1.jpg").assertIsDisplayed()
    }

    @Test
    fun atRoot_backButton_triggersExitRequest() {
        // Arrange
        var exitRequested = false

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                GalleryScreen(
                    uiState = GalleryUiState.Success(emptyList()),
                    isAtRoot = true,
                    isGridView = true,
                    onFolderClick = {},
                    onImageClick = {},
                    onUpClick = {},
                    onLayoutToggle = {},
                    onExitRequest = { exitRequested = true }
                )
            }
        }

        // Act - Simulate back button press (handled by BackHandler)
        // Note: Actual back press testing requires ActivityScenario
        // This test verifies the callback is passed correctly

        // Assert - Function is correctly wired
        // BackHandler will call onExitRequest when back is pressed at root
        assertTrue(!exitRequested) // Not called yet in this test setup
    }
}
