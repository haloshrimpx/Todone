package com.sijayyx.todone.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun BasicIconTextButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .wrapContentSize()
            .clickable(onClick = onClick)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(Modifier.Companion.width(4.dp))
        Text(text = label, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DropdownIconTextButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onMenuItemSelectString: (String) -> Unit = {},
    selectedStringProcessor: (String) -> String = { it },
    menuItems: @Composable ColumnScope.(() -> Unit) -> Unit = {},
    menuItemList: Map<String, String> = mapOf(), //第一个是下拉列表框展示的文本，第二个是下拉列表框选定的值
    onDismissRequest: () -> Unit = {},
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    BasicIconTextButton(
        label = label,
        icon = icon,
        onClick = { isDropdownExpanded = !isDropdownExpanded },
        modifier = modifier
    )

    Box(
        modifier = Modifier
            .wrapContentSize()
            .focusable(false)
    )
    {
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = {
                onDismissRequest()
                isDropdownExpanded = false
            },
            modifier = Modifier.Companion.focusable(false),
        ) {
            menuItemList.forEach {
                DropdownMenuItem(
                    text = { Text(it.key) },
                    onClick = {
                        onMenuItemSelectString(selectedStringProcessor(it.value))
                        isDropdownExpanded = false
                    }
                )
            }
            menuItems { isDropdownExpanded = false }
        }
    }
}

@Composable
@Preview(showSystemUi = false, showBackground = true)
fun IconTextButtonPreview() {
    TodoListTheme {
        BasicIconTextButton(
            icon = Icons.Filled.Info,
            label = "Text",
            onClick = {}
        )
    }
}