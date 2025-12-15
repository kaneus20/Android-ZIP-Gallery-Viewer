package id.flwi.zipgalleryviewer.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Confirmation dialog for sharing an image.
 * Asks the user to confirm before launching the share intent.
 *
 * @param onDismiss Callback when the dialog is dismissed (Cancel button or outside click)
 * @param onConfirm Callback when the user confirms the share action (Share button)
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun ShareConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Share Image")
        },
        text = {
            Text("Do you want to share this image?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Share")
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
