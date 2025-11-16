package com.sijayyx.todone.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun DropdownItemElement(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(icon, contentDescription = null)
        },
        text = {
            Text(text)
        },
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun DropdownItemElementPreview() {
    TodoListTheme {
        DropdownItemElement(
            text = "Test",
            icon = Icons.Filled.Today,
            onClick = {}
        )
    }
}