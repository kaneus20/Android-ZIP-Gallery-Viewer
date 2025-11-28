package id.flwi.zipgalleryviewer.ui.screens.gallery

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.flwi.zipgalleryviewer.data.FileRepository
import id.flwi.zipgalleryviewer.data.model.ExtractedEntry
import id.flwi.zipgalleryviewer.data.model.FolderEntry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.manager.NotificationManager
import id.flwi.zipgalleryviewer.service.CleanupService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * UI state for the Gallery screen.
 */
sealed class GalleryUiState {
    object Loading : GalleryUiState()
    data class Success(val entries: List<ExtractedEntry>) : GalleryUiState()
    data class Error(val message: String) : GalleryUiState()
}

/**
 * ViewModel for the Gallery screen.
 * Manages UI state and business logic for displaying extracted content.
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileRepository: FileRepository,
    private val cleanupService: CleanupService,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _isAtRoot = MutableStateFlow(true)
    val isAtRoot: StateFlow<Boolean> = _isAtRoot.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _isRandomized = MutableStateFlow(false)
    val isRandomized: StateFlow<Boolean> = _isRandomized.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> = _showShareDialog.asStateFlow()

    private val _selectedImageForShare = MutableStateFlow<ImageEntry?>(null)
    val selectedImageForShare: StateFlow<ImageEntry?> = _selectedImageForShare.asStateFlow()

    private val _finishActivityEvent = MutableSharedFlow<Unit>()
    val finishActivityEvent: SharedFlow<Unit> = _finishActivityEvent.asSharedFlow()

    init {
        loadEntries("/")
    }

    /**
     * Loads entries for the current path.
     */
    private fun loadEntries(path: String) {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            try {
                fileRepository.getExtractedEntries(path).collect { entries ->
                    val sortedEntries = sortEntries(entries)
                    _uiState.value = GalleryUiState.Success(sortedEntries)
                }
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(
                    message = "Failed to load gallery: ${e.message}"
                )
            }
        }
    }

    /**
     * Sorts entries with folders first (alphabetically), then images (random or alphabetical).
     */
    private fun sortEntries(entries: List<ExtractedEntry>): List<ExtractedEntry> {
        val folders = entries.filterIsInstance<FolderEntry>().sortedBy { it.name }
        val images = entries.filterIsInstance<ImageEntry>()

        val sortedImages = if (_isRandomized.value) {
            images.shuffled()
        } else {
            images.sortedBy { it.name }
        }

        return folders + sortedImages
    }

    /**
     * Navigates into a folder.
     */
    fun navigateToFolder(path: String) {
        _currentPath.value = path
        _isAtRoot.value = (path == "/")
        loadEntries(path)
    }

    /**
     * Navigates to the parent directory.
     */
    fun navigateUp() {
        val current = _currentPath.value
        if (current != "/") {
            val parent = current.substringBeforeLast("/", "/")
            val newPath = parent.ifEmpty { "/" }
            _currentPath.value = newPath
            _isAtRoot.value = (newPath == "/")
            loadEntries(newPath)
        }
    }

    /**
     * Refreshes the current directory.
     */
    fun refresh() {
        loadEntries(_currentPath.value)
    }

    /**
     * Toggles between grid and list view.
     */
    fun toggleLayout() {
        _isGridView.value = !_isGridView.value
    }

    /**
     * Toggles between randomized and alphabetical image order.
     */
    fun toggleRandomize() {
        _isRandomized.value = !_isRandomized.value
        // Reload entries with new sort order
        loadEntries(_currentPath.value)
    }

    /**
     * Initiates the exit flow by showing the confirmation dialog.
     */
    fun onExitRequest() {
        _showExitDialog.value = true
    }

    /**
     * Dismisses the exit confirmation dialog.
     */
    fun onExitDismiss() {
        _showExitDialog.value = false
    }

    /**
     * Confirms the exit action - cleans up data and closes the app.
     */
    fun onExitConfirm() {
        _showExitDialog.value = false
        viewModelScope.launch(Dispatchers.IO) {
            // Clean up all extracted content
            cleanupService.clearAllExtractedContent()

            // Hide the persistent notification
            notificationManager.hidePersistentExitNotification()

            // Signal the activity to finish
            _finishActivityEvent.emit(Unit)
        }
    }

    /**
     * Handles long press on an image - shows share confirmation dialog.
     */
    fun onImageLongPressed(image: ImageEntry) {
        _selectedImageForShare.value = image
        _showShareDialog.value = true
    }

    /**
     * Dismisses the share confirmation dialog.
     */
    fun onShareDismiss() {
        _showShareDialog.value = false
        _selectedImageForShare.value = null
    }

    /**
     * Confirms the share action - launches share intent with selected image.
     */
    fun onShareConfirm() {
        val image = _selectedImageForShare.value ?: return
        _showShareDialog.value = false

        viewModelScope.launch(Dispatchers.Main) {
            try {
                val file = File(image.path)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share image"))
            } catch (e: Exception) {
                // Handle error silently or log
            } finally {
                _selectedImageForShare.value = null
            }
        }
    }
}
