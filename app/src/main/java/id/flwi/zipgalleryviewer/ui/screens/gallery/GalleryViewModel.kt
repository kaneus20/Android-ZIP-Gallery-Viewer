package id.flwi.zipgalleryviewer.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.flwi.zipgalleryviewer.data.FileRepository
import id.flwi.zipgalleryviewer.data.model.ExtractedEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val _currentPath = MutableStateFlow("/")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _isAtRoot = MutableStateFlow(true)
    val isAtRoot: StateFlow<Boolean> = _isAtRoot.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

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
                    _uiState.value = GalleryUiState.Success(entries)
                }
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(
                    message = "Failed to load gallery: ${e.message}"
                )
            }
        }
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
}
