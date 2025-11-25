package id.flwi.zipgalleryviewer.data.exception

/**
 * Exception thrown when a zip file is corrupted or invalid.
 */
class ZipCorruptionException(
    message: String = "The selected zip file is corrupted or invalid",
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when there is insufficient storage space for extraction.
 */
class InsufficientStorageException(
    message: String = "Insufficient storage space to extract the archive",
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when a password is required but not provided.
 */
class PasswordRequiredException(
    message: String = "This archive is password protected",
    cause: Throwable? = null
) : Exception(message, cause)
