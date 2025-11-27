package id.flwi.zipgalleryviewer.ui.screens.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
                ZoomableImage(
                    imageEntry = images[page],
                    resetTrigger = page // Reset zoom when page changes
                )
            }
        }
    }
}

/**
 * Zoomable image composable with pinch-to-zoom and double-tap support.
 * Allows HorizontalPager swipe gestures when not zoomed in.
 */
@Composable
private fun ZoomableImage(
    imageEntry: ImageEntry,
    resetTrigger: Int,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Reset zoom state when image changes
    LaunchedEffect(resetTrigger) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Toggle between fit-to-screen (1f) and zoomed (2.5f)
                        if (scale > 1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        // Only consume events when actually zoomed or zooming
                        if (scale > 1f || zoom != 1f) {
                            // Update scale with bounds
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            scale = newScale

                            // Allow panning when zoomed in
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            }

                            // Consume the event only when zoomed
                            if (scale > 1f) {
                                event.changes.forEach { it.consume() }
                            }
                        }
                        // If not zoomed and no zoom gesture, don't consume - let pager handle it
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageEntry.fileUri,
            contentDescription = imageEntry.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
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
