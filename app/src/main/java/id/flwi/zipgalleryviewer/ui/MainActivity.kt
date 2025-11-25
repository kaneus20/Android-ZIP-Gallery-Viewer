package id.flwi.zipgalleryviewer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import id.flwi.zipgalleryviewer.service.CleanupService
import id.flwi.zipgalleryviewer.ui.screens.load.LoadScreen
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Main entry point activity for the Zip Gallery Viewer application.
 * Handles app initialization and cleanup on launch.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var cleanupService: CleanupService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ZipGalleryViewerTheme {
                var cleanupComplete by remember { mutableStateOf(false) }

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
                        LoadScreen()
                    }
                }
            }
        }
    }
}
