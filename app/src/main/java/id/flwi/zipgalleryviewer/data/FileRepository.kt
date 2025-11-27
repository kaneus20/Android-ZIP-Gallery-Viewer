package id.flwi.zipgalleryviewer.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import id.flwi.zipgalleryviewer.data.model.ExtractedEntry
import id.flwi.zipgalleryviewer.data.model.FolderEntry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for accessing and managing extracted files in app's private storage.
 * Provides an abstraction layer for file system operations.
 */
@Singleton
class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val EXTRACTED_DIR_NAME = "extracted"
        private val SUPPORTED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }

    /**
     * Gets the directory where extracted content is stored.
     */
    private fun getExtractedDir(): File {
        return File(context.getExternalFilesDir(null), EXTRACTED_DIR_NAME)
    }

    /**
     * Retrieves files and folders for a given relative path within extracted content.
     *
     * @param path Relative path within extracted content (use "/" for root)
     * @return Flow emitting list of ExtractedEntry objects
     */
    fun getExtractedEntries(path: String): Flow<List<ExtractedEntry>> = flow {
        val extractedDir = getExtractedDir()
        val targetDir = if (path == "/" || path.isEmpty()) {
            extractedDir
        } else {
            File(extractedDir, path.removePrefix("/"))
        }

        if (!targetDir.exists() || !targetDir.isDirectory) {
            emit(emptyList())
            return@flow
        }

        val entries = mutableListOf<ExtractedEntry>()
        val files = targetDir.listFiles()?.sortedBy { it.name.lowercase() } ?: emptyList()

        for (file in files) {
            val relativePath = file.relativeTo(extractedDir).path
            val parentPath = file.parentFile?.relativeTo(extractedDir)?.path?.takeIf { it.isNotEmpty() }

            if (file.isDirectory) {
                // Count items in folder
                val itemCount = file.listFiles()?.size ?: 0
                entries.add(
                    FolderEntry(
                        path = relativePath,
                        name = file.name,
                        parentPath = parentPath,
                        itemCount = itemCount
                    )
                )
            } else if (file.isFile && isImageFile(file)) {
                val mimeType = getMimeType(file)
                entries.add(
                    ImageEntry(
                        path = relativePath,
                        name = file.name,
                        parentPath = parentPath,
                        fileUri = file.toUri(),
                        thumbnailUri = file.toUri(), // For now, use full image as thumbnail
                        mimeType = mimeType
                    )
                )
            }
        }

        emit(entries)
    }.flowOn(Dispatchers.IO)

    /**
     * Checks if a file is an image based on extension.
     */
    private fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in SUPPORTED_IMAGE_EXTENSIONS
    }

    /**
     * Gets MIME type for an image file.
     */
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            else -> "image/*"
        }
    }

    /**
     * Clears all extracted content from private storage.
     */
    suspend fun clearExtractedContent() {
        withContext(Dispatchers.IO) {
            val extractedDir = getExtractedDir()
            if (extractedDir.exists()) {
                extractedDir.deleteRecursively()
            }
        }
    }
}
