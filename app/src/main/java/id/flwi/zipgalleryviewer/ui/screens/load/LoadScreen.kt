package id.flwi.zipgalleryviewer.ui.screens.load

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.flwi.zipgalleryviewer.R
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme

/**
 * Initial screen showing a large "Load" icon in the center.
 * Displays loading indicator during extraction and error dialogs on failure.
 *
 * @param uiState Current UI state
 * @param onLoadClicked Callback invoked when the Load icon is tapped
 * @param onDismissError Callback to dismiss error dialog
 * @param modifier Optional modifier for the composable
 */
@Composable
fun LoadScreen(
    uiState: LoadUiState = LoadUiState.Idle,
    onLoadClicked: () -> Unit = {},
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is LoadUiState.Idle -> {
                // Show Load icon
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = stringResource(R.string.load_content),
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(onClick = onLoadClicked),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            is LoadUiState.Loading -> {
                // Show full-screen loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.message ?: "Extracting...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
            is LoadUiState.Error -> {
                // Show error dialog
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = stringResource(R.string.load_content),
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(onClick = onLoadClicked),
                    tint = MaterialTheme.colorScheme.primary
                )

                AlertDialog(
                    onDismissRequest = onDismissError,
                    title = { Text("Extraction Error") },
                    text = { Text(uiState.message) },
                    confirmButton = {
                        TextButton(onClick = onDismissError) {
                            Text("OK")
                        }
                    }
                )
            }
            is LoadUiState.Success -> {
                // Success state - will transition to gallery in future story
                Text(
                    text = "Extraction complete!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadScreenPreview() {
    ZipGalleryViewerTheme {
        LoadScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoadScreenLoadingPreview() {
    ZipGalleryViewerTheme {
        LoadScreen(
            uiState = LoadUiState.Loading(50, "Extracting file.jpg")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadScreenErrorPreview() {
    ZipGalleryViewerTheme {
        LoadScreen(
            uiState = LoadUiState.Error("The selected file is corrupted or invalid.")
        )
    }
}
