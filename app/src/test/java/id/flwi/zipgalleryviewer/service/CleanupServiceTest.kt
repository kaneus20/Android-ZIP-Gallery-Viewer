package id.flwi.zipgalleryviewer.service

import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

/**
 * Unit tests for CleanupService.
 * Tests the deletion logic for extracted content.
 */
@RunWith(MockitoJUnitRunner::class)
class CleanupServiceTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var cleanupService: CleanupService
    private lateinit var testDirectory: File

    @Before
    fun setup() {
        // Create a temporary directory for testing
        testDirectory = createTempDir("cleanup_test")
        
        // Mock context to return our test directory
        `when`(mockContext.getExternalFilesDir(null)).thenReturn(testDirectory)
        
        cleanupService = CleanupService(mockContext)
    }

    @After
    fun teardown() {
        // Clean up test directory
        if (testDirectory.exists()) {
            testDirectory.deleteRecursively()
        }
    }

    @Test
    fun `clearAllExtractedContent returns true when directory is empty`() {
        // Given: Empty directory
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: Should return true
        assertTrue(result)
    }

    @Test
    fun `clearAllExtractedContent deletes single file`() {
        // Given: Directory with one file
        val testFile = File(testDirectory, "test.txt")
        testFile.writeText("test content")
        assertTrue(testFile.exists())
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: File should be deleted and return true
        assertTrue(result)
        assertFalse(testFile.exists())
    }

    @Test
    fun `clearAllExtractedContent deletes multiple files`() {
        // Given: Directory with multiple files
        val file1 = File(testDirectory, "file1.txt")
        val file2 = File(testDirectory, "file2.txt")
        val file3 = File(testDirectory, "file3.txt")
        
        file1.writeText("content 1")
        file2.writeText("content 2")
        file3.writeText("content 3")
        
        assertTrue(file1.exists())
        assertTrue(file2.exists())
        assertTrue(file3.exists())
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: All files should be deleted
        assertTrue(result)
        assertFalse(file1.exists())
        assertFalse(file2.exists())
        assertFalse(file3.exists())
    }

    @Test
    fun `clearAllExtractedContent deletes nested directories`() {
        // Given: Nested directory structure
        val subDir1 = File(testDirectory, "subdir1")
        val subDir2 = File(subDir1, "subdir2")
        subDir2.mkdirs()
        
        val file1 = File(testDirectory, "root.txt")
        val file2 = File(subDir1, "level1.txt")
        val file3 = File(subDir2, "level2.txt")
        
        file1.writeText("root")
        file2.writeText("level 1")
        file3.writeText("level 2")
        
        assertTrue(file1.exists())
        assertTrue(file2.exists())
        assertTrue(file3.exists())
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: All files and directories should be deleted
        assertTrue(result)
        assertFalse(file1.exists())
        assertFalse(file2.exists())
        assertFalse(file3.exists())
        assertFalse(subDir1.exists())
        assertFalse(subDir2.exists())
        
        // But the root directory should still exist
        assertTrue(testDirectory.exists())
    }

    @Test
    fun `clearAllExtractedContent handles null directory`() {
        // Given: Context returns null for external files directory
        `when`(mockContext.getExternalFilesDir(null)).thenReturn(null)
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: Should return true (nothing to clean)
        assertTrue(result)
    }

    @Test
    fun `clearAllExtractedContent handles non-existent directory`() {
        // Given: Directory that doesn't exist
        val nonExistentDir = File(testDirectory, "does_not_exist")
        `when`(mockContext.getExternalFilesDir(null)).thenReturn(nonExistentDir)
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: Should return true (nothing to clean)
        assertTrue(result)
    }

    @Test
    fun `clearAllExtractedContent deletes complex directory structure`() {
        // Given: Complex nested structure with multiple files and folders
        val images = File(testDirectory, "images")
        val vacation = File(images, "vacation")
        val work = File(images, "work")
        val documents = File(testDirectory, "documents")
        
        images.mkdirs()
        vacation.mkdirs()
        work.mkdirs()
        documents.mkdirs()
        
        val img1 = File(vacation, "beach.jpg")
        val img2 = File(vacation, "sunset.jpg")
        val img3 = File(work, "meeting.jpg")
        val doc1 = File(documents, "report.pdf")
        
        img1.writeText("image1")
        img2.writeText("image2")
        img3.writeText("image3")
        doc1.writeText("document")
        
        // Verify structure exists
        assertTrue(vacation.exists())
        assertTrue(work.exists())
        assertTrue(documents.exists())
        assertTrue(img1.exists())
        assertTrue(img2.exists())
        assertTrue(img3.exists())
        assertTrue(doc1.exists())
        
        // When: Clear is called
        val result = cleanupService.clearAllExtractedContent()
        
        // Then: Everything should be deleted
        assertTrue(result)
        assertFalse(img1.exists())
        assertFalse(img2.exists())
        assertFalse(img3.exists())
        assertFalse(doc1.exists())
        assertFalse(vacation.exists())
        assertFalse(work.exists())
        assertFalse(images.exists())
        assertFalse(documents.exists())
        
        // Root should still exist
        assertTrue(testDirectory.exists())
    }
}
