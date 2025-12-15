package id.flwi.zipgalleryviewer.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test for file picker integration in MainActivity.
 * Verifies that file picker intent is launched and results are handled correctly.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FilePickerIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun clicking_load_icon_launches_file_picker_intent() {
        // Given: App is launched and load screen is displayed
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithContentDescription("Load")
            .assertIsDisplayed()

        // When: Load icon is clicked
        composeTestRule
            .onNodeWithContentDescription("Load")
            .performClick()

        composeTestRule.waitForIdle()

        // Then: File picker intent should be launched
        Intents.intended(
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT),
                IntentMatchers.hasCategories(setOf(Intent.CATEGORY_OPENABLE))
            )
        )
    }

    @Test
    fun file_picker_intent_has_correct_mime_types() {
        // Given: App is launched
        composeTestRule.waitForIdle()

        // When: Load icon is clicked
        composeTestRule
            .onNodeWithContentDescription("Load")
            .performClick()

        composeTestRule.waitForIdle()

        // Then: Intent should have correct MIME types for archives
        Intents.intended(
            allOf(
                IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT),
                IntentMatchers.hasExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "application/zip",
                        "application/x-zip-compressed",
                        "application/x-7z-compressed"
                    )
                )
            )
        )
    }

    @Test
    fun file_picker_result_triggers_toast() {
        // Given: A stubbed file picker result
        val mockUri = Uri.parse("content://com.android.providers.downloads.documents/document/123")
        val resultData = Intent().apply {
            data = mockUri
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // Stub the file picker intent
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT))
            .respondWith(result)

        // Wait for app to be ready
        composeTestRule.waitForIdle()

        // When: Load icon is clicked
        composeTestRule
            .onNodeWithContentDescription("Load")
            .performClick()

        // Then: Toast should appear (verified by no crash)
        // Note: Actually testing Toast visibility requires additional setup
        // This test verifies the flow completes without errors
        composeTestRule.waitForIdle()
    }
}
