package id.flwi.zipgalleryviewer.ui.screens.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import android.app.Activity
import android.view.WindowInsets
import android.view.WindowInsetsController
import coil.compose.AsyncImage
import coil.size.Size
import coil.request.ImageRequest
import coil.imageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val window = (context as? Activity)?.window

// Enter fullscreen on launch
    LaunchedEffect(Unit) {
        window?.insetsController?.hide(WindowInsets.Type.systemBars())
        window?.insetsController?.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

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

        LaunchedEffect(pagerState.currentPage) {
            val current = pagerState.currentPage
            val prevIndex = (current - 1).coerceAtLeast(0)
            val nextIndex = (current + 1).coerceAtMost(images.lastIndex)

            // Preload previous and next
            listOf(prevIndex, nextIndex).forEach { index ->
                if (index != current) {
                    val request = ImageRequest.Builder(context)
                        .data(images[index].fileUri)
                        .size(Size.ORIGINAL)
                        .build()
                    // Coil's image loader will cache this
                    context.imageLoader.enqueue(request)
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    imageEntry = images[page],
                    resetTrigger = page,
                    onSlideUp = {
                        window?.insetsController?.show(WindowInsets.Type.systemBars())
                        scope.launch {
                            delay(3000)
                            window?.insetsController?.hide(WindowInsets.Type.systemBars())
                        }
                    }
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
    modifier: Modifier = Modifier,
    imageEntry: ImageEntry,
    resetTrigger: Int,
    onSlideUp: () -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var containerSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    // Reset zoom state when image changes
    LaunchedEffect(resetTrigger) {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var lastTapTime = 0L
                    val doubleTapThreshold = 300L

                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downTime = System.currentTimeMillis()
                        val downPosition = down.position
                        var totalVerticalDrag = 0f
                        var hasMoved = false

                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val newScale = (scale * zoom).coerceIn(1f, 10f)

                            totalVerticalDrag += pan.y
                            if (pan.x != 0f || pan.y != 0f) hasMoved = true

                            if (newScale > 1f || scale > 1f) {
                                val scaleChange = newScale / scale
                                offsetX = offsetX * scaleChange + pan.x
                                offsetY = offsetY * scaleChange + pan.y
                                scale = newScale

                                if (newScale > 1f) {
                                    val bounds = calculateBounds(scale, containerSize)
                                    offsetX = offsetX.coerceIn(-bounds.x, bounds.x)
                                    offsetY = offsetY.coerceIn(-bounds.y, bounds.y)
                                } else {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                                event.changes.forEach { it.consume() }
                            }
                        } while (event.changes.any { it.pressed })

                        if (hasMoved && scale == 1f && totalVerticalDrag < -100f) {
                            // Slide up → show bars
                            onSlideUp()
                        } else if (!hasMoved) {
                            // Tap without movement
                            if (downTime - lastTapTime < doubleTapThreshold) {
                                // Double tap
                                lastTapTime = 0L
                                if (scale > 1f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    val targetScale = 2.5f
                                    val containerCenterX = containerSize.width / 2f
                                    val containerCenterY = containerSize.height / 2f
                                    val tapFromCenterX = downPosition.x - containerCenterX
                                    val tapFromCenterY = downPosition.y - containerCenterY
                                    scale = targetScale
                                    offsetX = tapFromCenterX * (1f - targetScale)
                                    offsetY = tapFromCenterY * (1f - targetScale)
                                }
                            } else {
                                lastTapTime = downTime
                            }
                        }
                    }
                }
            },

        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageEntry.fileUri)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .build(),
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
private fun calculateBounds(scale: Float, containerSize: androidx.compose.ui.unit.IntSize): Offset {
    if (scale <= 1f) return Offset.Zero

    val imageWidth = containerSize.width.toFloat()
    val imageHeight = containerSize.height.toFloat()

    // When scaled, the visible portion is smaller than the actual image
    // The max translation before an edge reaches the border is:
    val maxX = (imageWidth * (scale - 1f)) / 2f
    val maxY = (imageHeight * (scale - 1f)) / 2f

    return Offset(maxX, maxY)
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