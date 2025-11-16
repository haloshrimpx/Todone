package com.sijayyx.todone.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.NONE_STRING

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoListDialog(
    dialogTitle: String,
    initialTextValue: String,
    initialColor: String,
    initialIcon: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, color: String, icon: String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable () -> Unit = {},
) {
    var inputFieldValue by rememberSaveable { mutableStateOf(initialTextValue) }
    var currentSelectColor by rememberSaveable { mutableStateOf(initialColor) }
    var currentSelectIcon by rememberSaveable { mutableStateOf(initialIcon) }

    AlertDialog(
        modifier = Modifier.semantics {
            contentDescription = "Todo List Dialog"
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            IconButton(
                modifier = Modifier.semantics {
                    contentDescription = "Add List Confirm Button"
                },
                onClick = {
                    onConfirm(inputFieldValue, currentSelectColor, currentSelectIcon)
                    onDismiss()
                },
                enabled = inputFieldValue.isNotEmpty()
            ) { Icon(Icons.Filled.Check, contentDescription = null) }
        },
        dismissButton = {
            IconButton(
                modifier = Modifier.semantics {
                    contentDescription = "Add List Cancel Button"
                },
                onClick = {
                    onDismiss()
                }
            ) { Icon(Icons.Filled.Clear, contentDescription = null) }
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Today,
                contentDescription = null,
            )
        },
        title = {
            Text(
                text = dialogTitle,
                fontSize = 28.sp,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.semantics {
                        contentDescription = "Add List Input Field"
                    },
                    trailingIcon = trailingIcon,
                    value = inputFieldValue,
                    onValueChange = { inputFieldValue = it },
                    label = { Text(stringResource(R.string.edit_list_dialog_label_list)) },
                )

                Spacer(Modifier.height(16.dp))

                IconSelectorLazyRow(
                    currentSelectOption = currentSelectIcon,
                    onSelect = { currentSelectIcon = it }
                )

                Spacer(Modifier.height(4.dp))


                //选颜色
//                ColorSelectorRow(
//                    currentSelectedColor = currentSelectColor,
//                    onSelectChange = { currentSelectColor = it },
//                    options = ColorOption.options
//                )
            }

        }
    )
//    Dialog(onDismissRequest = onDismiss) {
//        Card(
//            modifier = Modifier.Companion
//                .fillMaxWidth()
//                .height(320.dp)
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Spacer(Modifier.Companion.height(16.dp))
//
//            Column(
//                modifier = Modifier.Companion
//                    .fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.Companion.CenterHorizontally,
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.Today,
//                    contentDescription = null,
//                )
//
//                Text(
//                    text = dialogTitle,
//                    fontSize = 28.sp,
//                    modifier = Modifier.Companion.padding(16.dp)
//                )
//
//                Spacer(Modifier.Companion.padding(16.dp))
//                Row(
//                    modifier = Modifier.Companion
//                        .padding(4.dp)
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceAround
//                ) {
//                    TextButton(onClick = onDismiss) { Text("Cancel") }
//                    TextButton(
//                        enabled = !inputFieldValue.isEmpty(),
//                        onClick = {
//                            onConfirm(inputFieldValue)
//                            onDismiss()
//                        }
//                    ) { Text("Confirm") }
//                }
//            }
//        }
//
//    }
}

@Preview
@Composable
fun ListAddDialogPreview() {
    TodoListTheme {
        EditTodoListDialog(
            dialogTitle = "Title",
            initialTextValue = "",
            onDismiss = {},
            onConfirm = { p1, p2, p3 -> },
            initialColor = NONE_STRING,
            initialIcon = NONE_STRING,
        )
    }
}