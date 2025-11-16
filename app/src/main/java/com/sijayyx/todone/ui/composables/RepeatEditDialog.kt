package com.sijayyx.todone.ui.composables

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import com.sijayyx.todone.utils.TAG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatEditDialog(
    onConfirmInput: (Int, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    initialRepeatNum: Int = 0,
    initialRepeatPeriod: String = RepeatPeriod.Day.content,
) {
    var isShowingMenu by rememberSaveable { mutableStateOf(false) }

    //重复周期：必须为数字，必须大于0
    var selectedOption by rememberSaveable { mutableStateOf(initialRepeatPeriod) }
    var inputPeriod by rememberSaveable { mutableStateOf(initialRepeatNum.toString()) }
    val periodOptions =
        mapOf(
            stringResource(R.string.none) to RepeatPeriod.None.content,
            stringResource(R.string.option_day) to RepeatPeriod.Day.content,
            stringResource(R.string.option_week) to RepeatPeriod.Week.content,
            stringResource(R.string.option_month) to RepeatPeriod.Month.content,
            stringResource(R.string.option_year) to RepeatPeriod.Year.content,
        )


    AlertDialog(
        title = { Text(stringResource(R.string.title_repeat)) },
        icon = { Icon(Icons.Filled.EventRepeat, contentDescription = null) },
        onDismissRequest = onDismiss,
        confirmButton = {
            IconButton(
                onClick = {
                    onConfirmInput(inputPeriod.toInt(), selectedOption)
                    onDismiss()
                },
                enabled = selectedOption != RepeatPeriod.None.content
                        && (inputPeriod.isNotEmpty() && inputPeriod.isDigitsOnly())
                        && inputPeriod.toInt() >= 1
            )
            {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = null
                )
            }
        },
        text = {
            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                TextField(
                    value = inputPeriod,
                    textStyle = TextStyle(fontSize = 18.sp),
                    onValueChange = { if (it.isDigitsOnly() || it.isEmpty()) inputPeriod = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                    placeholder = {
                        Text(
                            stringResource(R.string.repeat_period),
                            fontSize = 12.sp,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.Companion.weight(0.5f)
                )

                ExposedDropdownMenuBox(
                    expanded = isShowingMenu,
                    onExpandedChange = { isShowingMenu = it },
                    modifier = Modifier.Companion
                        .padding(horizontal = 8.dp)
                        .weight(0.5f)
                ) {
                    TextField(
                        modifier = Modifier.Companion.menuAnchor(
                            MenuAnchorType.Companion.PrimaryEditable,
                            true
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isShowingMenu) },
                        value = periodOptions.filter {
                            Log.e(TAG, "filtered ${it.value}, selectedOption = $selectedOption")
                            it.value == selectedOption
                        }.keys.firstOrNull() ?: stringResource(R.string.none),
                        onValueChange = { },
                        readOnly = true // 设置为只读，只能通过下拉菜单选择
                    )

                    ExposedDropdownMenu(
                        expanded = isShowingMenu,
                        onDismissRequest = { isShowingMenu = false }
                    ) {
                        periodOptions.forEach { periodOption ->
                            DropdownMenuItem(
                                text = { Text(periodOption.key) },
                                onClick = {
                                    selectedOption = periodOption.value
                                    isShowingMenu = false
                                }
                            )
                        }

                    }
                }
            }
        }
    )
}