package id.flwi.zipgalleryviewer.ui.screens.load

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for LoadScreen.
 * Tests the UI behavior and file picker integration.
 */
@RunWith(AndroidJUnit4::class)
class LoadScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun loadScreen_displays_load_icon() {
        // Given: LoadScreen is rendered
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                LoadScreen()
            }
        }

        // Then: Load icon should be displayed
        composeTestRule
            .onNodeWithContentDescription("Load")
            .assertIsDisplayed()
    }

    @Test
    fun loadScreen_clicking_icon_triggers_callback() {
        // Given: A callback tracker
        var callbackTriggered = false

        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                LoadScreen(
                    onLoadClicked = { callbackTriggered = true }
                )
            }
        }

        // When: Load icon is clicked
        composeTestRule
            .onNodeWithContentDescription("Load")
            .performClick()

        // Then: Callback should be triggered
        assert(callbackTriggered)
    }

    @Test
    fun loadScreen_icon_is_clickable() {
        // Given: LoadScreen is rendered
        composeTestRule.setContent {
            ZipGalleryViewerTheme {
                LoadScreen()
            }
        }

        // When/Then: Icon should be clickable (no exception thrown)
        composeTestRule
            .onNodeWithContentDescription("Load")
            .performClick()
    }
}
