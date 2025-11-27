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
import id.flwi.zipgalleryviewer.ui.screens.gallery.GalleryScreen
import id.flwi.zipgalleryviewer.ui.screens.gallery.GalleryViewModel
import id.flwi.zipgalleryviewer.ui.screens.load.LoadScreen
import id.flwi.zipgalleryviewer.ui.screens.load.LoadUiState
import id.flwi.zipgalleryviewer.ui.screens.load.LoadViewModel
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Main entry point activity for the Zip Gallery Viewer application.
 * Handles app initialization, cleanup on launch, and navigation between screens.
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
                var showGallery by remember { mutableStateOf(false) }

                val loadViewModel: LoadViewModel = hiltViewModel()
                val galleryViewModel: GalleryViewModel = hiltViewModel()

                val shouldLaunchFilePicker by loadViewModel.shouldLaunchFilePicker.collectAsState()
                val selectedFileUri by loadViewModel.selectedFileUri.collectAsState()
                val loadUiState by loadViewModel.uiState.collectAsState()
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

                // Navigate to gallery on successful extraction
                LaunchedEffect(loadUiState) {
                    if (loadUiState is LoadUiState.Success) {
                        showGallery = true
                        galleryViewModel.refresh()
                    }
                }

                // Perform cleanup on app launch
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        cleanupService.clearAllExtractedContent()
                    }
                    cleanupComplete = true
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (cleanupComplete) {
                        if (showGallery) {
                            val galleryUiState by galleryViewModel.uiState.collectAsState()
                            val isAtRoot by galleryViewModel.isAtRoot.collectAsState()
                            GalleryScreen(
                                uiState = galleryUiState,
                                isAtRoot = isAtRoot,
                                onFolderClick = { path -> galleryViewModel.navigateToFolder(path) },
                                onImageClick = { path -> /* TODO: Navigate to image viewer */ },
                                onUpClick = { galleryViewModel.navigateUp() }
                            )
                        } else {
                            LoadScreen(
                                uiState = loadUiState,
                                onLoadClicked = { loadViewModel.onLoadClicked() },
                                onDismissError = { loadViewModel.dismissError() },
                                onPasswordSubmit = { password -> loadViewModel.onPasswordSubmit(password) },
                                onPasswordCancel = { loadViewModel.onPasswordCancel() }
                            )
                        }
                    }
                }
            }
        }
    }
}
