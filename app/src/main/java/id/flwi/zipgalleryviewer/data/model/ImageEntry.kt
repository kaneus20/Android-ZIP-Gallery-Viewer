package id.flwi.zipgalleryviewer.data.model

import android.net.Uri

/**
 * Represents an individual image file extracted from the zip archive.
 *
 * @property path Full relative path within the extracted structure
 * @property name Display name of the image file
 * @property parentPath Relative path to the parent directory, or null if at root
 * @property fileUri URI pointing to the actual image file in app's private storage
 * @property thumbnailUri Optional URI pointing to a cached thumbnail
 * @property mimeType MIME type of the image (e.g., "image/jpeg")
 * @property width Optional width dimension in pixels
 * @property height Optional height dimension in pixels
 */
data class ImageEntry(
    override val path: String,
    override val name: String,
    override val parentPath: String?,
    val fileUri: Uri,
    val thumbnailUri: Uri? = null,
    val mimeType: String,
    val width: Int? = null,
    val height: Int? = null
) : ExtractedEntry
