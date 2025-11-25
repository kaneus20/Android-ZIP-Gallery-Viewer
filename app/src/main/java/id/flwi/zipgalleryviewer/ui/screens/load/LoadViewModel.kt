package id.flwi.zipgalleryviewer.ui.screens.load

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.flwi.zipgalleryviewer.data.model.ExtractionState
import id.flwi.zipgalleryviewer.service.ZipExtractionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Load screen.
 */
sealed class LoadUiState {
    object Idle : LoadUiState()
    data class Loading(val progress: Int = 0, val message: String? = null) : LoadUiState()
    data class Error(val message: String, val exception: Throwable? = null) : LoadUiState()
    data class PasswordRequired(val errorMessage: String? = null) : LoadUiState()
    object Success : LoadUiState()
}

/**
 * ViewModel for the Load screen.
 * Manages file selection, extraction state, and UI state.
 */
@HiltViewModel
class LoadViewModel @Inject constructor(
    private val zipExtractionService: ZipExtractionService
) : ViewModel() {

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri.asStateFlow()

    private val _shouldLaunchFilePicker = MutableStateFlow(false)
    val shouldLaunchFilePicker: StateFlow<Boolean> = _shouldLaunchFilePicker.asStateFlow()

    private val _uiState = MutableStateFlow<LoadUiState>(LoadUiState.Idle)
    val uiState: StateFlow<LoadUiState> = _uiState.asStateFlow()

    private var currentPassword: String? = null // Ephemeral storage for password during extraction

    /**
     * Called when the user taps the "Load" icon.
     * Triggers the file picker to launch.
     */
    fun onLoadClicked() {
        _shouldLaunchFilePicker.value = true
    }

    /**
     * Resets the file picker launch flag after it has been consumed.
     */
    fun onFilePickerLaunched() {
        _shouldLaunchFilePicker.value = false
    }

    /**
     * Called when a file is successfully selected from the file picker.
     * Automatically starts extraction.
     *
     * @param uri The URI of the selected file
     */
    fun onFileSelected(uri: Uri) {
        _selectedFileUri.value = uri
        startExtraction(uri)
    }

    /**
     * Starts the extraction process for the given URI.
     * @param password Optional password for encrypted archives
     */
    private fun startExtraction(uri: Uri, password: String? = null) {
        viewModelScope.launch {
            zipExtractionService.extract(uri, password).collect { state ->
                when (state) {
                    is ExtractionState.Loading -> {
                        _uiState.value = LoadUiState.Loading(
                            progress = state.progress,
                            message = state.currentFile
                        )
                    }
                    is ExtractionState.Success -> {
                        // Clear password from memory after successful extraction
                        currentPassword = null
                        _uiState.value = LoadUiState.Success
                    }
                    is ExtractionState.Error -> {
                        // Clear password from memory on error
                        currentPassword = null
                        _uiState.value = LoadUiState.Error(
                            message = state.message,
                            exception = state.error
                        )
                    }
                    is ExtractionState.PasswordRequired -> {
                        _uiState.value = LoadUiState.PasswordRequired()
                    }
                }
            }
        }
    }

    /**
     * Called when user submits a password for an encrypted archive.
     * Retries extraction with the provided password.
     *
     * @param password The password to use for extraction
     */
    fun onPasswordSubmit(password: String) {
        val uri = _selectedFileUri.value ?: return

        // Store password ephemerally for potential retry
        currentPassword = password

        // Retry extraction with password
        viewModelScope.launch {
            zipExtractionService.extract(uri, password).collect { state ->
                when (state) {
                    is ExtractionState.Loading -> {
                        _uiState.value = LoadUiState.Loading(
                            progress = state.progress,
                            message = state.currentFile
                        )
                    }
                    is ExtractionState.Success -> {
                        // Clear password from memory after successful extraction
                        currentPassword = null
                        _uiState.value = LoadUiState.Success
                    }
                    is ExtractionState.Error -> {
                        // Check if it's a wrong password error
                        if (state.error is id.flwi.zipgalleryviewer.data.exception.PasswordRequiredException) {
                            // Show password dialog again with error message
                            _uiState.value = LoadUiState.PasswordRequired(
                                errorMessage = "Incorrect password. Please try again."
                            )
                        } else {
                            // Clear password and show general error
                            currentPassword = null
                            _uiState.value = LoadUiState.Error(
                                message = state.message,
                                exception = state.error
                            )
                        }
                    }
                    is ExtractionState.PasswordRequired -> {
                        // Shouldn't happen when password is provided, but handle gracefully
                        _uiState.value = LoadUiState.PasswordRequired()
                    }
                }
            }
        }
    }

    /**
     * Called when user cancels the password dialog.
     * Returns to idle state and clears password.
     */
    fun onPasswordCancel() {
        currentPassword = null
        _selectedFileUri.value = null
        _uiState.value = LoadUiState.Idle
    }

    /**
     * Clears the selected file URI.
     */
    fun clearSelectedFile() {
        _selectedFileUri.value = null
    }

    /**
     * Dismisses the error state and returns to idle.
     */
    fun dismissError() {
        _uiState.value = LoadUiState.Idle
    }
}
