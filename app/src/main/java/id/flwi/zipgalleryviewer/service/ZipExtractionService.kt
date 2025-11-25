package id.flwi.zipgalleryviewer.service

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hzy.lib7z.Z7Extractor
import id.flwi.zipgalleryviewer.data.exception.InsufficientStorageException
import id.flwi.zipgalleryviewer.data.exception.PasswordRequiredException
import id.flwi.zipgalleryviewer.data.exception.ZipCorruptionException
import id.flwi.zipgalleryviewer.data.model.ExtractionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

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
    }

    /**
     * Extracts a zip archive to the app's private storage.
     * Emits progress updates via Flow.
     *
     * @param zipUri URI of the zip file to extract
     * @param password Optional password for encrypted archives (not used in this story)
     * @return Flow of ExtractionState updates
     */
    fun extract(zipUri: Uri, password: String? = null): Flow<ExtractionState> = flow {
        try {
            Log.d(TAG, "Starting extraction for URI: $zipUri")
            
            val outputDir = context.getExternalFilesDir(null)
                ?: throw IllegalStateException("External files directory not available")

            // Check available storage space
            checkStorageSpace(outputDir)

            // Copy zip file to temporary location
            emit(ExtractionState.Loading(progress = 0, currentFile = "Preparing..."))
            val tempZipFile = copyUriToTempFile(zipUri)

            try {
                // Extract using 7-Zip library
                emit(ExtractionState.Loading(progress = 10, currentFile = "Extracting archive..."))
                
                val extractor = Z7Extractor.getInstance()
                val extractResult = extractor.extractFile(
                    tempZipFile.absolutePath,
                    outputDir.absolutePath,
                    object : Z7Extractor.ExtractCallback {
                        override fun onStart() {
                            Log.d(TAG, "Extraction started")
                        }

                        override fun onGetFileNum(fileNum: Int) {
                            Log.d(TAG, "Archive contains $fileNum files")
                        }

                        override fun onProgress(name: String?, size: Long) {
                            Log.d(TAG, "Extracting: $name")
                        }

                        override fun onError(errorCode: Int, message: String?) {
                            Log.e(TAG, "Extraction error: $errorCode - $message")
                        }

                        override fun onSucceed() {
                            Log.d(TAG, "Extraction completed successfully")
                        }
                    }
                )

                // Check extraction result
                when (extractResult) {
                    Z7Extractor.EXTRACT_SUCCESS -> {
                        // Count extracted files
                        val fileCount = countFiles(outputDir)
                        Log.i(TAG, "Successfully extracted $fileCount files to ${outputDir.absolutePath}")
                        
                        emit(ExtractionState.Success(
                            extractedPath = outputDir.absolutePath,
                            fileCount = fileCount
                        ))
                    }
                    Z7Extractor.EXTRACT_ERROR_FILE_INVALID -> {
                        throw ZipCorruptionException("Archive file is invalid or corrupted")
                    }
                    Z7Extractor.EXTRACT_ERROR_WRONG_PASSWORD -> {
                        throw PasswordRequiredException("Archive requires a password")
                    }
                    else -> {
                        throw ZipCorruptionException("Failed to extract archive (error code: $extractResult)")
                    }
                }
            } finally {
                // Clean up temp file
                tempZipFile.delete()
            }
        } catch (e: ZipCorruptionException) {
            Log.e(TAG, "Zip corruption detected", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            emit(ExtractionState.Error(e, "The selected file is corrupted or invalid."))
        } catch (e: InsufficientStorageException) {
            Log.e(TAG, "Insufficient storage", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            emit(ExtractionState.Error(e, "Not enough storage space to extract this archive."))
        } catch (e: PasswordRequiredException) {
            Log.e(TAG, "Password required", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            emit(ExtractionState.Error(e, "This archive is password protected."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during extraction", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            emit(ExtractionState.Error(e, "An error occurred while extracting the archive."))
        }
    }.flowOn(Dispatchers.IO)

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
}
