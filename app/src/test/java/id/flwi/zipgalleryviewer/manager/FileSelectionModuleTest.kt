package id.flwi.zipgalleryviewer.manager

import android.content.Intent
import android.net.Uri
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for FileSelectionModule.
 * Tests the file picker intent creation and URI handling.
 */
@RunWith(RobolectricTestRunner::class)
class FileSelectionModuleTest {

    private lateinit var fileSelectionModule: FileSelectionModule

    @Before
    fun setup() {
        fileSelectionModule = FileSelectionModule()
    }

    @Test
    fun `createArchivePickerIntent should return correct intent action`() {
        // When: Creating the picker intent
        val intent = fileSelectionModule.createArchivePickerIntent()

        // Then: Intent should have ACTION_OPEN_DOCUMENT
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
    }

    @Test
    fun `createArchivePickerIntent should include CATEGORY_OPENABLE`() {
        // When: Creating the picker intent
        val intent = fileSelectionModule.createArchivePickerIntent()

        // Then: Intent should have OPENABLE category
        assertTrue(intent.categories?.contains(Intent.CATEGORY_OPENABLE) == true)
    }

    @Test
    fun `createArchivePickerIntent should set correct MIME types`() {
        // When: Creating the picker intent
        val intent = fileSelectionModule.createArchivePickerIntent()

        // Then: Intent should have correct MIME types
        val mimeTypes = intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
        assertNotNull(mimeTypes)
        assertEquals(3, mimeTypes?.size)
        assertTrue(mimeTypes?.contains("application/zip") == true)
        assertTrue(mimeTypes?.contains("application/x-zip-compressed") == true)
        assertTrue(mimeTypes?.contains("application/x-7z-compressed") == true)
    }

    @Test
    fun `createArchivePickerIntent should request read permissions`() {
        // When: Creating the picker intent
        val intent = fileSelectionModule.createArchivePickerIntent()

        // Then: Intent should have read permission flags
        val hasReadPermission = (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
        assertTrue(hasReadPermission)
    }

    @Test
    fun `createArchivePickerIntent should request persistable permissions`() {
        // When: Creating the picker intent
        val intent = fileSelectionModule.createArchivePickerIntent()

        // Then: Intent should have persistable permission flag
        val hasPersistablePermission = (intent.flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) != 0
        assertTrue(hasPersistablePermission)
    }

    @Test
    fun `getFileNameFromUri should extract filename from URI path`() {
        // Given: A mock URI with a filename in the path
        val mockUri: Uri = mock()
        whenever(mockUri.lastPathSegment).thenReturn("primary:Download/archive.zip")

        // When: Extracting filename
        val filename = fileSelectionModule.getFileNameFromUri(mockUri)

        // Then: Should return the filename
        assertEquals("archive.zip", filename)
    }

    @Test
    fun `getFileNameFromUri should return fallback when path segment is null`() {
        // Given: A mock URI with null path segment
        val mockUri: Uri = mock()
        whenever(mockUri.lastPathSegment).thenReturn(null)

        // When: Extracting filename
        val filename = fileSelectionModule.getFileNameFromUri(mockUri)

        // Then: Should return fallback name
        assertEquals("selected_archive", filename)
    }

    @Test
    fun `getFileNameFromUri should handle simple filename`() {
        // Given: A mock URI with just a filename
        val mockUri: Uri = mock()
        whenever(mockUri.lastPathSegment).thenReturn("myarchive.7z")

        // When: Extracting filename
        val filename = fileSelectionModule.getFileNameFromUri(mockUri)

        // Then: Should return the filename
        assertEquals("myarchive.7z", filename)
    }

    @Test
    fun `getFileNameFromUri should handle complex path`() {
        // Given: A mock URI with complex path
        val mockUri: Uri = mock()
        whenever(mockUri.lastPathSegment).thenReturn("primary:Documents/Work/backup.zip")

        // When: Extracting filename
        val filename = fileSelectionModule.getFileNameFromUri(mockUri)

        // Then: Should return just the filename
        assertEquals("backup.zip", filename)
    }
}
