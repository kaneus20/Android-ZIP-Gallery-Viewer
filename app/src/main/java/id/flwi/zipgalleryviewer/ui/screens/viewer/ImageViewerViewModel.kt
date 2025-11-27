package id.flwi.zipgalleryviewer.ui.screens.viewer

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ImageViewer screen.
 * Manages current image index and toast notifications for image position.
 */
@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _images = MutableStateFlow<List<ImageEntry>>(emptyList())
    val images: StateFlow<List<ImageEntry>> = _images.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /**
     * Initializes the viewer with the list of images and the selected index.
     */
    fun initialize(imageList: List<ImageEntry>, selectedIndex: Int) {
        _images.value = imageList
        _currentIndex.value = selectedIndex.coerceIn(0, imageList.size - 1)
        showPositionToast()
    }

    /**
     * Updates the current image index.
     */
    fun updateCurrentIndex(index: Int) {
        if (index != _currentIndex.value && index in _images.value.indices) {
            _currentIndex.value = index
            showPositionToast()
        }
    }

    /**
     * Shows a toast message indicating the current image position.
     */
    private fun showPositionToast() {
        viewModelScope.launch {
            val current = _currentIndex.value + 1
            val total = _images.value.size
            Toast.makeText(
                context,
                " of ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
