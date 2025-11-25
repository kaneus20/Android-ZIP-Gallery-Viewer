package id.flwi.zipgalleryviewer.manager

import android.content.Intent
import android.net.Uri

/**
 * Module responsible for file selection functionality.
 * Handles creating intents for file picker and processing selected files.
 */
class FileSelectionModule {

    /**
     * Creates an intent for selecting archive files (.zip and .7z).
     *
     * @return Intent configured to open document picker for archive files
     */
    fun createArchivePickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)

            // Set MIME types for zip and 7z files
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/zip",
                "application/x-zip-compressed",
                "application/x-7z-compressed"
            ))

            // Request persistent read permission
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
    }

    /**
     * Extracts the filename from a content URI.
     *
     * @param uri The content URI
     * @return The filename if available, or a fallback string
     */
    fun getFileNameFromUri(uri: Uri): String {
        return uri.lastPathSegment?.substringAfterLast('/') ?: "selected_archive"
    }
}
