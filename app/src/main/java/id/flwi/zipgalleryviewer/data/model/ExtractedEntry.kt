package id.flwi.zipgalleryviewer.data.model

/**
 * Sealed interface representing a generic entry (file or folder) within extracted zip content.
 * This allows unified handling in the gallery view.
 */
sealed interface ExtractedEntry {
    /**
     * Full relative path of the entry within the extracted structure.
     * Example: "photos/vacation/image1.jpg"
     */
    val path: String

    /**
     * Display name of the entry.
     * Example: "image1.jpg" or "vacation"
     */
    val name: String

    /**
     * Relative path to the parent directory, or null if at root.
     * Example: "photos/vacation" or null
     */
    val parentPath: String?
}
