package com.sijayyx.todone.ui.composables

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun DeleteConfirmDialog(
    deleteInfo: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.are_you_sure)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        text = { Text(deleteInfo) },
        confirmButton = {
            IconButton(onClick = {
                onDismiss()
                onConfirm()
            }) {
                Icon(Icons.Filled.Check, contentDescription = null)
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Clear, contentDescription = null)
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun DeleteConfirmDialogPreview() {
    TodoListTheme {
        DeleteConfirmDialog(
            deleteInfo = "List will be deleted forever!",
            onDismiss = {},
            onConfirm = {},
        )
    }
}