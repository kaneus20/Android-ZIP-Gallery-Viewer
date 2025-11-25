package id.flwi.zipgalleryviewer.data.model

/**
 * Represents the state of a zip extraction operation.
 */
sealed class ExtractionState {
    /**
     * Extraction is in progress.
     * @param progress Progress percentage (0-100)
     * @param currentFile Name of the file currently being extracted
     */
    data class Loading(
        val progress: Int = 0,
        val currentFile: String? = null
    ) : ExtractionState()

    /**
     * Extraction completed successfully.
     * @param extractedPath Path to the extracted contents
     * @param fileCount Number of files extracted
     */
    data class Success(
        val extractedPath: String,
        val fileCount: Int
    ) : ExtractionState()

    /**
     * Extraction failed with an error.
     * @param error The exception that caused the failure
     * @param message User-friendly error message
     */
    data class Error(
        val error: Throwable,
        val message: String
    ) : ExtractionState()
}
