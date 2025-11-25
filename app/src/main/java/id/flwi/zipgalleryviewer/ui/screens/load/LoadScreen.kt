package id.flwi.zipgalleryviewer.ui.screens.load

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.flwi.zipgalleryviewer.R
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme

/**
 * Initial screen showing a large "Load" icon in the center.
 * Displayed after the cleanup process completes.
 */
@Composable
fun LoadScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.FolderOpen,
            contentDescription = stringResource(R.string.load_content),
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadScreenPreview() {
    ZipGalleryViewerTheme {
        LoadScreen()
    }
}
