@file:OptIn(ExperimentalMaterial3Api::class)

package com.sijayyx.todone.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun MoveGroupDialog(
    options: Map<Long, String>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var isShowingMenu by rememberSaveable { mutableStateOf(false) }
    var selectedOptionId by rememberSaveable { mutableLongStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            IconButton(onClick = {
                onConfirm(selectedOptionId)
                onDismiss()
            }) {
                Icon(Icons.Filled.Check, contentDescription = null)
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Clear, contentDescription = null)
            }
        },
        title = {
            Text(stringResource(R.string.move_to))
        },
        icon = { Icon(ExtendedIcons.MoveGroup, contentDescription = null) },
        text = {
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isShowingMenu = it }
            ) {
                TextField(
                    value = options[selectedOptionId] ?: stringResource(R.string.none),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.Companion.menuAnchor(
                        MenuAnchorType.Companion.PrimaryEditable,
                        true
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isShowingMenu) },
                )

                ExposedDropdownMenu(
                    expanded = isShowingMenu,
                    onDismissRequest = { isShowingMenu = false }
                ) {
                    options.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = {
                                Text(name)
                            },
                            onClick = {
                                selectedOptionId = id
                                isShowingMenu = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun MoveGroupDialogPreview() {
    TodoListTheme {
        MoveGroupDialog(
            onDismiss = {}, onConfirm = {},
            options = mapOf()
        )
    }
}