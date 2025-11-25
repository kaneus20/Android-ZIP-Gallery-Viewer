package id.flwi.zipgalleryviewer.ui.screens.load

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the Load screen.
 * Manages the state for file selection and triggers file picker launch.
 */
@HiltViewModel
class LoadViewModel @Inject constructor() : ViewModel() {

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri.asStateFlow()

    private val _shouldLaunchFilePicker = MutableStateFlow(false)
    val shouldLaunchFilePicker: StateFlow<Boolean> = _shouldLaunchFilePicker.asStateFlow()

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
     * @param uri The URI of the selected file
     */
    fun onFileSelected(uri: Uri) {
        _selectedFileUri.value = uri
    }

    /**
     * Clears the selected file URI.
     */
    fun clearSelectedFile() {
        _selectedFileUri.value = null
    }
}
