package id.flwi.zipgalleryviewer.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.flwi.zipgalleryviewer.ui.theme.ZipGalleryViewerTheme

/**
 * Exit confirmation dialog that asks the user if they want to close the app and clear data.
 */
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Exit Application")
        },
        text = {
            Text("Are you sure you want to leave this app?")
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text("Yes, clear and exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ExitConfirmationDialogPreview() {
    ZipGalleryViewerTheme {
        ExitConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
