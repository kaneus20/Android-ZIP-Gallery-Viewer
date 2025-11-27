package id.flwi.zipgalleryviewer.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for ExitConfirmationDialog.
 */
@RunWith(AndroidJUnit4::class)
class ExitConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dialog_displaysCorrectTitle() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ExitConfirmationDialog(
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Exit Application").assertIsDisplayed()
    }

    @Test
    fun dialog_displaysCorrectMessage() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ExitConfirmationDialog(
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Are you sure you want to leave this app?").assertIsDisplayed()
    }

    @Test
    fun dialog_displaysConfirmButton() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ExitConfirmationDialog(
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Yes, clear and exit").assertIsDisplayed()
    }

    @Test
    fun dialog_displaysCancelButton() {
        // Arrange & Act
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ExitConfirmationDialog(
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun cancelButton_triggersOnDismiss() {
        // Arrange
        var dismissCalled = false

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ExitConfirmationDialog(
                    onConfirm = {},
                    onDismiss = { dismissCalled = true }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Assert
        assertTrue(dismissCalled)
    }

    @Test
    fun confirmButton_triggersOnConfirm() {
        // Arrange
        var confirmCalled = false

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                ExitConfirmationDialog(
                    onConfirm = { confirmCalled = true },
                    onDismiss = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Yes, clear and exit").performClick()

        // Assert
        assertTrue(confirmCalled)
    }
}
