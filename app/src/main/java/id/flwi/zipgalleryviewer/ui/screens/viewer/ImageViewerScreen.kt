package id.flwi.zipgalleryviewer.ui.screens.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import coil.compose.AsyncImage
import id.flwi.zipgalleryviewer.data.model.ImageEntry
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme

/**
 * Full-screen image viewer with swipe navigation.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerScreen(
    viewModel: ImageViewerViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val images by viewModel.images.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    BackHandler {
        onBackPressed()
    }

    if (images.isNotEmpty()) {
        val pagerState = rememberPagerState(
            initialPage = currentIndex,
            pageCount = { images.size }
        )

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                viewModel.updateCurrentIndex(page)
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images[page].fileUri,
                    contentDescription = images[page].name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageViewerScreenPreview() {
    // Note: Preview requires mock ViewModel - shown for structure only
    ZipGalleryViewerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Preview placeholder
        }
    }
}
