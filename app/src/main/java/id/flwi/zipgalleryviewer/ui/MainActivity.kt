package id.flwi.zipgalleryviewer.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import id.flwi.zipgalleryviewer.manager.FileSelectionModule
import id.flwi.zipgalleryviewer.service.CleanupService
import id.flwi.zipgalleryviewer.ui.screens.load.LoadScreen
import id.flwi.zipgalleryviewer.ui.screens.load.LoadViewModel
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Main entry point activity for the Zip Gallery Viewer application.
 * Handles app initialization, cleanup on launch, and file selection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var cleanupService: CleanupService

    @Inject
    lateinit var fileSelectionModule: FileSelectionModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ZipGalleryViewerTheme {
                var cleanupComplete by remember { mutableStateOf(false) }
                val loadViewModel: LoadViewModel = hiltViewModel()
                val shouldLaunchFilePicker by loadViewModel.shouldLaunchFilePicker.collectAsState()
                val selectedFileUri by loadViewModel.selectedFileUri.collectAsState()
                val context = LocalContext.current

                // Register file picker launcher
                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        result.data?.data?.let { uri ->
                            // Take persistent permission
                            try {
                                contentResolver.takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: SecurityException) {
                                // Permission not available, continue anyway
                            }
                            loadViewModel.onFileSelected(uri)
                        }
                    }
                }

                // Launch file picker when triggered
                LaunchedEffect(shouldLaunchFilePicker) {
                    if (shouldLaunchFilePicker) {
                        val intent = fileSelectionModule.createArchivePickerIntent()
                        filePickerLauncher.launch(intent)
                        loadViewModel.onFilePickerLaunched()
                    }
                }

                // Show toast when file is selected (removed - handled by extraction)
                // Toast notification removed to avoid confusion during extraction

                // Perform cleanup on app launch
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        cleanupService.clearAllExtractedContent()
                    }
                    cleanupComplete = true
                }

                // Observe UI state
                val uiState by loadViewModel.uiState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (cleanupComplete) {
                        LoadScreen(
                            uiState = uiState,
                            onLoadClicked = { loadViewModel.onLoadClicked() },
                            onDismissError = { loadViewModel.dismissError() }
                        )
                    }
                }
            }
        }
    }
}
