package id.flwi.zipgalleryviewer.service

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.util.Log
import id.flwi.zipgalleryviewer.data.exception.InsufficientStorageException
import id.flwi.zipgalleryviewer.data.exception.PasswordRequiredException
import id.flwi.zipgalleryviewer.data.exception.ZipCorruptionException
import id.flwi.zipgalleryviewer.data.model.ExtractionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.sf.sevenzipjbinding.ExtractAskMode
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IArchiveExtractCallback
import net.sf.sevenzipjbinding.IArchiveOpenCallback
import net.sf.sevenzipjbinding.ICryptoGetTextPassword
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject
import com.hzy.libp7zip.P7ZipApi

/**
 * Service responsible for extracting zip archives using 7-Zip-JBinding-4Android.
 * Handles progress tracking, error detection, and maintains directory structure.
 */
class ZipExtractionService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ZipExtractionService"
        private const val MIN_FREE_SPACE_BYTES = 10 * 1024 * 1024 // 10 MB minimum
        private const val EXTRACTED_DIR_NAME = "extracted"
    }

    /**
     * Extracts a zip archive to the app's private storage.
     * Emits progress updates via Flow.
     *
     * @param zipUri URI of the zip file to extract
     * @param password Optional password for encrypted archives
     * @return Flow of ExtractionState updates
     */
    fun extract(zipUri: Uri, password: String? = null): Flow<ExtractionState> = flow {
        var randomAccessFile: RandomAccessFile? = null
        var inStream: RandomAccessFileInStream? = null
        var inArchive: IInArchive? = null
        var tempZipFile: File? = null
        var outputDir: File? = null

        try {
            Log.d(TAG, "Starting extraction for URI: $zipUri")

            val baseDir = context.getExternalFilesDir(null)
                ?: throw IllegalStateException("External files directory not available")

            outputDir = File(baseDir, EXTRACTED_DIR_NAME).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            // Check available storage space
            checkStorageSpace(outputDir)

            // Copy zip file to temporary location
            emit(ExtractionState.Loading(progress = 0, currentFile = "Preparing..."))
            tempZipFile = copyUriToTempFile(zipUri)

            try {
                // Extract using 7-Zip library
                emit(ExtractionState.Loading(progress = 10, currentFile = "Opening archive..."))

                randomAccessFile = RandomAccessFile(tempZipFile, "r")
                inStream = RandomAccessFileInStream(randomAccessFile)

                // If it's a 7z file and no password provided, request it BEFORE attempting to open
                // because 7z encrypts its header, so we can't even check encryption status without it
                if (getFileExtension(zipUri).equals("7z", ignoreCase = true) && password == null) {
                    Log.d(TAG, "7z file detected, requesting password upfront")
                    emit(ExtractionState.PasswordRequired)
                    return@flow
                }

                val openCallback = ArchiveOpenCallback(password)
                inArchive = SevenZip.openInArchive(null, inStream, openCallback)

                // Check if archive is encrypted and no password provided
                val isEncrypted = isArchiveEncrypted(inArchive)
                if (isEncrypted && password == null) {
                    Log.d(TAG, "Archive is password-protected, requesting password")
                    emit(ExtractionState.PasswordRequired)
                    return@flow
                }

                val itemCount = inArchive.numberOfItems
                Log.d(TAG, "Archive contains $itemCount items")

                // For archives with encrypted headers, item count might be reported as -1 or 0
                // We can still extract by iterating through all available indices
                val effectiveItemCount = if (itemCount <= 0) {
                    // Try to get a reasonable upper bound, or just iterate and stop when errors occur
                    Log.w(TAG, "Item count unavailable (encrypted header), iterating blindly")
                    Int.MAX_VALUE // Will break when getProperty throws for out-of-bounds
                } else {
                    itemCount
                }

                emit(ExtractionState.Loading(progress = 20, currentFile = "Extracting files..."))

                val extractCallback = ArchiveExtractCallback(
                    inArchive,
                    outputDir,
                    effectiveItemCount,
                    password,
                    onProgress = { current, total, fileName ->
                        val progress = 20 + ((current.toFloat() / total.coerceAtLeast(1)) * 70).toInt()
                        if (current % 5 == 0 || current == total) {
                            // progress tracking
                        }
                    }
                )

                inArchive.extract(null, false, extractCallback)                // Check if extraction was successful
                if (extractCallback.hasError) {
                    throw extractCallback.error ?: ZipCorruptionException("Extraction failed")
                }

                // Count extracted files
                val fileCount = countFiles(outputDir)
                Log.i(TAG, "Successfully extracted $fileCount files to ${outputDir.absolutePath}")

                emit(ExtractionState.Success(
                    extractedPath = outputDir.absolutePath,
                    fileCount = fileCount
                ))
            } finally {
                // Close resources
                inArchive?.close()
                inStream?.close()
                randomAccessFile?.close()
                // Clean up temp file
                tempZipFile.delete()
            }
        } catch (e: ZipCorruptionException) {
            Log.e(TAG, "Zip corruption detected", e)
            emit(ExtractionState.Error(e, "The selected file is corrupted or invalid."))
        } catch (e: InsufficientStorageException) {
            Log.e(TAG, "Insufficient storage", e)
            emit(ExtractionState.Error(e, "Not enough storage space to extract this archive."))
        } catch (e: PasswordRequiredException) {
            Log.e(TAG, "Password required", e)
            emit(ExtractionState.Error(e, "This archive is password protected."))
        } catch (e: SevenZipException) {
            Log.e(TAG, "7-Zip error during extraction", e)
            Log.d(TAG, "SevenZipException caught. Message: ${e.message}")
            Log.d(TAG, "File extension: ${getFileExtension(zipUri)}")
            Log.d(TAG, "Contains 'can't be opened': ${e.message?.contains("can't be opened with any of the registered codecs")}")
            Log.d(TAG, "Is 7z file: ${getFileExtension(zipUri).equals("7z", ignoreCase = true)}")
            Log.e(TAG, "7-Zip error during extraction", e)

            // Check if this is the 7z encrypted filename issue
            if (e.message?.contains("can't be opened with any of the registered codecs") == true &&
                getFileExtension(zipUri).equals("7z", ignoreCase = true)) {

                Log.d(TAG, "7z encrypted header detected, falling back to p7zip")
                emit(ExtractionState.Loading(progress = 15, currentFile = "Using alternative extractor..."))

                try {
                    // Close streams before fallback
                    try {
                        inStream?.close()
                        randomAccessFile?.close()
                    } catch (closeError: Exception) {
                        Log.e(TAG, "Error closing streams before fallback", closeError)
                    }

                    // Safe access with null checks
                    val archivePath = tempZipFile?.absolutePath
                    val outPath = outputDir?.absolutePath

                    if (archivePath != null && outPath != null) {
                        val success = extractViaP7zip(
                            archivePath = archivePath,
                            outputDir = outPath,
                            password = password
                        )

                        if (success) {
                            val fileCount = countFiles(outputDir!!)
                            Log.i(TAG, "p7zip successfully extracted $fileCount files")
                            emit(ExtractionState.Success(
                                extractedPath = outPath,
                                fileCount = fileCount
                            ))
                        } else {
                            emit(ExtractionState.Error(e, "Alternative extraction also failed."))
                        }
                    } else {
                        emit(ExtractionState.Error(e, "Required paths are not available for fallback extraction."))
                    }
                } catch (p7zipError: Exception) {
                    Log.e(TAG, "p7zip fallback also failed", p7zipError)
                    emit(ExtractionState.Error(p7zipError, "Extraction failed with both methods."))
                }
                return@flow
            }

            // Check for specific error types
            val errorMessage = when {
                e.message?.contains("password", ignoreCase = true) == true -> {
                    emit(ExtractionState.Error(PasswordRequiredException(e.message ?: ""), "This archive is password protected."))
                    return@flow
                }
                e.message?.contains("corrupt", ignoreCase = true) == true ||
                e.message?.contains("invalid", ignoreCase = true) == true -> {
                    "The selected file is corrupted or invalid."
                }
                else -> "An error occurred while extracting the archive."
            }
            emit(ExtractionState.Error(e, errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during extraction", e)
            emit(ExtractionState.Error(e, "An error occurred while extracting the archive."))
        } finally {
            // Ensure cleanup even on error
            try {
                inArchive?.close()
                inStream?.close()
                randomAccessFile?.close()
                tempZipFile?.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Checks if an archive is encrypted by testing the first item.
     */
    private fun isArchiveEncrypted(inArchive: IInArchive): Boolean {
        return try {
            val itemCount = inArchive.numberOfItems
            if (itemCount == 0) return false

            // Check if any item is encrypted
            for (i in 0 until itemCount) {
                val encrypted = inArchive.getProperty(i, PropID.ENCRYPTED) as? Boolean
                if (encrypted == true) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking encryption status", e)
            false
        }
    }

    /**
     * Archive open callback for 7-Zip with password support.
     */
    private inner class ArchiveOpenCallback(
        private val password: String? = null
    ) : IArchiveOpenCallback, ICryptoGetTextPassword {

        var openSucceeded = false

        override fun setTotal(files: Long?, bytes: Long?) {
            Log.d(TAG, "Archive open, total: $files files, $bytes bytes")
        }

        override fun setCompleted(files: Long?, bytes: Long?) {
            openSucceeded = true
            Log.d(TAG, "Archive open completed: $files files, $bytes bytes")
        }

        override fun cryptoGetTextPassword(): String {
            return password ?: ""
        }
    }

    /**
     * Archive extract callback for 7-Zip with progress tracking and password support.
     */
    private inner class ArchiveExtractCallback(
        private val inArchive: IInArchive,
        private val outputDir: File,
        private val totalItems: Int,
        private val password: String?,
        private val onProgress: (current: Int, total: Int, fileName: String?) -> Unit
    ) : IArchiveExtractCallback, ICryptoGetTextPassword {

        var hasError = false
        var error: Exception? = null
        private var currentIndex = 0
        private var extractedCount = 0
        override fun getStream(index: Int, extractAskMode: ExtractAskMode?): ISequentialOutStream? {
            currentIndex = index

            if (extractAskMode != ExtractAskMode.EXTRACT) {
                return null
            }

            return try {
                val path = inArchive.getStringProperty(index, PropID.PATH) ?: "file_$index"
                val isFolder = inArchive.getSimpleInterface().getArchiveItem(index).isFolder

                if (isFolder) {
                    // Create directory
                    File(outputDir, path).mkdirs()
                    null
                } else {
                    // Create file output stream
                    val outFile = File(outputDir, path)
                    outFile.parentFile?.mkdirs()

                    Log.d(TAG, "Extracting: $path")
                    FileSequentialOutStream(outFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating output stream for index $index", e)
                hasError = true
                error = ZipCorruptionException("Failed to create output stream: ${e.message}")
                null
            }
        }

        override fun prepareOperation(extractAskMode: ExtractAskMode?) {
            // No action needed
        }

        override fun setOperationResult(extractOperationResult: ExtractOperationResult?) {
            when (extractOperationResult) {
                ExtractOperationResult.OK -> {
                    extractedCount++
                    val fileName = try {
                        inArchive.getStringProperty(currentIndex, PropID.PATH)
                    } catch (e: Exception) {
                        null
                    }
                    onProgress(extractedCount, totalItems, fileName)
                }
                ExtractOperationResult.WRONG_PASSWORD -> {
                    hasError = true
                    error = PasswordRequiredException("Archive requires a password")
                }
                ExtractOperationResult.CRCERROR,
                ExtractOperationResult.DATAERROR -> {
                    hasError = true
                    error = ZipCorruptionException("Archive is corrupted (CRC/Data error)")
                }
                else -> {
                    hasError = true
                    error = ZipCorruptionException("Extraction failed: $extractOperationResult")
                }
            }
        }

        override fun setTotal(total: Long) {
            Log.d(TAG, "Total bytes to extract: $total")
        }

        override fun setCompleted(complete: Long) {
            // Progress in bytes - could be used for more granular progress
        }

        override fun cryptoGetTextPassword(): String {
            return password ?: ""
        }
    }

    /**
     * Sequential output stream for writing extracted file data.
     */
    private class FileSequentialOutStream(private val file: File) : ISequentialOutStream {
        private val outputStream = FileOutputStream(file)

        override fun write(data: ByteArray?): Int {
            if (data == null || data.isEmpty()) {
                return 0
            }
            outputStream.write(data)
            return data.size
        }

        fun close() {
            outputStream.close()
        }
    }

    /**
     * Checks if there is sufficient storage space for extraction.
     */
    private fun checkStorageSpace(directory: File) {
        val stat = StatFs(directory.absolutePath)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong

        if (availableBytes < MIN_FREE_SPACE_BYTES) {
            throw InsufficientStorageException(
                "Only ${availableBytes / 1024 / 1024} MB available, need at least ${MIN_FREE_SPACE_BYTES / 1024 / 1024} MB"
            )
        }
    }

    /**
     * Copies a content URI to a temporary file for extraction.
     */
    private fun copyUriToTempFile(uri: Uri): File {
        val tempFile = File.createTempFile("archive_", ".zip", context.cacheDir)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Unable to open input stream for URI: $uri")

        return tempFile
    }

    /**
     * Recursively counts all files in a directory.
     */
    private fun countFiles(directory: File): Int {
        var count = 0
        directory.walkTopDown().forEach { file ->
            if (file.isFile) {
                count++
            }
        }
        return count
    }

    /**
     * Extracts the file extension from a Uri.
     */
    private fun getFileExtension(uri: Uri): String {
        var fileName = ""

        // Try to get the display name from the content resolver
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying file name", e)
        }

        // Fallback to parsing the URI path if the resolver didn't work
        if (fileName.isEmpty()) {
            fileName = uri.lastPathSegment?.substringAfterLast('/') ?: ""
        }

        return fileName.substringAfterLast('.', "")
    }

    /**
     * Fallback extraction method for 7z files, especially with encrypted headers.
     */
    private fun extractViaP7zip(archivePath: String, outputDir: String, password: String?): Boolean {
        // Construct the command, adding a password if one exists
        val command = if (password != null) {
            "7z x \"$archivePath\" -o\"$outputDir\" -p\"$password\" -y"
        } else {
            "7z x \"$archivePath\" -o\"$outputDir\" -y"
        }

        // Execute the command and check the return code
        val result = P7ZipApi.executeCommand(command)
        return result == 0
    }
}
