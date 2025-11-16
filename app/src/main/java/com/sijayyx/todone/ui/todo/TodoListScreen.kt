package com.sijayyx.todone.ui.todo

import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 或许是SaveState的ID冲突，如果使用TodoScreen，在导航时恢复界面状态就会造成冲突，
 * 把TodoList的界面恢复到了Todo的界面，
 * 所以复制一份TodoScreen，改个名字就行了（）
 *
 * @param drawerState
 * @param closeDraw
 * @param listIdToShow
 * @param displayType
 * @param openDraw
 * @param navController
 * @param permissionViewModel
 * @param modifier
 * @param viewModel
 */
//
//@OptIn(ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
//@Composable
//fun TodoListScreen(
//    drawerState: DrawerState,
//    closeDraw: () -> Unit,
//    listIdToShow: Long?,
//    displayType: TodoDisplayType,
//    openDraw: () -> Unit,
//    navController: NavController,
//    permissionViewModel: GlobalViewModel,
//    modifier: Modifier = Modifier,
//    viewModel: TodoScreenViewModel = viewModel(
//        key = "todo_${listIdToShow}_$displayType",
//        factory = AppViewModelProvider.factory
//    ),
//) {
//    Log.e(TAG, "TodoScreen: composition! list id = $listIdToShow, $displayType")
//    val key = "todo_${listIdToShow}_$displayType"
//
//    val context = LocalContext.current
//
//    val isAllowNotification = permissionViewModel.isAllowNotification.collectAsState().value
//
//    val coroutineScope = rememberCoroutineScope()
//    val uiState = viewModel.uiState.collectAsState()
//
//    val allTodoLists = viewModel.allTodoLists.collectAsState()
//    val allTodoItems = viewModel.allTodoItems.collectAsState()
//    val allTodoListData = viewModel.allTodoListDatas.collectAsState() //所有已经添加的Todo列表
//
//    //选定的列表
//    val cachedSelectedList = viewModel.cachedSelectedList
//    val selectedList by produceState<TodoListData?>(
//        initialValue = null,
//        key1 = listIdToShow,
//        key2 = key
//    ) {
//        viewModel.getSelectedListById(listIdToShow).collect {
//            value = it
//        }
//    }
//    val displaySelectedList =
//        remember(selectedList) { selectedList ?: cachedSelectedList[uiState.value.selectedListId] }
//
//    val isBottomBarVisible =
//        remember(uiState.value.textInputState) { uiState.value.textInputState != UiEnterState.None }
//    var isScreenEmpty by rememberSaveable(key = key) { mutableStateOf(true) }
//
//
//    val focusRequester = remember { FocusRequester() }
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    var isInEditMode by rememberSaveable(key = key) { mutableStateOf(false) }
//    var selectedEditItems by remember { mutableStateOf(emptySet<Long>()) }
//
//    //全选时选定的列表
//    val targetItems = remember(allTodoItems.value, displayType) {
//        derivedStateOf {
//            allTodoItems.value.filter {
//                when (displayType) {
//                    TodoDisplayType.SpecifiedList -> {
//                        it.todoListId == listIdToShow
//                    }
//
//                    TodoDisplayType.Today -> {
//                        checkTimeInToday(it.deadlineStamp)
//                    }
//
//                    else -> true
//                }
//            }
//        }
//    }.value
//
//    val dismissInput: () -> Unit = {
////        viewModel.resetUiState()
//        focusManager.clearFocus()
//        keyboardController?.hide()
//        viewModel.onEndInput()
//    }
//
//    val exitEditMode: () -> Unit = {
//        isInEditMode = false
////        isAllSelected = false
//        selectedEditItems = emptySet()
//    }
//
//    val initialSelectedReminderTime =
//        remember(uiState.value.selectedReminder) { viewModel.formatTimeFromTimestamp(uiState.value.selectedReminder) }
//    val selectedRemindTime =
//        remember(uiState.value.selectedReminder) {
//            viewModel.checkSelectedRemindTime(
//                context,
//                uiState.value.selectedReminder,
//                context.getString(R.string.reminder)
//            )
//        }
//    val initialSelectedReminderUtcDate = remember(uiState.value.selectedReminder) {
//        if (uiState.value.selectedReminder > 0) uiState.value.selectedReminder
//        else System.currentTimeMillis()
//    }
//    val initialSelectedDeadlineDate = remember(uiState.value.selectedDeadline) {
//        if (uiState.value.selectedDeadline > 0)
//            uiState.value.selectedDeadline
//        else
//            System.currentTimeMillis()
//    }
//
//
//    // 当输入框显示时自动获取焦点和打开键盘
//    LaunchedEffect(isBottomBarVisible) {
//        if (isBottomBarVisible) {
//            focusRequester.requestFocus()
//            keyboardController?.show()
//        }
//    }
//
//    // 处理返回键
//    BackHandler(enabled = isBottomBarVisible) {
//        dismissInput()
//    }
//
//    BackHandler(enabled = isInEditMode) {
//        exitEditMode()
//    }
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        gesturesEnabled = !isBottomBarVisible && !isInEditMode,
//        drawerContent = {
//            DrawerScreen(
//                closeDraw = closeDraw,
//                navController = navController,
//            )
//        }
//    ) {
//        TodoScreenContent(
//            isInEditMode = isInEditMode,
//            isAllSelected = selectedEditItems.count() == targetItems.count(),
//            getSelectedState = {
//                Log.e(
//                    TAG,
//                    "Check select item $it, isSelect = ${selectedEditItems.contains(it)}"
//                )
//                selectedEditItems.contains(it)
//            },
//            onEnterEditMode = {
//                selectedEditItems = selectedEditItems + it
//                dismissInput()
//                isInEditMode = true
//            },
//            onCancelEditMode = exitEditMode,
//            onSelectInEditMode = { id, isSelected ->
//                selectedEditItems =
//                    if (isSelected) selectedEditItems - id
//                    else selectedEditItems + id
//                Log.e(
//                    TAG,
//                    "Edit item $id, isSelected = $isSelected, set count = ${selectedEditItems.count()}"
//                )
//            },
//            onEditSelectAll = {
//                coroutineScope.launch {
//                    selectedEditItems = selectedEditItems + targetItems.map { it.todoItemId }
////                    isAllSelected = true
//                }
//
//            },
//            onCancelSelectAll = {
//                selectedEditItems = emptySet()
////                isAllSelected = false
//            },
//            onDeleteEditItems = {
//                viewModel.deleteTodoItemDataList(selectedEditItems)
//                exitEditMode()
//            },
//            selectedEditItemsCount = selectedEditItems.count(),
//
//            isScreenEmpty = isScreenEmpty,
//            onDisplayContentInScreen = { isScreenEmpty = false },
//
//            topTitle =
//                if (displayType == TodoDisplayType.SpecifiedList) {
//                    if (displaySelectedList != null) {
//                        "${
//                            if (displaySelectedList.listIcon != NONE_STRING) displaySelectedList.listIcon
//                            else EMPTY_STRING
//                        } ${displaySelectedList.listName}"
//                    } else stringResource(R.string.title_todo)
//                } else if (displayType == TodoDisplayType.Today) {
//                    stringResource(R.string.title_today)
//                } else stringResource(R.string.title_todo),
//
//            selectedListData = displaySelectedList,
//
//            displayType =
//                if (listIdToShow != null) displayType
//                else TodoDisplayType.All,
//
//            getFormattedDeadline = {
//                viewModel.formatDeadlineTimestamp(
//                    context,
//                    it,
//                    fallbackString = EMPTY_STRING
//                )
//            },
//
//            onSelectRemindTime = { date, hour, min ->
//                viewModel.updateSelectedRemindTime(
//                    date,
//                    hour,
//                    min
//                )
//            },
//
//            onRenameList = { listData, newName, color, icon, isRenamed ->
//                viewModel.onRenameSelectedList(
//                    selectedList = listData,
//                    newName = newName,
//                    color = color,
//                    icon = icon,
//                    isRenamed = isRenamed
//                )
//            },
//            onDeleteList = {
//                viewModel.onDeleteSelectedList(it)
//                //由于这个按钮一定是在显示当前列表时删除，所以退回导航栈
//                navController.popBackStack(ScreenNavDestination.Todo.createRoute(), false)
//            },
//
//            onSelectRemindString = { viewModel.updateSelectedRemindTime(it) },
//            selectedRemindTime = selectedRemindTime,
//            initialSelectedReminderTime = initialSelectedReminderTime,//viewModel.formatTimeFromTimestamp(uiState.value.selectedReminder),
//            initialSelectedReminderUtcDate = initialSelectedReminderUtcDate,
//            initialSelectedDeadlineDate = initialSelectedDeadlineDate,
//
//            getFormattedReminder = {
//                viewModel.formatReminderTimestamp(
//                    context,
//                    it,
//                    EMPTY_STRING
//                )
//            },
//
//            checkTimestampInToday = { viewModel.checkTimestampInToday(it) },
//
//            inputTextValue = uiState.value.itemContent,
//            onInputValueChange = { viewModel.updateInputContent(it) },
//            onSelectDeadline = { viewModel.updateSelectedDeadline(it) },
//            onPickDateDeadline = { viewModel.updateSelectedDeadline(it) },
//            selectedDeadline = viewModel.formatDeadlineTimestamp(
//                context,
//                uiState.value.selectedDeadline,
//                stringResource(R.string.deadline)
//            ),
//
//            onSelectTodoList = { viewModel.updateSelectedList(it) },
//            selectedListName = viewModel.checkSelectedListName(
//                uiState.value.selectedListName,
//                stringResource(R.string.task)
//            ),
//            confirmAddNewItem = { viewModel.addNewItem() },
//            confirmEditItem = { viewModel.updateItemData() },
//            openDraw = openDraw,
//            dismissInput = dismissInput,
//            isBottomBarVisible = isBottomBarVisible,
//            setAddInputState = {
//                //在应用内允许通知但没有权限时弹窗提醒
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
//                    && (!viewModel.canScheduleAlarms())
//                    && isAllowNotification
//                ) {
//                    permissionViewModel.requestPermission(
//                        Manifest.permission.SCHEDULE_EXACT_ALARM,
//                        onCloseDialog = { viewModel.setAddInputState(listIdToShow, displayType) }
//                    )
//                } else {
//                    viewModel.setAddInputState(listIdToShow, displayType)
//                }
//
//            },
//            setEditInputState = {
//                viewModel.setEditInputState()
//                viewModel.onEditItem(it)
//            },
//            onSelectDelete = { viewModel.deleteTodoItemData(it) },
//            onDoneStateChange = { id, isDone -> viewModel.setTodoItemDoneState(isDone, id) },
//            focusRequester = focusRequester,
//            textInputState = uiState.value.textInputState,
//
//            allTodos = allTodoLists.value,
//            allTodoItems = allTodoItems.value,
//            allTodoListData = allTodoListData.value,
//            key = key
//        )
//    }
//}