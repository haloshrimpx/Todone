@file:OptIn(ExperimentalMaterial3Api::class)

package com.sijayyx.todone.ui.checklists

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.GlobalViewModel
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.composables.ColorOption
import com.sijayyx.todone.ui.composables.DeleteConfirmDialog
import com.sijayyx.todone.ui.composables.IconTextRow
import com.sijayyx.todone.ui.composables.RoundCheckbox
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.ui.composables.EditableScreen
import com.sijayyx.todone.ui.composables.EditableScreenState
import com.sijayyx.todone.ui.navigation.ScreenNavDestination
import com.sijayyx.todone.ui.composables.TopAppBarWithMenu
import com.sijayyx.todone.ui.composables.rememberEditableScreenState
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.UiEnterState
import com.sijayyx.todone.utils.formatReminderTimestampToString
import com.sijayyx.todone.utils.formatRepeatPeriodToString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistsScreen(
    setGestureEnable: (Boolean) -> Unit,
    openDraw: () -> Unit,
    navController: NavController,
    globalViewModel: GlobalViewModel,
    modifier: Modifier = Modifier,
    viewModel: ChecklistScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isAllowNotification = globalViewModel.isAllowNotification.collectAsState().value

    val checklists = viewModel.allChecklists.collectAsState().value

    val deleteStagingChecklists = viewModel.deleteStagingChecklists.collectAsState().value

    val editableScreenState = rememberEditableScreenState(
        targetElementList = checklists.keys.map { it.id }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deleteStagingChecklists) {
        coroutineScope.launch {
            if (deleteStagingChecklists.isNotEmpty()) {

                //销毁上一次出现的snackbar
                snackbarHostState.currentSnackbarData?.dismiss()

                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.items_deleted_successfully),
                    actionLabel = context.getString(R.string.undo),
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> viewModel.setChecklistsVisible()
                    SnackbarResult.Dismissed -> viewModel.deleteHiddenChecklists()
                }
            }
        }
    }

    LaunchedEffect(editableScreenState.isInEditMode) {
        setGestureEnable(!editableScreenState.isInEditMode)
    }

    ChecklistsScreenContent(
        editableScreenState = editableScreenState,
        snackbarHostState = snackbarHostState,
        checkLists = checklists,
        onCheckValueChanged = { id, isDone -> viewModel.updateItemDoneState(id, isDone) },
        openDraw = openDraw,
        onDelete = { viewModel.deleteChecklistDataById(it) },
        navController = navController,
        formatRemindTime = {
            formatReminderTimestampToString(
                context,
                it,
                fallbackString = EMPTY_STRING
            )
        },
        formatRepeatPeriod = { num, period ->
            formatRepeatPeriodToString(
                context,
                num,
                period,
                fallbackString = EMPTY_STRING
            )
        },
        onDeleteEditItems = {
            coroutineScope.launch {
                viewModel.setChecklistsHidden(editableScreenState.selectedEditItems)
                editableScreenState.exitEditMode()
            }
        },
        onFabClicked = {
            val clickToDo: () -> Unit = {
                navController.navigate(
                    route = ScreenNavDestination.EditChecklist.createRoute(
                        enterState = UiEnterState.Add
                    )
                )
            }

            //在应用内允许通知但没有权限时弹窗提醒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && (!viewModel.canScheduleAlarms())
                && isAllowNotification
            ) {
                globalViewModel.requestPermission(
                    Manifest.permission.SCHEDULE_EXACT_ALARM,
                    onCloseDialog = { clickToDo() }
                )
            } else {
                clickToDo()
            }

        },
        isDarkTheme = { globalViewModel.isDarkTheme(context) },
    )

}

@Composable
fun ChecklistsScreenContent(
    editableScreenState: EditableScreenState,
    snackbarHostState: SnackbarHostState,

    onDeleteEditItems: () -> Unit,

    onFabClicked: () -> Unit,
    checkLists: Map<ChecklistData, List<ChecklistItemData>>,
    openDraw: () -> Unit,
    onDelete: (Long) -> Unit,
    onCheckValueChanged: (Long, Boolean) -> Unit,
    navController: NavController,
    formatRemindTime: (Long) -> String,
    formatRepeatPeriod: (Int, String) -> String, isDarkTheme: () -> Boolean,
    modifier: Modifier = Modifier
) {
    var isShowEditDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val dataList = remember(checkLists) { checkLists.entries.toList() }

    if (isShowEditDeleteDialog) {
        DeleteConfirmDialog(
            title = stringResource(R.string.title_confirm_deletion),
            deleteInfo =
                pluralStringResource(
                    R.plurals.checklist_delete_confirm_message,
                    editableScreenState.selectionCount
                ),
            onDismiss = { isShowEditDeleteDialog = false },
            onConfirm = { onDeleteEditItems() }
        )
    }

    EditableScreen(
        snackBarHost = {
            SnackbarHost(snackbarHostState)
        },
        normalModeTopAppBar = {
            TopAppBarWithMenu(
                title = stringResource(R.string.title_checklists),
                navigationIcon = {
                    IconButton(onClick = openDraw) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!editableScreenState.isInEditMode) {
                FloatingActionButton(
                    onClick = onFabClicked,
                    modifier = Modifier.padding(8.dp),
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        },
        editableScreenState = editableScreenState,
        onDeleteSelectedItem = { isShowEditDeleteDialog = true },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            if (dataList.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(
                        dataList.filter { (checklistData, _) -> !checklistData.isHide },
                    ) { it ->
                        key(it.key.id) {
                            ChecklistCard(
                                checklistData = it.key,
                                checklistItems = it.value,
                                onCheckValueChanged = onCheckValueChanged,
                                onDelete = onDelete,
                                onCardClick = {
                                    Log.e(TAG, "checklist which id = $it clicked!")
                                    navController.navigate(
                                        ScreenNavDestination.EditChecklist.createRoute(
                                            checklistId = it,
                                            enterState = UiEnterState.Edit
                                        )
                                    )
                                },
                                formatRepeatPeriod = formatRepeatPeriod,
                                formatRemindTime = formatRemindTime,
                                isInEditMode = editableScreenState.isInEditMode,
                                isEditSelected = editableScreenState.getSelectedState(it.key.id),
                                onSelectInEditMode = editableScreenState::trySelectItem,
                                onCardLongClick = editableScreenState::enterEditMode,
                                isDarkTheme = isDarkTheme,
                                modifier = Modifier.animateItem(),
                            )
                        }

                    }
                }
            } else {
//                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize())
//                {
//                    Text(
//                        "No checklists here yet. \nClick \"+\" to add a checklist.",
//                        textAlign = TextAlign.Center,
//                        color = MaterialTheme.colorScheme.secondary,
//                    )
//                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ChecklistCard(
    checklistData: ChecklistData,
    checklistItems: List<ChecklistItemData>,
    isInEditMode: Boolean,
    isEditSelected: Boolean,
    onSelectInEditMode: (id: Long, isSelect: Boolean) -> Unit,
    onCardLongClick: (Long) -> Unit,
    formatRepeatPeriod: (Int, String) -> String,
    formatRemindTime: (Long) -> String,
    onDelete: (Long) -> Unit,
    onCardClick: (Long) -> Unit,
    onCheckValueChanged: (Long, Boolean) -> Unit,
    isDarkTheme: () -> Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    val totalItemsNum = remember(checklistItems) { checklistItems.count() }
    val doneItemsNum = remember(checklistItems) { checklistItems.count { it.isDone } }
    val remind = remember(checklistData) { formatRemindTime(checklistData.remindTimestamp) }
    val repeat = remember(checklistData) {
        formatRepeatPeriod(
            checklistData.repeatNum,
            checklistData.repeatPeriod
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onLongClick = {
                    if (!isInEditMode && !isEditSelected)
                        onCardLongClick(checklistData.id)
                },
                onClick = {
                    if (!isInEditMode) onCardClick(checklistData.id)
                    else onSelectInEditMode(
                        checklistData.id,
                        isEditSelected
                    )
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    color = ColorOption.formatStringToColor(
                        checklistData.color,
                        isDarkTheme()
                    ) ?: CardDefaults.cardColors().containerColor
//                    brush = Brush.horizontalGradient(
//                        0.0f to (ColorOption.formatStringToColor(
//                            checklistData.color,
//                            isDarkTheme()
//                        )
//                            ?: CardDefaults.cardColors().containerColor),
//
//                        1f to CardDefaults.cardColors().containerColor
//                    ),
                )

        ) {
            Crossfade(
                targetState = !isInEditMode
            ) {
                if (it) {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (!isExpanded) Icons.AutoMirrored.Filled.KeyboardArrowRight
                            else Icons.Filled.KeyboardArrowDown,

                            contentDescription = null
                        )
                    }
                } else {
                    RoundCheckbox(
                        checked = isEditSelected,
                        onCheckedChange = {
                            onSelectInEditMode(
                                checklistData.id,
                                isEditSelected
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = checklistData.checklistName,
                    fontSize = 22.sp,
                    maxLines = if (!isExpanded) 1 else 5,
                    overflow = TextOverflow.Ellipsis, //超出部分显示省略号
                    modifier = Modifier
                        .animateContentSize()
                )
                FlowRow {
                    IconTextRow(
                        text = remind,
                        icon = Icons.Filled.Notifications,
                        hideIfEmpty = true
                    )
                    if (remind.isNotEmpty())
                        Spacer(Modifier.padding(4.dp))

                    IconTextRow(
                        text = repeat,
                        icon = Icons.Filled.Repeat,
                        hideIfEmpty = true
                    )
                }

            }
            if (!checklistItems.isEmpty())
                Text(text = "$doneItemsNum/$totalItemsNum", modifier = Modifier.padding(12.dp))
        }

        AnimatedVisibility(
            visible = isExpanded && !checklistItems.isEmpty()
        ) {

            HorizontalDivider(Modifier.padding(horizontal = 8.dp))

            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 18.dp)
                    .fillMaxWidth()
            ) {
                checklistItems.sortedBy { it.checklistItemId }.forEach { it ->
                    key(it.checklistItemId) {
                        ChecklistItem(
                            checklistItemData = it,
                            onCheckValueChanged = onCheckValueChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItem(
    checklistItemData: ChecklistItemData,
    onCheckValueChanged: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val decoration = remember(checklistItemData.isDone) {
        if (checklistItemData.isDone)
            TextDecoration.LineThrough
        else
            TextDecoration.None
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Checkbox(
            checked = checklistItemData.isDone,
            onCheckedChange = { onCheckValueChanged(checklistItemData.checklistItemId, it) },
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = checklistItemData.content,
            fontSize = 18.sp,
            textDecoration = decoration,
            overflow = TextOverflow.Ellipsis, //超出部分显示省略号
            maxLines = 5,
            modifier = Modifier.weight(1f)
//                .widthIn(20.dp, 200.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChecklistItemPreview() {
    TodoListTheme {
        ChecklistItem(
            checklistItemData = ChecklistItemData(
                content = "awdawdaw", checklistId = 1,
                isDone = false,
            ),
            onCheckValueChanged = { p1, p2 -> }
        )
    }
}

@Preview
@Composable
fun ChecklistElementPreview() {
    TodoListTheme {
        ChecklistCard(
            ChecklistData(checklistName = "Fuck you"),
            listOf(
                ChecklistItemData(
                    content = "awfaw  awfawa", checklistId = 1,
                    isDone = false,
                ),
                ChecklistItemData(
                    content = "awgaw geah4", checklistId = 1,
                    isDone = false,
                ),
                ChecklistItemData(
                    content = " etjeer ", checklistId = 1,
                    isDone = false,
                ),
                ChecklistItemData(
                    content = "wwrywyj ", checklistId = 1,
                    isDone = false,
                ),
                ChecklistItemData(
                    content = "wrywrheh", checklistId = 1,
                    isDone = false,
                ),

                ),
            onCardClick = {},
            onCheckValueChanged = { p1, p2 -> },
            onDelete = {},
            formatRepeatPeriod = { p1, p2 -> "" },
            formatRemindTime = { "" },
            isInEditMode = false,
            isEditSelected = false,
            onSelectInEditMode = { p1, p2 -> },
            onCardLongClick = {},
            isDarkTheme = { false }
        )
    }
}

@Preview
@Composable
fun ChecklistsScreenPreview() {
    TodoListTheme {
        ChecklistsScreenContent(
            navController = rememberNavController(),
            openDraw = {},
            checkLists = mapOf(),
            onCheckValueChanged = { p1, p2 -> },
            onDelete = {},
            formatRemindTime = { "" },
            formatRepeatPeriod = { p1, p2 -> "" },

            onDeleteEditItems = {},
            onFabClicked = {},
            editableScreenState = rememberEditableScreenState(),
            isDarkTheme = { false },
            snackbarHostState = SnackbarHostState()
        )
    }
}