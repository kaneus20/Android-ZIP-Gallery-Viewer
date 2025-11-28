package id.flwi.zipgalleryviewer.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
import id.flwi.zipgalleryviewer.manager.NotificationManager
import id.flwi.zipgalleryviewer.service.CleanupService
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.ui.components.ExitConfirmationDialog
import id.flwi.zipgalleryviewer.ui.components.ShareConfirmationDialog
import id.flwi.zipgalleryviewer.ui.screens.gallery.GalleryScreen
import id.flwi.zipgalleryviewer.ui.screens.gallery.GalleryViewModel
import id.flwi.zipgalleryviewer.ui.screens.gallery.GalleryUiState
import id.flwi.zipgalleryviewer.ui.screens.load.LoadScreen
import id.flwi.zipgalleryviewer.ui.screens.load.LoadUiState
import id.flwi.zipgalleryviewer.ui.screens.load.LoadViewModel
import id.flwi.zipgalleryviewer.ui.screens.viewer.ImageViewerScreen
import id.flwi.zipgalleryviewer.ui.screens.viewer.ImageViewerViewModel
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

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply FLAG_SECURE to prevent content from appearing in recent apps/task switcher
        // and to block screenshots for privacy protection
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            ZipGalleryViewerTheme {
                var cleanupComplete by remember { mutableStateOf(false) }
                var showGallery by remember { mutableStateOf(false) }
                var showImageViewer by remember { mutableStateOf(false) }
                var selectedImagePath by remember { mutableStateOf<String?>(null) }

                val loadViewModel: LoadViewModel = hiltViewModel()
                val galleryViewModel: GalleryViewModel = hiltViewModel()
                val imageViewerViewModel: ImageViewerViewModel = hiltViewModel()

                val shouldLaunchFilePicker by loadViewModel.shouldLaunchFilePicker.collectAsState()
                val selectedFileUri by loadViewModel.selectedFileUri.collectAsState()
                val loadUiState by loadViewModel.uiState.collectAsState()
                val showExitDialog by galleryViewModel.showExitDialog.collectAsState()
                val showShareDialog by galleryViewModel.showShareDialog.collectAsState()
                val context = LocalContext.current

                // Handle exit intent from notification
                LaunchedEffect(intent) {
                    if (intent?.action == NotificationManager.ACTION_EXIT_APP) {
                        galleryViewModel.onExitRequest()
                    }
                }

                // Listen for finish activity event
            LaunchedEffect(Unit) {
                galleryViewModel.finishActivityEvent.collect {
                    finish()
                }
            }

            // Observe share image event
            LaunchedEffect(Unit) {
                galleryViewModel.shareImageEvent.collect { shareIntent ->
                    android.util.Log.d("MainActivity", "Share event received, launching chooser")
                    startActivity(Intent.createChooser(shareIntent, "Share image"))
                }
            }                // Register file picker launcher
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
                        // Show persistent notification after gallery is displayed
                        notificationManager.showPersistentExitNotification()
                    }
                }

                // Perform cleanup on app launch
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        cleanupService.clearAllExtractedContent()
                    }
                    // Hide notification on cleanup
                    notificationManager.hidePersistentExitNotification()
                    cleanupComplete = true
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (cleanupComplete) {
                        if (showImageViewer) {
                            ImageViewerScreen(
                                viewModel = imageViewerViewModel,
                                onBackPressed = {
                                    showImageViewer = false
                                    selectedImagePath = null
                                }
                            )
                        } else if (showGallery) {
                            val galleryUiState by galleryViewModel.uiState.collectAsState()
                            val isAtRoot by galleryViewModel.isAtRoot.collectAsState()
                            val isGridView by galleryViewModel.isGridView.collectAsState()
                            val isRandomized by galleryViewModel.isRandomized.collectAsState()

                            // Handle image viewer navigation
                            LaunchedEffect(selectedImagePath, galleryUiState) {
                                if (selectedImagePath != null && galleryUiState is GalleryUiState.Success) {
                                    val state = galleryUiState as GalleryUiState.Success
                                    val images = state.entries.filterIsInstance<ImageEntry>()
                                    val selectedIndex = images.indexOfFirst { it.path == selectedImagePath }
                                    if (selectedIndex >= 0) {
                                        imageViewerViewModel.initialize(images, selectedIndex)
                                        showImageViewer = true
                                    }
                                }
                            }

                            GalleryScreen(
                                uiState = galleryUiState,
                                isAtRoot = isAtRoot,
                                isGridView = isGridView,
                                isRandomized = isRandomized,
                                onFolderClick = { path -> galleryViewModel.navigateToFolder(path) },
                                onImageClick = { path -> selectedImagePath = path },
                                onImageLongPress = { image -> galleryViewModel.onImageLongPressed(image) },
                                onUpClick = { galleryViewModel.navigateUp() },
                                onLayoutToggle = { galleryViewModel.toggleLayout() },
                                onRandomizeToggle = { galleryViewModel.toggleRandomize() },
                                onExitRequest = { galleryViewModel.onExitRequest() }
                            )

                            // Show exit confirmation dialog
                            if (showExitDialog) {
                                ExitConfirmationDialog(
                                    onConfirm = { galleryViewModel.onExitConfirm() },
                                    onDismiss = { galleryViewModel.onExitDismiss() }
                                )
                            }

                            // Show share confirmation dialog
                            if (showShareDialog) {
                                ShareConfirmationDialog(
                                    onConfirm = { galleryViewModel.onShareConfirm() },
                                    onDismiss = { galleryViewModel.onShareDismiss() }
                                )
                            }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
