package id.flwi.zipgalleryviewer.ui.screens.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import id.flwi.zipgalleryviewer.data.model.ExtractedEntry
import id.flwi.zipgalleryviewer.data.model.FolderEntry
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme

/**
 * Main gallery screen displaying extracted content in a grid layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    uiState: GalleryUiState,
    isAtRoot: Boolean,
    onFolderClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler(enabled = !isAtRoot) {
        onUpClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    if (!isAtRoot) {
                        IconButton(onClick = onUpClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate up"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
        when (uiState) {
            is GalleryUiState.Loading -> {
                CircularProgressIndicator()
            }
            is GalleryUiState.Success -> {
                if (uiState.entries.isEmpty()) {
                    Text(
                        text = "No images or folders found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    GalleryGrid(
                        entries = uiState.entries,
                        onFolderClick = onFolderClick,
                        onImageClick = onImageClick
                    )
                }
            }
            is GalleryUiState.Error -> {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        }
    }
}

/**
 * Grid layout displaying folders and images.
 */
@Composable
private fun GalleryGrid(
    entries: List<ExtractedEntry>,
    onFolderClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(entries, key = { it.path }) { entry ->
            when (entry) {
                is FolderEntry -> FolderItem(
                    folder = entry,
                    onClick = { onFolderClick(entry.path) }
                )
                is ImageEntry -> ImageItem(
                    image = entry,
                    onClick = { onImageClick(entry.path) }
                )
            }
        }
    }
}

/**
 * Individual folder item in the gallery grid.
 */
@Composable
private fun FolderItem(
    folder: FolderEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder: ${folder.name}",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
        Text(
            text = folder.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

/**
 * Individual image item in the gallery grid.
 */
@Composable
private fun ImageItem(
    image: ImageEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = image.thumbnailUri ?: image.fileUri,
                contentDescription = image.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = image.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenPreview() {
    ZipGalleryViewerTheme {
        GalleryScreen(
            uiState = GalleryUiState.Success(
                entries = listOf(
                    FolderEntry(
                        path = "photos",
                        name = "photos",
                        parentPath = null,
                        itemCount = 5
                    ),
                    ImageEntry(
                        path = "image1.jpg",
                        name = "image1.jpg",
                        parentPath = null,
                        fileUri = "".toUri(),
                        mimeType = "image/jpeg"
                    )
                )
            ),
            isAtRoot = true,
            onFolderClick = {},
            onImageClick = {},
            onUpClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenWithUpButtonPreview() {
    ZipGalleryViewerTheme {
        GalleryScreen(
            uiState = GalleryUiState.Success(
                entries = listOf(
                    ImageEntry(
                        path = "photos/image1.jpg",
                        name = "image1.jpg",
                        parentPath = "photos",
                        fileUri = "".toUri(),
                        mimeType = "image/jpeg"
                    )
                )
            ),
            isAtRoot = false,
            onFolderClick = {},
            onImageClick = {},
            onUpClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenEmptyPreview() {
    ZipGalleryViewerTheme {
        GalleryScreen(
            uiState = GalleryUiState.Success(emptyList()),
            isAtRoot = true,
            onFolderClick = {},
            onImageClick = {},
            onUpClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenLoadingPreview() {
    ZipGalleryViewerTheme {
        GalleryScreen(
            uiState = GalleryUiState.Loading,
            isAtRoot = true,
            onFolderClick = {},
            onImageClick = {},
            onUpClick = {}
        )
    }
}
