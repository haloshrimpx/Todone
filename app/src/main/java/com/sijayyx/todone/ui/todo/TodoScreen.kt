package com.sijayyx.todone.ui.todo

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.GlobalViewModel
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.composables.DeleteConfirmDialog
import com.sijayyx.todone.ui.composables.DropdownIconTextButton
import com.sijayyx.todone.ui.composables.EditTodoListDialog
import com.sijayyx.todone.ui.composables.FutureDatePicker
import com.sijayyx.todone.ui.composables.IconTextRow
import com.sijayyx.todone.ui.composables.RoundCheckbox
import com.sijayyx.todone.utils.DEFAULT_LIST_NAME
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.data.TodoItemData
import com.sijayyx.todone.data.TodoListData
import com.sijayyx.todone.ui.composables.DropdownItemElement
import com.sijayyx.todone.ui.composables.EditableScreen
import com.sijayyx.todone.ui.composables.EditableScreenState
import com.sijayyx.todone.ui.composables.ExtendedIcons
import com.sijayyx.todone.ui.composables.InputFieldBottomBar
import com.sijayyx.todone.ui.composables.MoveGroupDialog
import com.sijayyx.todone.utils.formatTimestampToReadableString
import com.sijayyx.todone.utils.timeAfterOfLocalDate
import com.sijayyx.todone.ui.composables.TopAppBarWithMenu
import com.sijayyx.todone.ui.composables.rememberEditableScreenState
import com.sijayyx.todone.ui.navigation.ScreenNavDestination
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.UiEnterState
import com.sijayyx.todone.utils.checkTimeInToday
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun TodoScreen(
    setGestureEnable: (Boolean) -> Unit,
    listIdToShow: Long?,
    displayType: TodoDisplayType,
    openDraw: () -> Unit,
    navController: NavController,
    permissionViewModel: GlobalViewModel,
    modifier: Modifier = Modifier,
    viewModel: TodoScreenViewModel = viewModel(
        key = "todo_${listIdToShow}_$displayType",
        factory = AppViewModelProvider.factory
    ),
) {
    Log.e(TAG, "TodoScreen: composition! list id = $listIdToShow, $displayType")
    val context = LocalContext.current

    val isAllowNotification = permissionViewModel.isAllowNotification.collectAsState().value

    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsState()

    val allTodoLists = viewModel.allTodoLists.collectAsState()
    val allTodoItems = viewModel.allTodoItems.collectAsState()
    val allTodoListData = viewModel.allTodoListDatas.collectAsState() //所有已经添加的Todo列表

    //选定的列表
    val cachedSelectedList = remember(viewModel.cachedSelectedList) { viewModel.cachedSelectedList }
    val selectedList by produceState<TodoListData?>(
        initialValue = null,
        key1 = listIdToShow,
    ) {
        viewModel.getSelectedListById(listIdToShow).collect {
            value = it
        }
    }
    val displaySelectedList =
        remember(selectedList, listIdToShow, displayType) {
            selectedList ?: cachedSelectedList[uiState.value.selectedListId]
        }

    val topTitle = remember(displayType, listIdToShow, displaySelectedList) {
        derivedStateOf {
            if (displayType == TodoDisplayType.SpecifiedList) {
                if (displaySelectedList != null) {
                    "${
                        if (displaySelectedList.listIcon != NONE_STRING) displaySelectedList.listIcon
                        else EMPTY_STRING
                    }${displaySelectedList.listName}"
                } else {
                    Log.e(TAG, "FAILED to get displaySelectedList")
                    context.getString(R.string.title_todo)
                }
            } else if (displayType == TodoDisplayType.Today) {
                context.getString(R.string.title_today)
            } else context.getString(R.string.title_todo)
        }
    }.value

    val isBottomBarVisible =
        remember(uiState.value.textInputState) { uiState.value.textInputState != UiEnterState.None }
    var isScreenEmpty by rememberSaveable { mutableStateOf(false) }


    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    //全选时选定的列表
    val targetItems = remember(allTodoItems.value, displayType) {
        derivedStateOf {
            allTodoItems.value.filter {
                when (displayType) {
                    TodoDisplayType.SpecifiedList -> {
                        it.todoListId == listIdToShow
                    }

                    TodoDisplayType.Today -> {
                        checkTimeInToday(it.deadlineStamp)
                    }

                    else -> true
                }
            }
        }
    }.value

    val editableScreenState = rememberEditableScreenState(
        targetElementList = targetItems.map { it.todoItemId }
    )

    val dismissInput: () -> Unit = remember {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
            viewModel.onEndInput()
        }
    }

    val initialSelectedReminderTime =
        remember(uiState.value.selectedReminder) { viewModel.formatTimeFromTimestamp(uiState.value.selectedReminder) }
    val selectedRemindTime =
        remember(uiState.value.selectedReminder) {
            viewModel.checkSelectedRemindTime(
                context,
                uiState.value.selectedReminder,
                context.getString(R.string.reminder)
            )
        }
    val selectedDeadline = remember(uiState.value.selectedDeadline) {
        viewModel.formatDeadlineTimestamp(
            context,
            uiState.value.selectedDeadline,
            context.getString(R.string.deadline)
        )
    }
    val selectedListName = remember(uiState.value.selectedListName) {
        viewModel.checkSelectedListName(
            uiState.value.selectedListName,
            context.getString(R.string.task)
        )
    }

    val initialSelectedReminderUtcDate = remember(uiState.value.selectedReminder) {
        if (uiState.value.selectedReminder > 0) uiState.value.selectedReminder
        else System.currentTimeMillis()
    }
    val initialSelectedDeadlineDate = remember(uiState.value.selectedDeadline) {
        if (uiState.value.selectedDeadline > 0)
            uiState.value.selectedDeadline
        else
            System.currentTimeMillis()
    }

    // 当输入框显示时自动获取焦点和打开键盘
    LaunchedEffect(isBottomBarVisible) {
        if (isBottomBarVisible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(editableScreenState.isInEditMode, isBottomBarVisible) {
        setGestureEnable(!isBottomBarVisible && !editableScreenState.isInEditMode)
    }

    // 处理返回键
    BackHandler(enabled = isBottomBarVisible) {
        dismissInput()
    }

    TodoScreenContent(
        editableScreenState = editableScreenState,
        onDeleteEditItems = {
            coroutineScope.launch {
                viewModel.deleteTodoItemDataList(editableScreenState.selectedEditItems)
                editableScreenState.exitEditMode()
            }
        },
        onMoveSelectedItems = {
            coroutineScope.launch {
                viewModel.moveItemsToList(editableScreenState.selectedEditItems, it)
                editableScreenState.exitEditMode()
            }
        },
        shareSelectedItems = {
            viewModel.startSharing(context, editableScreenState.selectedEditItems)
        },

        isScreenEmpty = isScreenEmpty,
        onDisplayContentInScreen = { isScreenEmpty = false },

        topTitle = topTitle,

        selectedListData = displaySelectedList,

        displayType =
            if (listIdToShow != null) displayType
            else TodoDisplayType.All,

        getFormattedDeadline = {
            viewModel.formatDeadlineTimestamp(
                context,
                it,
                fallbackString = EMPTY_STRING
            )
        },

        onSelectRemindTime = { date, hour, min ->
            viewModel.updateSelectedRemindTime(
                date,
                hour,
                min
            )
        },

        onRenameList = { listData, newName, color, icon, isRenamed ->
            viewModel.onRenameSelectedList(
                selectedList = listData,
                newName = newName,
                color = color,
                icon = icon,
                isRenamed = isRenamed
            )
        },
        onDeleteList = {
            viewModel.onDeleteSelectedList(it)
            //由于这个按钮一定是在显示当前列表时删除，所以退回导航栈
            navController.navigateUp()
        },

        onSelectRemindString = { viewModel.updateSelectedRemindTime(it) },
        selectedRemindTime = { selectedRemindTime },
        initialSelectedReminderTime = initialSelectedReminderTime,//viewModel.formatTimeFromTimestamp(uiState.value.selectedReminder),
        initialSelectedReminderUtcDate = initialSelectedReminderUtcDate,
        initialSelectedDeadlineDate = initialSelectedDeadlineDate,

        getFormattedReminder = {
            viewModel.formatReminderTimestamp(
                context,
                it,
                EMPTY_STRING
            )
        },

        checkTimestampInToday = { viewModel.checkTimestampInToday(it) },

        inputTextValue = uiState.value.itemContent,
        onInputValueChange = { viewModel.updateInputContent(it) },
        onSelectDeadline = { viewModel.updateSelectedDeadline(it) },
        onPickDateDeadline = { viewModel.updateSelectedDeadline(it) },
        selectedDeadline = {
            selectedDeadline
        },
        selectedListName = {
            selectedListName
        },
        onSelectTodoList = { viewModel.updateSelectedList(it) },
        confirmAddNewItem = { viewModel.addNewItem() },
        confirmEditItem = { viewModel.updateItemData() },
        openDraw = openDraw,
        dismissInput = dismissInput,
        isBottomBarVisible = isBottomBarVisible,
        setAddInputState = {

            //在应用内允许通知但没有权限时弹窗提醒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && (!viewModel.canScheduleAlarms())
                && isAllowNotification
            ) {
                permissionViewModel.requestPermission(
                    Manifest.permission.SCHEDULE_EXACT_ALARM,
                    onCloseDialog = { viewModel.setAddInputState(listIdToShow, displayType) }
                )
            } else {
                viewModel.setAddInputState(listIdToShow, displayType)
            }
//                viewModel.setAddInputState(listIdToShow, displayType)

        },
        setEditInputState = {
            viewModel.setEditInputState()
            viewModel.onEditItem(it)
        },
        onSelectDelete = { viewModel.deleteTodoItemData(it) },
        onDoneStateChange = { id, isDone -> viewModel.setTodoItemDoneState(isDone, id) },
        focusRequester = focusRequester,
        textInputState = uiState.value.textInputState,

        allTodos = allTodoLists.value,
        allTodoItems = allTodoItems.value,
        allTodoListData = allTodoListData.value,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreenContent(
    editableScreenState: EditableScreenState,
    onDeleteEditItems: () -> Unit,
    onMoveSelectedItems: (Long) -> Unit,
    shareSelectedItems: () -> Unit,

    topTitle: String,
//    isListNotExists: (Long) -> Boolean,
    displayType: TodoDisplayType,
    inputTextValue: String,
    onInputValueChange: (String) -> Unit,

    isScreenEmpty: Boolean,
    onDisplayContentInScreen: () -> Unit,
    selectedListData: TodoListData?,

    onSelectDeadline: (String) -> Unit,
    onPickDateDeadline: (Long?) -> Unit,
    initialSelectedDeadlineDate: Long,

    getFormattedDeadline: (Long) -> String,
    getFormattedReminder: (Long) -> String,

    onSelectRemindString: (String) -> Unit,
    onSelectRemindTime: (Long, Int, Int) -> Unit,
    initialSelectedReminderTime: Pair<Int, Int>,
    initialSelectedReminderUtcDate: Long,

    checkTimestampInToday: (Long) -> Boolean,
    onSelectTodoList: (String) -> Unit,

    selectedListName: () -> String,
    selectedDeadline: () -> String,
    selectedRemindTime: () -> String,

    confirmAddNewItem: () -> Unit,
    confirmEditItem: () -> Unit,
    openDraw: () -> Unit,
    dismissInput: () -> Unit,
    isBottomBarVisible: Boolean,
    setAddInputState: () -> Unit,
    setEditInputState: (Long) -> Unit,
    onSelectDelete: (Long) -> Unit,
    onDoneStateChange: (Long, Boolean) -> Unit,
    focusRequester: FocusRequester,
    textInputState: UiEnterState,

    onRenameList: (TodoListData, title: String, color: String, icon: String, isRenamed: Boolean) -> Unit,
    onDeleteList: (TodoListData) -> Unit,

    modifier: Modifier = Modifier,

    allTodoListData: List<TodoListData> = listOf(),
    allTodos: Map<TodoListData, List<TodoItemData>> = mapOf(),
    allTodoItems: List<TodoItemData> = listOf(),
) {
    var isShowMoveGroupDialog by rememberSaveable { mutableStateOf(false) }
    var isShowDatePicker by rememberSaveable { mutableStateOf(false) }
    var isShowReminderPicker by rememberSaveable { mutableStateOf(false) }
    var isShowRenameListDialog by rememberSaveable { mutableStateOf(false) }
    var isShowEditDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isShowSingleListDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isShowHelpDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val moveGroupOption = remember {
        (allTodoListData.filter { it.listName != DEFAULT_LIST_NAME }
            .associate { it.id to it.listName }
                + mapOf(-1L to context.getString(R.string.none)))
    }

    val allTodoShowingList = remember(allTodos, displayType) {
        derivedStateOf {
            allTodos.filter { (todoListData, _) -> todoListData.listName == DEFAULT_LIST_NAME }
        }
    }.value

    val exceptTodayShowingList = remember(allTodos, displayType) {
        derivedStateOf {
            allTodos.filter { (todoListData, _) ->
                if (displayType == TodoDisplayType.SpecifiedList) {
                    todoListData.id == selectedListData?.id
                } else //过滤掉默认列表名字
                    todoListData.listName != DEFAULT_LIST_NAME
            }
        }
    }.value

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    if (isShowEditDeleteDialog) {
        DeleteConfirmDialog(
            title = stringResource(R.string.title_confirm_deletion),
            deleteInfo = pluralStringResource(
                R.plurals.delete_item_confirm_message,
                editableScreenState.selectionCount
            ),
            onDismiss = { isShowEditDeleteDialog = false },
            onConfirm = { onDeleteEditItems() }
        )
    }

    if (isShowSingleListDeleteDialog) {
        DeleteConfirmDialog(
            title = stringResource(R.string.title_confirm_deletion),
            deleteInfo = stringResource(R.string.delete_list_confirm_message),
            onDismiss = { isShowSingleListDeleteDialog = false },
            onConfirm = {
                if (selectedListData != null)
                    onDeleteList(selectedListData)
            }
        )
    }

    if (isShowDatePicker) {
        FutureDatePicker(
            initialSelectedFutureDate = initialSelectedDeadlineDate,
            onDateSelected = onPickDateDeadline,
            onDismiss = { isShowDatePicker = false },
        )
    }

    if (isShowReminderPicker) {
        ReminderTimePicker(
            initialSelectedReminderDate = initialSelectedReminderUtcDate,
            initialSelectedReminderTime = initialSelectedReminderTime,
            onConfirmSelect = onSelectRemindTime,
            onDismiss = { isShowReminderPicker = false })
    }

    if (isShowRenameListDialog && selectedListData != null) {
        EditTodoListDialog(
            dialogTitle = stringResource(R.string.rename_list),
            initialTextValue = selectedListData.listName,
            onDismiss = { isShowRenameListDialog = false },
            onConfirm = { name, color, icon ->
                onRenameList(
                    selectedListData,
                    name,
                    color,
                    icon,
                    name != selectedListData.listName //是否重命名了
                )
            },
            initialColor = selectedListData.listColor,
            initialIcon = selectedListData.listIcon,
        )
    }

    //移动选定的项到新的列表中
    if (isShowMoveGroupDialog && editableScreenState.isSelectedItem) {
        MoveGroupDialog(
            options = moveGroupOption,

            onDismiss = { isShowMoveGroupDialog = false },
            onConfirm = { onMoveSelectedItems(it) }
        )
    }

    EditableScreen(
        editableScreenState = editableScreenState,
        onDeleteSelectedItem = { isShowEditDeleteDialog = true },
        moreActions = { dismiss ->
            DropdownItemElement(
                text = "Share",
                icon = Icons.Filled.Share,
                onClick = {
                    shareSelectedItems()
                    dismiss()
                },
                enabled = editableScreenState.isSelectedItem,
            )
            DropdownItemElement(
                text = "Move",
                icon = ExtendedIcons.MoveGroup,
                onClick = {
                    isShowMoveGroupDialog = true
                    dismiss()
                },
                enabled = editableScreenState.isSelectedItem,
            )
        },
        normalModeTopAppBar = {
            //正常模式
            TopAppBarWithMenu(
                title = topTitle,
                navigationIcon = {
                    IconButton(onClick = openDraw) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (displayType == TodoDisplayType.SpecifiedList && selectedListData != null) {
                        IconButton(onClick = { isShowSingleListDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { isShowRenameListDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isBottomBarVisible && !editableScreenState.isInEditMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        setAddInputState()
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .semantics {
                            contentDescription = "Todo add FAB"
                        },
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (displayType == TodoDisplayType.Today) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    ) {
                        //仅在Today中显示

//                        Text(
//                            text = stringResource(R.string.today_hi),
//                            fontWeight = FontWeight.W500,
//                            fontSize = 48.sp,
//                        )
//                        Text(
//                            text = stringResource(R.string.today_what_you_gonna_do_today),
//                            fontSize = 28.sp,
//                            lineHeight = 36.sp,
//                            fontWeight = FontWeight.W500,
//                            modifier = Modifier.fillMaxWidth()
//                        )
                        Text(
                            text = formatTimestampToReadableString(
                                System.currentTimeMillis(),
                                DateFormatters.monthDayAndWeekday(context)
                            ),
                            fontWeight = FontWeight.W500,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
//                                modifier = Modifier.align(Alignment.Bottom)
                        )
                    }

                    TodoListElement(
                        hideTitleElement = true,
                        isInEditMode = editableScreenState.isInEditMode,
                        onEnterEditMode = editableScreenState::enterEditMode,
                        onSelectInEditMode = editableScreenState::trySelectItem,
                        getSelectedState = editableScreenState::getSelectedState,

                        listTitle = stringResource(R.string.title_today),
                        onCardEdit = { setEditInputState(it) },
                        listRawContent = { allTodoItems },
                        onDoneStateChange = { id, isDone ->
                            onDoneStateChange(id, isDone)
                        },
                        filterFunction = { checkTimestampInToday(it.deadlineStamp) && !it.isDone }, //必须在今天且未完成
                        onSelectDelete = onSelectDelete,
                        getFormattedDeadline = getFormattedDeadline,
                        getFormattedReminder = getFormattedReminder,
                        onDisplayContentInScreen = onDisplayContentInScreen
                    )
                }

                //默认列表，仅在All时显示
                if (displayType == TodoDisplayType.All) {
                    allTodoShowingList.forEach { (todoParent, todosList) ->
                        key(todoParent.id) {
                            TodoListElement(
                                isInEditMode = editableScreenState.isInEditMode,
                                onEnterEditMode = editableScreenState::enterEditMode,
                                onSelectInEditMode = editableScreenState::trySelectItem,
                                getSelectedState = editableScreenState::getSelectedState,
                                listTitle = stringResource(R.string.list_title_in_progress),
                                listRawContent = { todosList },
                                onCardEdit = { setEditInputState(it) },
                                onDoneStateChange = { id, isDone ->
                                    onDoneStateChange(id, isDone)
                                },
                                filterFunction = { todoItemData ->
                                    !todoItemData.isDone
                                },
                                onSelectDelete = onSelectDelete,
                                getFormattedDeadline = getFormattedDeadline,
                                getFormattedReminder = getFormattedReminder,
                                onDisplayContentInScreen = onDisplayContentInScreen
                            )
                        }
                    }
                }

                if (displayType != TodoDisplayType.Today) {
                    exceptTodayShowingList.forEach { (todoListData, todosList) ->
                        Log.e("MainActivity", "allTodosForEach - ${todoListData.listName}")
                        key(todoListData.id) {
                            TodoListElement(
                                isInEditMode = editableScreenState.isInEditMode,
                                onEnterEditMode = editableScreenState::enterEditMode,
                                onSelectInEditMode = editableScreenState::trySelectItem,
                                getSelectedState = editableScreenState::getSelectedState,
                                listTitle = //如果是单独的列表，为了避免与大标题重复，改为"Undone"
                                    if (displayType == TodoDisplayType.SpecifiedList) stringResource(
                                        R.string.todo_list_title_undone
                                    )
                                    else {
                                        "${
                                            if (todoListData.listIcon != NONE_STRING) todoListData.listIcon
                                            else EMPTY_STRING
                                        } ${todoListData.listName}"
                                    },
                                listRawContent = { todosList },
                                onCardEdit = { setEditInputState(it) },
                                onDoneStateChange = { id, isDone ->
                                    onDoneStateChange(id, isDone)
                                },
                                filterFunction = { todoItemData ->
                                    !todoItemData.isDone
                                },
                                onSelectDelete = onSelectDelete,
                                getFormattedDeadline = getFormattedDeadline,
                                getFormattedReminder = getFormattedReminder,
                                onDisplayContentInScreen = onDisplayContentInScreen
                            )
                        }

                    }
                }

                TodoListElement(
                    hideWhenListEmpty = true,
                    initialExpandState = false,
                    isInEditMode = editableScreenState.isInEditMode,
                    onEnterEditMode = editableScreenState::enterEditMode,
                    onSelectInEditMode = editableScreenState::trySelectItem,
                    getSelectedState = editableScreenState::getSelectedState,
                    listTitle = stringResource(R.string.todo_list_title_done),
                    listRawContent =
                        {
                            when (displayType) {
                                TodoDisplayType.SpecifiedList -> allTodoItems.filter { itemData -> itemData.todoListId == selectedListData?.id }
                                TodoDisplayType.Today -> allTodoItems.filter { itemData ->
                                    checkTimestampInToday(
                                        itemData.deadlineStamp
                                    )
                                }

                                else -> allTodoItems
                            }
                        },
                    onCardEdit = { },
                    onDoneStateChange = { id, isDone ->
                        onDoneStateChange(id, isDone)
                    },
                    filterFunction = { todoItemData ->
                        todoItemData.isDone
                    },
                    onSelectDelete = onSelectDelete,
                    getFormattedDeadline = getFormattedDeadline,
                    getFormattedReminder = getFormattedReminder,
                    onDisplayContentInScreen = onDisplayContentInScreen
                )
            }
        }
    }

    //输入框，放在这可以让遮罩遮住除输入框内的所有内容
    AnimatedVisibility(
        visible = isBottomBarVisible && !editableScreenState.isInEditMode,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        InputFieldBottomBar(
            onDismiss = dismissInput,
            textValue = inputTextValue,
            onValueChange = onInputValueChange,
            focusRequester = focusRequester,
            inputFieldLabel = stringResource(R.string.input_bar_label_todo),

            //取消键
            leadingAction = {
                IconButton(onClick = {
                    dismissInput()
                }) {
                    Icon(
                        Icons.Outlined.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            //发送键
            trailingAction = {
                IconButton(
                    modifier = Modifier.semantics { contentDescription = "Confirm Input Button" },
                    enabled = it,
                    onClick = {
                        Log.e(TAG, "Text input state = $textInputState")
                        when (textInputState) {
                            UiEnterState.Add -> {
                                confirmAddNewItem()
                            }

                            UiEnterState.Edit -> {
                                confirmEditItem()
                            }

                            else -> {}
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            bottomActions = {
                DropdownIconTextButton(
                    selectedListName(),
                    icon = Icons.Filled.Checklist,
                    menuItemList = (allTodoListData.map { it.listName }
                        .filter { it != DEFAULT_LIST_NAME }).associateWith { it }
                            + mapOf(stringResource(R.string.none) to NONE_STRING),
                    onMenuItemSelectString = { onSelectTodoList(it) },
                    modifier = Modifier
                        .animateContentSize()
                        .semantics {
                            contentDescription = "Select list"
                        },
                )
                DropdownIconTextButton(
                    selectedDeadline(),
                    icon = Icons.Filled.DateRange,
                    modifier = Modifier
                        .animateContentSize()
                        .semantics {
                            contentDescription = "Select deadline"
                        },
                    //可选的截止日期：无，今天，明天，后天，下周
                    menuItemList = mapOf(
                        stringResource(R.string.option_today) to TodoDeadline.Today.content,
                        stringResource(R.string.option_tomorrow) to TodoDeadline.Tomorrow.content,
                        stringResource(R.string.option_in_two_days) to TodoDeadline.InTwoDays.content,
                        stringResource(R.string.option_next_week) to TodoDeadline.NextWeek.content
                    ),
                    onMenuItemSelectString = { onSelectDeadline(it) },
                    menuItems = {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_a_deadline)) },
                            onClick = {
                                it()
                                isShowDatePicker = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.none)) },
                            onClick = {
                                it()
                                onSelectDeadline(NONE_STRING)
                            },
                        )

                    }
                )
                DropdownIconTextButton(
                    label = selectedRemindTime(),
                    icon = Icons.Filled.AddAlert,
                    onMenuItemSelectString = onSelectRemindString,
                    menuItems = {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_a_time)) },
                            onClick = {
                                it()
                                isShowReminderPicker = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.none)) },
                            onClick = {
                                it()
                                onSelectRemindString(NONE_STRING)
                            },
                        )
                    },
                    menuItemList = mapOf(
                        stringResource(R.string.one_hour_later) to RemindOptions.OneHourLater.content,
                        stringResource(R.string.tomorrow_morning_09_00) to RemindOptions.TomorrowMorning.content,
                    ),
                    modifier = Modifier
                        .animateContentSize()
                        .semantics {
                            contentDescription = "Select reminder"
                        },
                )
            },
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TodoListElement(
    listTitle: String,
    isInEditMode: Boolean,
    onEnterEditMode: (startId: Long) -> Unit,
    onSelectInEditMode: (Long, Boolean) -> Unit,
    getSelectedState: (Long) -> Boolean,
    onDisplayContentInScreen: () -> Unit,
    onCardEdit: (Long) -> Unit,
    onSelectDelete: (Long) -> Unit,
    getFormattedDeadline: (Long) -> String,
    getFormattedReminder: (Long) -> String,
    onDoneStateChange: (Long, Boolean) -> Unit,
    filterFunction: (TodoItemData) -> Boolean,
    listRawContent: () -> List<TodoItemData>,
    modifier: Modifier = Modifier,
    hideWhenListEmpty: Boolean = true,
    initialExpandState: Boolean = true,
    hideTitleElement: Boolean = false,
    showListNameInCard: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by rememberSaveable { mutableStateOf(initialExpandState) }
    val filteredList = remember(listRawContent()) {
        derivedStateOf { listRawContent().filter { filterFunction(it) } }
    }.value

    val isListTitleVisible =
        remember(filteredList) { !(hideWhenListEmpty && filteredList.isEmpty()) && !hideTitleElement }

    AnimatedVisibility(
        visible = isListTitleVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(modifier = modifier) {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(start = 4.dp, end = 10.dp)
            ) {
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector =
                            if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
                Text(
                    text = listTitle,
                    modifier = Modifier.semantics {
                        contentDescription = "Title $listTitle"
                    }
                )

                if (filteredList.count() != 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(text = "${filteredList.count()}", textAlign = TextAlign.End)
                }
            }
        }
    }

    if (isListTitleVisible) {
        Spacer(Modifier.height(8.dp))
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            filteredList.forEach { item ->
                key(item.todoItemId) {
                    var visible by remember { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut()
                    ) {
                        TodoCard(
                            isEditSelected = getSelectedState(item.todoItemId),
                            onSelectInEditMode = onSelectInEditMode,
                            onCardLongClick = onEnterEditMode, //启动编辑模式
                            isInEditMode = isInEditMode,

                            todoItemData = item,
                            onDoneStateChange = { onDoneStateChange(item.todoItemId, it) },
                            onCardEdit = onCardEdit,
                            onSelectDelete = {
                                coroutineScope.launch {
                                    visible = false
                                    delay(300)
                                    onSelectDelete(it)
                                }
                            },
                            showListName = showListNameInCard,
                            listName = EMPTY_STRING, //todo
                            getFormattedDeadline = getFormattedDeadline,
                            getFormattedReminder = getFormattedReminder,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }

            }
        }
    }
}


@OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun TodoCard(
    onCardLongClick: (Long) -> Unit,
    getFormattedDeadline: (Long) -> String,
    getFormattedReminder: (Long) -> String,
    onSelectDelete: (Long) -> Unit,
    isInEditMode: Boolean,
    isEditSelected: Boolean,
    todoItemData: TodoItemData,
    onDoneStateChange: (Boolean) -> Unit,
    onCardEdit: (Long) -> Unit,
    showListName: Boolean,
    listName: String,
    onSelectInEditMode: (id: Long, isSelect: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val deadline = remember(todoItemData) { getFormattedDeadline(todoItemData.deadlineStamp) }
    val reminder = remember(todoItemData) { getFormattedReminder(todoItemData.reminderStamp) }
    val decoration = remember {
        if (todoItemData.isDone)
            TextDecoration.LineThrough
        else
            TextDecoration.None
    }

    var currentProgress by remember { mutableFloatStateOf(0f) }
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            // 从右往左滑时删除
            if (it == SwipeToDismissBoxValue.EndToStart && currentProgress >= 0.5f) {
                onSelectDelete(todoItemData.todoItemId)
                true
            } else
                false
        },
        positionalThreshold = { it / 2 }
    )

    UpdateSwipeData { currentProgress = swipeToDismissBoxState.progress }

    AnimatedVisibility(
        visible = true
    ) {
        SwipeToDismissBox(
            modifier = Modifier.semantics {
                contentDescription = "Todo Card ${todoItemData.content} Swipe"
            },
            state = swipeToDismissBoxState,
            backgroundContent = {
                when (swipeToDismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        Crossfade(
                            targetState = swipeToDismissBoxState.targetValue,
                            animationSpec = tween(300)
                        ) {
                            when (it) {
                                SwipeToDismissBoxValue.EndToStart ->
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Red)
                                            .wrapContentSize(Alignment.CenterEnd)
                                            .padding(12.dp),
                                    )


                                else ->
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        tint = Color.Red,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                            .wrapContentSize(Alignment.CenterEnd)
                                            .padding(12.dp),
                                    )
                            }

                        }

                    }

                    else -> {}
                }

            },
            gesturesEnabled = !isInEditMode,
            enableDismissFromStartToEnd = false,
        ) {
            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (!isInEditMode) onCardEdit(todoItemData.todoItemId)
                            else {
                                onSelectInEditMode(todoItemData.todoItemId, isEditSelected)
                            }
                        },
                        onLongClick = {
                            if (!isInEditMode && !isEditSelected) {
                                onCardLongClick(todoItemData.todoItemId)
                            }
                        }
                    )
                    .semantics {
                        contentDescription = "Todo Card ${todoItemData.content}"
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Crossfade(
                        targetState = !isInEditMode
                    ) {
                        when (it) {
                            true -> Checkbox(
                                checked = todoItemData.isDone,
                                onCheckedChange = onDoneStateChange
                            )

                            false -> RoundCheckbox(
                                checked = isEditSelected,
                                onCheckedChange = { _ ->
                                    onSelectInEditMode(
                                        todoItemData.todoItemId,
                                        isEditSelected
                                    )
                                }
                            )
                        }

                    }

                    Column {
                        Text(
                            text = todoItemData.content,
                            textDecoration = decoration,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .widthIn(20.dp, 250.dp)
                                .animateContentSize()
                        )
                        FlowRow(
                            modifier = Modifier.animateContentSize()
                        ) {
                            if (showListName) {
                                IconTextRow(
                                    text = listName,
                                    hideIfEmpty = true,
                                    icon = Icons.Filled.Checklist,
                                    modifier = Modifier.semantics {
                                        contentDescription = "List IconTextRow"
                                    }
                                )
                            }

                            IconTextRow(
                                text = deadline,
                                hideIfEmpty = true,
                                icon = Icons.Filled.DateRange,
                                modifier = Modifier.semantics {
                                    contentDescription = "Deadline IconTextRow"
                                }
                            )

                            if (deadline.isNotEmpty())
                                Spacer(Modifier.padding(4.dp))

                            IconTextRow(
                                text = reminder,
                                hideIfEmpty = true,
                                icon = Icons.Filled.NotificationsActive,
                                modifier = Modifier.semantics {
                                    contentDescription = "Reminder IconTextRow"
                                }
                            )
                        }
                    }

                    //            Spacer(Modifier.weight(1f))
                    //
                    //            if (!isInEditMode) {
                    //                IconButton(
                    //                    onClick = { isExpanded = !isExpanded },
                    //                ) {
                    //                    Icon(
                    //                        imageVector =
                    //                            if (!isExpanded) Icons.Filled.KeyboardArrowDown
                    //                            else Icons.Filled.KeyboardArrowUp,
                    //                        contentDescription = null,
                    //                        tint = MaterialTheme.colorScheme.onBackground
                    //                    )
                    //                }
                    //            }
                }

                //        if (!isInEditMode) {
                //            Box {
                //                DropdownMenu(
                //                    expanded = isLongPressed,
                //                    onDismissRequest = { isLongPressed = false }
                //                ) {
                //                    DropdownMenuItem(
                //                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                //                        text = { Text("Edit") },
                //                        onClick = {
                //                            onCardEdit(todoItemData.todoItemId)
                //                            isLongPressed = false
                //                        }
                //                    )
                //
                //                    DropdownMenuItem(
                //                        leadingIcon = {
                //                            Icon(
                //                                Icons.Filled.Delete,
                //                                contentDescription = null,
                //                                tint = Color.Red
                //                            )
                //                        },
                //                        text = { Text("Delete", color = Color.Red) },
                //                        onClick = {
                //                            isLongPressed = false
                //                            onSelectDelete(todoItemData.todoItemId)
                //                        }
                //                    )
                //                }
                //            }
                //
                //            if (isExpanded && (!deadline.isEmpty() || !reminder.isEmpty())) {
                //                if (!deadline.isEmpty()) {
                //                    Text(
                //                        "Deadline: $deadline",
                //                        modifier.padding(start = 48.dp)
                //                    )
                //                }
                //                if (!reminder.isEmpty()) {
                //                    Text(
                //                        "Remind me at: $reminder",
                //                        modifier.padding(start = 48.dp)
                //                    )
                //                }
                //
                //                Spacer(Modifier.height(8.dp))
                //            }
                //        }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePicker(
    initialSelectedReminderDate: Long,
    initialSelectedReminderTime: Pair<Int, Int>,
    onDismiss: () -> Unit,
    onConfirmSelect: (dateMillisLocal: Long, hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.i(
        TAG, "initial date = $initialSelectedReminderDate, of ${
            formatTimestampToReadableString(
                Instant
                    .ofEpochMilli(initialSelectedReminderDate)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                DateFormatters.fullDate()
            )
        }"
    )

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val datePickerState = rememberDatePickerState(

        //我也不知道这里为啥要加1天，不然就会有bug
        initialSelectedDateMillis = timeAfterOfLocalDate(initialSelectedReminderDate, days = 0),

        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val localDate = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()

                val currentDate = LocalDate.now()

                return ChronoUnit.DAYS.between(currentDate, localDate) >= 0
            }
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialSelectedReminderTime.first,
        initialMinute = initialSelectedReminderTime.second,
        is24Hour = true
    )

    if (selectedTabIndex == 0) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                IconButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        selectedTabIndex = 1
                    }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
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
            }
        ) {
            DatePicker(datePickerState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            dismissButton = {
                IconButton(onClick = { selectedTabIndex = 0 }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            confirmButton = {
                IconButton(onClick = {
                    onConfirmSelect(
                        datePickerState.selectedDateMillis!!,
                        timePickerState.hour,
                        timePickerState.minute
                    )

                    Log.e(
                        TAG, "selected date: ${
                            formatTimestampToReadableString(
                                datePickerState.selectedDateMillis!!,
                                DateFormatters.fullDate()
                            )
                        }"
                    )

                    onDismiss()
                }) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.reminder_picker_title_select_time),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(timePickerState)
                }
            }
        )
    }
}

@Composable
@Preview
fun DatePickerPreview() {
    TodoListTheme {
        FutureDatePicker(
            onDateSelected = {},
            onDismiss = {},
            initialSelectedFutureDate = 0,
        )
    }
}

@Composable
@Preview(
    showSystemUi = false, showBackground = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
fun ReminderPickerPreview() {
    TodoListTheme {

        ReminderTimePicker(
            onDismiss = {},
            initialSelectedReminderDate = -1,
            onConfirmSelect = { P1, P2, P3 -> },
            initialSelectedReminderTime = Pair(0, 0)
        )
    }
}

@Preview
@Composable
fun TodoCardPreview() {
    TodoListTheme {
        Column {
            TodoCard(
                todoItemData = TodoItemData(
                    content = "Test Content",
                    todoListId = 0,
                    deadlineStamp = 0,
                    reminderStamp = 0
                ),
                onDoneStateChange = {},
                onCardEdit = {},
                onSelectInEditMode = { p1, p2 -> },
                onCardLongClick = {},
                getFormattedDeadline = { "" },
                getFormattedReminder = { "" },
                onSelectDelete = {},
                isInEditMode = false,
                isEditSelected = false,
                showListName = true,
                listName = ""
            )

        }

    }
}

@Preview
@Composable
fun TodoScreenPreview() {
    TodoListTheme {
        TodoScreenContent(
            topTitle = "Todo",
            initialSelectedDeadlineDate = -1,
            inputTextValue = "",
            onInputValueChange = {},
            onSelectTodoList = {},
            selectedListName = { "" },
            confirmAddNewItem = { },
            confirmEditItem = {},
            openDraw = {},
            dismissInput = {},
            isBottomBarVisible = false,
            setAddInputState = {},
            setEditInputState = {},
            onSelectDelete = {},
            onDoneStateChange = { _, _ -> },
            focusRequester = remember { FocusRequester() },
            textInputState = UiEnterState.None,
            allTodoListData = listOf(),
            displayType = TodoDisplayType.Today,
            selectedListData = TodoListData(),
            onSelectDeadline = {},
            selectedDeadline = { "" },
            getFormattedDeadline = { "" },
            onPickDateDeadline = {},
            selectedRemindTime = { "" },
            onSelectRemindTime = { p1, p2, p3 -> },
            getFormattedReminder = { "" },
            onSelectRemindString = {},
            checkTimestampInToday = { false },
            initialSelectedReminderUtcDate = 0,
            initialSelectedReminderTime = Pair(0, 0),
            onRenameList = { p1, p2, p3, p4, p5 -> },
            onDeleteList = {},
            onDeleteEditItems = {},
            isScreenEmpty = false,
            onDisplayContentInScreen = {},
            editableScreenState = rememberEditableScreenState(initialEditModeState = false),
            onMoveSelectedItems = { },
            shareSelectedItems = {}
        )
    }
}

@Composable
private fun UpdateSwipeData(onUpdate: () -> Unit) {
    onUpdate()
}