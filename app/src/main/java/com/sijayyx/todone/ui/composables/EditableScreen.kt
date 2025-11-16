package com.sijayyx.todone.ui.composables

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sijayyx.todone.R
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberEditableScreenState(
    initialEditModeState: Boolean = false,
    initialSelectedItems: Set<Long> = emptySet(),
    targetElementList: List<Long> = listOf(),
    initialMenuExpandState: Boolean = false
): EditableScreenState {
    return rememberSaveable(targetElementList, saver = EditableScreenState.Saver()) {
        EditableScreenState(
            initialEditModeState = initialEditModeState,
            initialSelectedItems = initialSelectedItems,
            targetElementList = targetElementList,
            initialMenuExpandState = initialMenuExpandState
        )
    }
}

@Stable
class EditableScreenState(
    private val targetElementList: List<Long> = listOf(),
    initialSelectedItems: Set<Long> = emptySet(),
    initialEditModeState: Boolean = false,
    initialMenuExpandState: Boolean = false
) {
    companion object {
        //自定义Saver，在配置更改时保存状态
        //保存已选择的Set和是否在编辑状态
        fun Saver(): Saver<EditableScreenState, Any> = listSaver(
            save = {
                listOf(
                    it.selectedEditItems,
                    it.isInEditMode,
                    it.isMoreActionsExpanded
                )
            },
            restore = {
                EditableScreenState(
                    initialSelectedItems = it[0] as Set<Long>,
                    initialEditModeState = it[1] as Boolean,
                    initialMenuExpandState = it[2] as Boolean,
                )
            }
        )
    }

    var selectedEditItems by mutableStateOf(initialSelectedItems)
        private set

    var isInEditMode: Boolean by mutableStateOf(initialEditModeState)
        private set

    var isMoreActionsExpanded by mutableStateOf(initialEditModeState)

    val selectionCount: Int
        get() = selectedEditItems.size

    val isAllSelected: Boolean
        get() = targetElementList.size == selectedEditItems.size

    val isSelectedItem: Boolean
        get() = selectedEditItems.isNotEmpty()

    suspend fun exitEditMode() {
        isInEditMode = false
        delay(500)
        clearSelectedItems()

        Log.e(
            TAG,
            "EditableScreenState: exit edit mode! selectedEditItems.size = ${selectedEditItems.size}"
        )
    }

    fun trySelectItem(id: Long, isSelected: Boolean) {
        selectedEditItems =
            if (!isSelected) onAddItem(id)
            else onRemoveItem(id)
    }

    private fun onAddItem(id: Long): Set<Long> {
        return selectedEditItems + id
    }

    private fun onRemoveItem(id: Long): Set<Long> {
        return selectedEditItems - id
    }

    fun enterEditMode(id: Long) {
        isInEditMode = true
        clearSelectedItems()
        selectedEditItems = onAddItem(id)
    }

    fun selectAllEditItems() {
        selectedEditItems = targetElementList.toSet()
    }

    fun cancelSelectAllItems() {
        clearSelectedItems()
    }

    fun getSelectedState(id: Long): Boolean {
        return selectedEditItems.contains(id)
    }

    private fun clearSelectedItems() {
        selectedEditItems = emptySet()
        Log.e(TAG, "Edit items is cleared!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableScreen(
    editableScreenState: EditableScreenState,
    onDeleteSelectedItem: () -> Unit,
    normalModeTopAppBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    additionalEditAction: (@Composable () -> Unit)? = null,
    moreActions: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    snackBarHost: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = editableScreenState.isInEditMode) {
        coroutineScope.launch {
            editableScreenState.exitEditMode()
        }
    }

    Scaffold(
        snackbarHost = { snackBarHost?.invoke() },
        topBar = {
            Crossfade(
                targetState = editableScreenState.isInEditMode
            ) { it ->
                if (it) {
                    //编辑模式
                    TopAppBarWithMenu(
                        title = stringResource(
                            R.string.selected_edit_items_count,
                            editableScreenState.selectionCount
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    editableScreenState.exitEditMode()
                                }
                            }
                            ) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = null,
                                )
                            }
                        },
                        actions = {
                            additionalEditAction?.invoke()

                            IconButton(
                                onClick = {
                                    onDeleteSelectedItem()
                                },
                                enabled = editableScreenState.selectionCount > 0
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = {
                                if (!editableScreenState.isAllSelected) editableScreenState.selectAllEditItems()
                                else editableScreenState.cancelSelectAllItems()
                            }
                            ) {
                                Icon(
                                    if (editableScreenState.isAllSelected) Icons.Filled.Deselect
                                    else Icons.Filled.SelectAll,
                                    contentDescription = null,
                                )
                            }

                            if (moreActions != null) {
                                MoreActionButton(
                                    isExpanded = editableScreenState.isMoreActionsExpanded,
                                    onDismiss = {
                                        editableScreenState.isMoreActionsExpanded = false
                                    }
                                ) { dismiss ->
                                    moreActions(dismiss)
                                }
                            }
                        }
                    )

                } else {
                    normalModeTopAppBar()
                }
            }
        },
        floatingActionButton = floatingActionButton,
        content = content
    )
}