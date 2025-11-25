package id.flwi.zipgalleryviewer.service

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import javax.inject.Inject

/**
 * Service responsible for cleaning up extracted content from app-specific storage.
 * This ensures data ephemerality by removing all temporary files.
 */
class CleanupService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "CleanupService"
    }

    /**
     * Recursively deletes all content from the app's external files directory.
     * This method is called on app launch to ensure no data persists from previous sessions.
     *
     * @return true if cleanup was successful or directory was already empty, false if errors occurred
     */
    fun clearAllExtractedContent(): Boolean {
        return try {
            val extractedDir = context.getExternalFilesDir(null)
            
            if (extractedDir == null) {
                Log.w(TAG, "External files directory is null")
                return true // Nothing to clean
            }

            if (!extractedDir.exists()) {
                Log.d(TAG, "External files directory does not exist, nothing to clean")
                return true
            }

            Log.d(TAG, "Starting cleanup of: ${extractedDir.absolutePath}")
            
            val deleted = deleteRecursively(extractedDir)
            
            if (deleted) {
                Log.i(TAG, "Successfully cleaned all extracted content")
            } else {
                Log.e(TAG, "Failed to clean some extracted content")
                FirebaseCrashlytics.getInstance().recordException(
                    Exception("Cleanup incomplete for: ${extractedDir.absolutePath}")
                )
            }
            
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }

    /**
     * Recursively deletes all files and subdirectories within a directory,
     * but keeps the root directory itself.
     *
     * @param directory The directory to clean
     * @return true if all contents were successfully deleted
     */
    private fun deleteRecursively(directory: File): Boolean {
        if (!directory.exists()) {
            return true
        }

        var success = true
        
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // Recursively delete directory contents first
                if (!deleteDirectoryContents(file)) {
                    success = false
                    Log.w(TAG, "Failed to delete directory contents: ${file.absolutePath}")
                }
                // Then delete the directory itself
                if (!file.delete()) {
                    success = false
                    Log.w(TAG, "Failed to delete directory: ${file.absolutePath}")
                }
            } else {
                // Delete file
                if (!file.delete()) {
                    success = false
                    Log.w(TAG, "Failed to delete file: ${file.absolutePath}")
                }
            }
        }

        return success
    }

    /**
     * Deletes all contents of a directory and the directory itself.
     *
     * @param directory The directory to delete completely
     * @return true if deletion was successful
     */
    private fun deleteDirectoryContents(directory: File): Boolean {
        if (!directory.exists()) {
            return true
        }

        var success = true
        
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                if (!deleteDirectoryContents(file)) {
                    success = false
                }
            }
            if (!file.delete()) {
                success = false
                Log.w(TAG, "Failed to delete: ${file.absolutePath}")
            }
        }

        return success
    }
}
