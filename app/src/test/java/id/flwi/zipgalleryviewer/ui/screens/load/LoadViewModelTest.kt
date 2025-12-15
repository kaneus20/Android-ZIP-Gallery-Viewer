package id.flwi.zipgalleryviewer.ui.screens.load

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * Unit tests for LoadViewModel.
 * Tests the file selection logic and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoadViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoadViewModel

    @Before
    fun setup() {
        viewModel = LoadViewModel()
    }

    @Test
    fun `initial state should have no selected file`() {
        assertNull(viewModel.selectedFileUri.value)
    }

    @Test
    fun `initial state should not trigger file picker`() {
        assertFalse(viewModel.shouldLaunchFilePicker.value)
    }

    @Test
    fun `onLoadClicked should trigger file picker launch`() {
        // When: Load button is clicked
        viewModel.onLoadClicked()

        // Then: File picker should be triggered
        assertTrue(viewModel.shouldLaunchFilePicker.value)
    }

    @Test
    fun `onFilePickerLaunched should reset launch flag`() {
        // Given: File picker has been triggered
        viewModel.onLoadClicked()
        assertTrue(viewModel.shouldLaunchFilePicker.value)

        // When: File picker has been launched
        viewModel.onFilePickerLaunched()

        // Then: Launch flag should be reset
        assertFalse(viewModel.shouldLaunchFilePicker.value)
    }

    @Test
    fun `onFileSelected should update selected file URI`() = runTest {
        // Given: A mock URI
        val mockUri: Uri = mock()

        // When: File is selected
        viewModel.onFileSelected(mockUri)

        // Then: Selected URI should be updated
        assertEquals(mockUri, viewModel.selectedFileUri.value)
    }

    @Test
    fun `clearSelectedFile should clear the selected URI`() {
        // Given: A file has been selected
        val mockUri: Uri = mock()
        viewModel.onFileSelected(mockUri)
        assertNotNull(viewModel.selectedFileUri.value)

        // When: Clear is called
        viewModel.clearSelectedFile()

        // Then: Selected URI should be null
        assertNull(viewModel.selectedFileUri.value)
    }

    @Test
    fun `multiple load clicks should maintain trigger state`() {
        // When: Load is clicked multiple times
        viewModel.onLoadClicked()
        viewModel.onLoadClicked()
        viewModel.onLoadClicked()

        // Then: File picker should still be triggered
        assertTrue(viewModel.shouldLaunchFilePicker.value)
    }

    @Test
    fun `file selection workflow should work correctly`() {
        // When: User clicks load
        viewModel.onLoadClicked()
        assertTrue(viewModel.shouldLaunchFilePicker.value)

        // And: File picker is launched
        viewModel.onFilePickerLaunched()
        assertFalse(viewModel.shouldLaunchFilePicker.value)

        // And: User selects a file
        val mockUri: Uri = mock()
        viewModel.onFileSelected(mockUri)

        // Then: URI should be stored
        assertEquals(mockUri, viewModel.selectedFileUri.value)
    }
}
