package com.sijayyx.todone.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun PermissionRequestDialog(
    permission: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onAllow: (() -> Unit)? = null,
    onDeny: (() -> Unit)? = null
) {
    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        onDismissRequest = { onDismiss() },
        text = {
            Text(
                stringResource(R.string.permission_request_text_line1) +
                        stringResource(R.string.permission_request_text_line2) +
                        stringResource(R.string.permission_request_text_line3) +
                        stringResource(R.string.permission_request_text_line4)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onAllow?.invoke()
            }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                onDeny?.invoke()
            }
            ) {
                Text(stringResource(R.string.deny))
            }
        },
        title = { Text(stringResource(R.string.dialog_title_permission_request)) },
        icon = { Icon(Icons.Filled.Info, contentDescription = null) }
    )
}

@Preview(locale = "zh-rCN")
@Composable
private fun PermissionRequestDialogPreview() {
    TodoListTheme {
        PermissionRequestDialog(
            permission = "",
            onDismiss = {}
        )
    }
}

