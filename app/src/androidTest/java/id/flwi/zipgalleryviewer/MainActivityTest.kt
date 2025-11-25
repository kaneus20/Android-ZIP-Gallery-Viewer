package id.flwi.zipgalleryviewer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import id.flwi.zipgalleryviewer.ui.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumentation test for app startup and cleanup functionality.
 * Verifies that the app clears old data on launch and displays the Load screen.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var testDirectory: File

    @Before
    fun setup() {
        hiltRule.inject()
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        testDirectory = context.getExternalFilesDir(null)!!
    }

    @After
    fun teardown() {
        // Clean up any test files
        testDirectory.listFiles()?.forEach { it.deleteRecursively() }
    }

    @Test
    fun app_launches_and_displays_load_screen() {
        // Then: Load icon should be displayed
        composeTestRule
            .onNodeWithContentDescription("Load")
            .assertIsDisplayed()
    }

    @Test
    fun app_cleans_existing_content_on_startup() {
        // Given: Create some test files in the external files directory
        val testFile1 = File(testDirectory, "old_file.txt")
        val testSubDir = File(testDirectory, "old_directory")
        val testFile2 = File(testSubDir, "nested_file.txt")
        
        testSubDir.mkdirs()
        testFile1.writeText("old content")
        testFile2.writeText("nested content")
        
        // Verify files exist before launching
        assert(testFile1.exists())
        assert(testFile2.exists())
        assert(testSubDir.exists())
        
        // When: App is launched (MainActivity is created by the rule)
        // Wait for UI to be ready
        composeTestRule.waitForIdle()
        
        // Then: Old files should be deleted
        assert(!testFile1.exists()) { "Test file 1 should be deleted" }
        assert(!testFile2.exists()) { "Test file 2 should be deleted" }
        assert(!testSubDir.exists()) { "Test subdirectory should be deleted" }
        
        // And: Load screen should be displayed
        composeTestRule
            .onNodeWithContentDescription("Load")
            .assertIsDisplayed()
    }

    @Test
    fun app_handles_empty_directory_on_startup() {
        // Given: Directory is already empty
        testDirectory.listFiles()?.forEach { it.deleteRecursively() }
        
        // When: App is launched
        composeTestRule.waitForIdle()
        
        // Then: Load screen should still be displayed
        composeTestRule
            .onNodeWithContentDescription("Load")
            .assertIsDisplayed()
    }

    @Test
    fun app_cleans_complex_directory_structure() {
        // Given: Complex nested directory structure
        val images = File(testDirectory, "images")
        val vacation = File(images, "vacation")
        val documents = File(testDirectory, "documents")
        
        images.mkdirs()
        vacation.mkdirs()
        documents.mkdirs()
        
        val img1 = File(vacation, "photo1.jpg")
        val img2 = File(images, "photo2.jpg")
        val doc1 = File(documents, "readme.txt")
        
        img1.writeText("image1")
        img2.writeText("image2")
        doc1.writeText("document")
        
        // Verify structure exists
        assert(img1.exists())
        assert(img2.exists())
        assert(doc1.exists())
        
        // When: App is launched
        composeTestRule.waitForIdle()
        
        // Then: All content should be cleaned
        assert(!img1.exists())
        assert(!img2.exists())
        assert(!doc1.exists())
        assert(!vacation.exists())
        assert(!images.exists())
        assert(!documents.exists())
        
        // And: Load screen is displayed
        composeTestRule
            .onNodeWithContentDescription("Load")
            .assertIsDisplayed()
    }
}
