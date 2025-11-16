package com.sijayyx.todone.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun TipDialog(
    title: String,
    content: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        icon = {
            Icon(Icons.Filled.Lightbulb, contentDescription = null)
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.ok)) }
        },
        text = { Text(content) },
    )
}

@Preview
@Composable
private fun TipDialogPreview() {
    TodoListTheme {
        TipDialog(
            title = "Tips",
            content = "This is a test tip dialog",
            onDismissRequest = {}
        )
    }
}