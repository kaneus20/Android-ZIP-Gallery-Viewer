package id.flwi.zipgalleryviewer.data.model

/**
 * Represents a directory within the extracted zip content.
 *
 * @property path Full relative path within the extracted structure
 * @property name Display name of the folder
 * @property parentPath Relative path to the parent directory, or null if at root
 * @property itemCount Number of items (files and folders) in this folder
 */
data class FolderEntry(
    override val path: String,
    override val name: String,
    override val parentPath: String?,
    val itemCount: Int = 0
) : ExtractedEntry
