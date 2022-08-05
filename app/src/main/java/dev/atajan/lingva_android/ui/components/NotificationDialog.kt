package dev.atajan.lingva_android.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.atajan.lingva_android.R

@Composable
fun ErrorNotificationDialog(
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    if (shouldShowDialog) {
        AlertDialog(
            backgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty onCloseRequest.
                onDismissRequest()
            },
            title = {
                Text(
                    text = "Uh oh :(",
                    style = MaterialTheme.typography.headlineLarge
                )
            },
            text = {
                Text(context.getString(R.string.error_dialog_body))
            },
            confirmButton = {
                // this component is meant only for a passive error notification. No positive action should be here.
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(
                        text = context.getString(R.string.error_dialog_dismiss_button),
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewNotificationDialog() {
    ErrorNotificationDialog(
        shouldShowDialog = true,
        onDismissRequest = {}
    )
}