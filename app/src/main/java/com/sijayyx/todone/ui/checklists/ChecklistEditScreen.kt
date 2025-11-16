package com.sijayyx.todone.ui.checklists

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.GlobalViewModel
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.composables.ColorSelectorDialog
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.ui.composables.BasicIconTextButton
import com.sijayyx.todone.ui.composables.ColorOption
import com.sijayyx.todone.ui.composables.DeleteConfirmDialog
import com.sijayyx.todone.ui.composables.DropdownIconTextButton
import com.sijayyx.todone.ui.composables.IconTextRow
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistWithDatas
import com.sijayyx.todone.ui.composables.InputFieldBottomBar
import com.sijayyx.todone.ui.composables.RepeatEditDialog
import com.sijayyx.todone.ui.todo.ReminderTimePicker
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.UiEnterState
import com.sijayyx.todone.utils.formatRepeatPeriodToString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistEditScreen(
    setGestureEnable: (Boolean) -> Unit,
    checklistId: Long?,
    checklistEditEnterState: UiEnterState,
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
    globalViewModel: GlobalViewModel,
    viewModel: ChecklistEditScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val uiState = viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val cachedSelectedListWithItem = viewModel.cachedSelectedList
    val selectedListWithItem by produceState<ChecklistWithDatas?>(
        initialValue = null,
        key1 = uiState.value.selectedChecklistId
    ) {
        viewModel.getChecklistWithData(uiState.value.selectedChecklistId)
            .collect {
                value = it
            }
    }
    val displayChecklistWithItems =
        remember(selectedListWithItem, uiState.value.selectedChecklistId) {
            derivedStateOf {
                selectedListWithItem
                    ?: cachedSelectedListWithItem[uiState.value.selectedChecklistId]
                    ?: ChecklistWithDatas(
                        ChecklistData(),
                        listOf()
                    )
            }
        }.value

    val isShowingInputBar =
        remember(uiState.value.itemInputState) { uiState.value.itemInputState != ChecklistItemInputState.None }
    var isShowingTitleDialog by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val dismissInput: () -> Unit = remember {
        {
            viewModel.onEndInput()
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // 当输入框显示时自动获取焦点和打开键盘
    LaunchedEffect(checklistEditEnterState) {
        Log.e(TAG, "editing checklistId = $checklistId")

        setGestureEnable(false)

        if (checklistEditEnterState == UiEnterState.Edit
            && checklistId != null && checklistId >= 0
        ) {
            viewModel.getChecklistDataAndItemsById(checklistId)
        } else if (checklistEditEnterState == UiEnterState.Add) {
            viewModel.initializeChecklistData()
            isShowingTitleDialog = true
        } else {
            onNavigateUp()
        }

    }

    LaunchedEffect(isShowingInputBar) {
        if (isShowingInputBar) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 处理返回键
    BackHandler(enabled = isShowingInputBar) {
        dismissInput()
    }

    ChecklistEditScreenContent(
        isShowingTitleDialog = isShowingTitleDialog,
        onOpenTitleDialog = { isShowingTitleDialog = true },
        onDismissTitleDialog = {
            isShowingTitleDialog = false
        },
        onCancelAdd = if (checklistEditEnterState == UiEnterState.Add
            && displayChecklistWithItems.checklist.isHide
        ) { //在添加列表时关闭对话框，将返回上个页面
            {
                viewModel.deleteChecklistData()
                onNavigateUp()
            }
        } else
            null,

        onShareChecklist = { data, items ->
            viewModel.shareChecklist(context, data, items)
        },

        checklistWithDatas = displayChecklistWithItems,
        enterState = checklistEditEnterState,
        checklistTitle = uiState.value.checklistTitle,
        selectRepeatPeriod = { num, period -> viewModel.updateRepeatPeriod(num, period) },
        onDeleteItem = { viewModel.deleteChecklistItem(it) },
        onCheckChanged = { id, isDone -> viewModel.updateItemDoneState(id, isDone) },
        onEditClick = { viewModel.onEditListItem(it) },

        originalColor = displayChecklistWithItems.checklist.color,

        //重命名框内的显示
        getFormattedRemindString = { date, hour, min ->
            viewModel.formatReminderTimestampToString(
                context, date, hour, min, context.getString(R.string.reminder)
            )
        },
        getFormattedRepeatString = { num, period ->
            formatRepeatPeriodToString(
                context,
                num,
                period,
                context.getString(R.string.title_repeat)
            )
        },
        getFormattedColorString = {
            viewModel.checkColorString(
                it,
                context.getString(R.string.color),
                context
            )
        },

        //编辑页面的显示
        getFormattedRemind = {
            viewModel.formatReminderTimestampToString(
                context,
                it,
                fallbackString = EMPTY_STRING
            )
        },
        getFormattedRepeat = { num, period ->
            formatRepeatPeriodToString(
                context,
                num,
                period,
                fallbackString = EMPTY_STRING
            )
        },

        selectRemindTime = { date, hour, min -> viewModel.updateRemindTime(date, hour, min) },
        selectColor = { viewModel.updateSelectedColor(it) },
        onSelectRemindString = { viewModel.updateRemindTime(it) },

        getRemindTime = { viewModel.formatTimestampToTimePair(it) },

        onConfirmList = {
            viewModel.confirmListAdding()
            onNavigateUp()
        },
        onConfirmItem = {
            if (uiState.value.itemInputState == ChecklistItemInputState.Edit) {
                viewModel.updateItemData()
            } else if (uiState.value.itemInputState == ChecklistItemInputState.Add) {
                viewModel.addChecklistItem()
            }

            viewModel.confirmNewListHideState()
        },
        onDeleteList = {
            coroutineScope.launch {
                viewModel.setListHide(selectedListWithItem?.checklist)
                onNavigateUp()
            }
        },
        onAddNewItem = { viewModel.onSetAddState() },
        onRefreshList = { viewModel.refreshItemsDoneState() },
        dismissInput = dismissInput,
        resetUiState = { viewModel.resetUiState() },
        onNavigateUp = {
            coroutineScope.launch {
                if (!uiState.value.isNewListConfirmed)
                    viewModel.setListHide(selectedListWithItem?.checklist)
                onNavigateUp()
            }
        },
        cancelAddNewItem = { viewModel.onEndInput() },
        focusManager = focusManager,
        keyboardController = keyboardController,
        isShowingInputBar = isShowingInputBar,
        newItemContent = uiState.value.itemContent,
        inputNewItemContent = { viewModel.inputNewItemContent(it) },
        onConfirmListRename = {
            viewModel.updateChecklistData(it)
            viewModel.confirmNewListHideState()
        },
        focusRequester = focusRequester,
        isDarkTheme = { globalViewModel.isDarkTheme(context) },
        isNewListConfirmed = { uiState.value.isNewListConfirmed }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChecklistEditScreenContent(
    isShowingTitleDialog: Boolean,
    onDismissTitleDialog: () -> Unit,
    onOpenTitleDialog: () -> Unit,
    onCancelAdd: (() -> Unit)?,

    onShareChecklist: (ChecklistData, List<ChecklistItemData>) -> Unit,

    checklistWithDatas: ChecklistWithDatas,
    enterState: UiEnterState,
    onDeleteItem: (Long) -> Unit,
    onCheckChanged: (Long, Boolean) -> Unit,
    onEditClick: (Long) -> Unit,
    checklistTitle: String,
    selectRepeatPeriod: (Int, String) -> Unit,
    selectColor: (String) -> Unit,

    getFormattedRepeat: (Int, String) -> String,
    getFormattedRemind: (Long) -> String,

    getFormattedRepeatString: (Int, String) -> String,
    getFormattedRemindString: (Long, Int, Int) -> String,
    getFormattedColorString: (String) -> String,

    getRemindTime: (Long) -> Pair<Int, Int>,

    originalColor: String,

    selectRemindTime: (Long, Int, Int) -> Unit,
    onSelectRemindString: (String) -> Unit,

    onConfirmList: () -> Unit,
    onConfirmItem: () -> Unit,
    onDeleteList: () -> Unit,
    onAddNewItem: () -> Unit,
    onRefreshList: () -> Unit,
    dismissInput: () -> Unit,
    resetUiState: () -> Unit,
    onNavigateUp: () -> Unit,
    cancelAddNewItem: () -> Unit,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    isShowingInputBar: Boolean,
    newItemContent: String,
    inputNewItemContent: (String) -> Unit,
    onConfirmListRename: (String) -> Unit,
    focusRequester: FocusRequester,

    isNewListConfirmed: () -> Boolean,
    isDarkTheme: () -> Boolean,

    modifier: Modifier = Modifier
) {
    var isShowingDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val checklist =
        remember(checklistWithDatas.checklist) { derivedStateOf { checklistWithDatas.checklist } }.value
    val checklistItems =
        remember(checklistWithDatas.checklistItems) { derivedStateOf { checklistWithDatas.checklistItems } }.value

    val remind =
        remember(checklistWithDatas.checklist.remindTimestamp) {
            derivedStateOf {
                getFormattedRemind(
                    checklistWithDatas.checklist.remindTimestamp
                )
            }
        }.value

    val repeat = remember(
        checklistWithDatas.checklist.repeatNum,
        checklistWithDatas.checklist.repeatPeriod
    ) {
        derivedStateOf {
            getFormattedRepeat(
                checklistWithDatas.checklist.repeatNum,
                checklistWithDatas.checklist.repeatPeriod
            )
        }
    }.value


    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isShowingInputBar,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column {
                    FloatingActionButton(
                        modifier = Modifier
                            .padding(8.dp),
                        elevation = FloatingActionButtonDefaults.elevation(2.dp),
                        onClick = onAddNewItem
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }

                    FloatingActionButton(
                        modifier = Modifier
                            .padding(8.dp),
                        elevation = FloatingActionButtonDefaults.elevation(2.dp),
                        onClick = onRefreshList
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                    }
                }
            }
        },

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            if (enterState == UiEnterState.Add) stringResource(R.string.title_add_checklist)
                            else stringResource(R.string.title_edit_checklist),
                        fontSize = 26.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateUp()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },

                actions = {
                    IconButton(
                        //分享
                        enabled = (checklist.checklistName.isNotEmpty() || checklistItems.isNotEmpty()),
                        onClick = {
                            onShareChecklist(
                                checklist,
                                checklistItems
                            )
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                    }
                    IconButton(
                        enabled = (checklist.checklistName.isNotEmpty() || checklistItems.isNotEmpty()),
                        onClick = { isShowingDeleteDialog = true }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }
                }
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        //取消添加
                        if (isShowingInputBar) {
                            cancelAddNewItem()
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .animateContentSize()
                    ) {
                        Text(
                            text = checklist.checklistName,
                            fontSize = 22.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier
                                .clickable(onClick = onOpenTitleDialog)
                        )

                        FlowRow(modifier = Modifier.animateContentSize()) {
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


                    IconButton(
                        onClick = onOpenTitleDialog,
                    ) {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = null,
                            tint = ColorOption.formatStringToColor(checklist.color, false)
                                ?: MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Crossfade(
                    targetState = checklistItems.isEmpty()
                ) { it ->
                    if (it) {
                        Text(
                            text = stringResource(R.string.list_is_empty),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(alignment = Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(checklistItems, key = { data -> data.checklistItemId }) { data ->
                                ChecklistItemElement(
                                    checklistItemData = data,
                                    onCheckChanged = onCheckChanged,
                                    onDelete = onDeleteItem,
                                    onEditClick = onEditClick,
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }

            }
        }
    }


    if (isShowingDeleteDialog) {
        DeleteConfirmDialog(
            deleteInfo = stringResource(
                R.string.checklist_name_will_be_deleted_forever,
                checklist.checklistName
            ),
            onDismiss = { isShowingDeleteDialog = false },
            onConfirm = onDeleteList
        )
    }

    if (isShowingTitleDialog) {
        Log.e(TAG, "ChecklistScreen: Title dialog showing!")
        EditChecklistTitleDialog(
            getIsDarkTheme = isDarkTheme,
            originalColor = checklist.color,
            originalRepeatNum = checklist.repeatNum,
            originalRepeatPeriod = checklist.repeatPeriod,
            originalRemindDate = checklist.remindTimestamp,
            originalRemindTime = getRemindTime(checklist.remindTimestamp),
            originalTitle = checklistTitle,
            getFormattedRemindString = getFormattedRemindString,
            getFormattedRepeatString = getFormattedRepeatString,
            getFormattedColorString = getFormattedColorString,
            selectRepeatPeriod = selectRepeatPeriod,
            onSelectRemindString = onSelectRemindString,
            onCancelAdd = onCancelAdd,
            onDismiss = {
                onDismissTitleDialog()
            },
            onConfirmInput = {
                onConfirmListRename(it)
            },
            selectRemindTime = selectRemindTime,
            selectColor = selectColor
        )
    }

    AnimatedVisibility(
        visible = isShowingInputBar,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        InputFieldBottomBar(
            textValue = newItemContent,
            onValueChange = { inputNewItemContent(it) },
            focusRequester = focusRequester,
            inputFieldLabel = stringResource(R.string.input_bar_label_checklist_item),
            leadingAction = {
                IconButton(onClick = dismissInput) {
                    Icon(
                        Icons.Outlined.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            trailingAction = {
                IconButton(
                    enabled = it,
                    onClick = onConfirmItem
                ) {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            bottomActions = {},
            onDismiss = dismissInput
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChecklistTitleDialog(
    getIsDarkTheme: () -> Boolean,
    getFormattedRepeatString: (Int, String) -> String,
    getFormattedRemindString: (Long, Int, Int) -> String,
    getFormattedColorString: (String) -> String,
    originalRepeatPeriod: String,
    originalRepeatNum: Int,
    originalRemindDate: Long,
    originalRemindTime: Pair<Int, Int>,
    originalColor: String,
    selectRepeatPeriod: (Int, String) -> Unit,
    selectRemindTime: (Long, Int, Int) -> Unit,
    selectColor: (String) -> Unit,
    onSelectRemindString: (String) -> Unit,
    originalTitle: String,
    onConfirmInput: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onCancelAdd: (() -> Unit)? = null,
) {
    var inputValue by rememberSaveable { mutableStateOf(originalTitle) }
    var isShowingRepeatDialog by rememberSaveable { mutableStateOf(false) }
    var isShowingRemindDatePicker by rememberSaveable { mutableStateOf(false) }
    var isShowingColorPicker by rememberSaveable { mutableStateOf(false) }

    var selectedRemindTime by rememberSaveable(stateSaver = DateAndTimeSaver) {
        mutableStateOf(
            DateAndTime(originalRemindDate, originalRemindTime.first, originalRemindTime.second)
        )
    }
    var selectedRepeatPeriod by rememberSaveable {
        mutableStateOf(
            Pair(
                originalRepeatNum,
                originalRepeatPeriod
            )
        )
    }
    var selectedColor by rememberSaveable { mutableStateOf(originalColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,    // 按返回键是否关闭
            dismissOnClickOutside = false // 关键：禁止点击外部关闭
        ),
        confirmButton = {
            IconButton(
                enabled = inputValue.isNotEmpty(),
                onClick = {
                    selectRemindTime(
                        selectedRemindTime.date,
                        selectedRemindTime.hour,
                        selectedRemindTime.min
                    )
                    selectColor(selectedColor)
                    selectRepeatPeriod(selectedRepeatPeriod.first, selectedRepeatPeriod.second)
                    onConfirmInput(inputValue)
                    onDismiss()
                }
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        dismissButton = {
            IconButton(
                onClick = {
                    onCancelAdd?.invoke()
                    onDismiss()
                }
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = null
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    placeholder = { Text(stringResource(R.string.enter_title_here)) },
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.animateContentSize()
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BasicIconTextButton(
                        label = getFormattedColorString(selectedColor), //不需要格式化方法？
                        icon = Icons.Filled.ColorLens,
                        onClick = { isShowingColorPicker = true },
                        modifier = Modifier.animateContentSize()
                    )

                    DropdownIconTextButton(
                        label = getFormattedRepeatString(
                            selectedRepeatPeriod.first,
                            selectedRepeatPeriod.second
                        ),
                        icon = Icons.Filled.Repeat,
                        menuItemList = mapOf(
                            stringResource(R.string.every_day_singular) to RepeatPeriod.Day.content,
                            stringResource(R.string.every_week_singular) to RepeatPeriod.Week.content,
                            stringResource(R.string.every_month_singular) to RepeatPeriod.Month.content,
                            stringResource(R.string.every_year_singular) to RepeatPeriod.Year.content,
                        ),
                        onMenuItemSelectString = {
                            selectedRepeatPeriod = Pair(1, it)
                        },
                        menuItems = {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_custom)) },
                                onClick = {
                                    it()
                                    isShowingRepeatDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.none)) },
                                onClick = {
                                    it()
                                    selectedRepeatPeriod = Pair(0, NONE_STRING)
                                }
                            )
                        },
                        modifier = Modifier.animateContentSize()
                    )
                    DropdownIconTextButton(
                        label = getFormattedRemindString(
                            selectedRemindTime.date,
                            selectedRemindTime.hour,
                            selectedRemindTime.min
                        ),
                        icon = Icons.Filled.AddAlert,
                        menuItems = {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_custom)) },
                                onClick = {
                                    it()
                                    isShowingRemindDatePicker = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.none)) },
                                onClick = {
                                    it()
                                    selectedRemindTime = DateAndTime(-1, 0, 0)
                                }
                            )
                        }, modifier = Modifier.animateContentSize()
                    )
                }
            }


        },
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        title = { Text(stringResource(R.string.title_edit_checklist)) }
    )

    if (isShowingRepeatDialog) {
        RepeatEditDialog(
            initialRepeatNum = originalRepeatNum,
            initialRepeatPeriod = originalRepeatPeriod,
            onConfirmInput = { num, period -> selectedRepeatPeriod = Pair(num, period) },
            onDismiss = { isShowingRepeatDialog = false })
    }

    if (isShowingRemindDatePicker) {
        ReminderTimePicker(
            initialSelectedReminderDate = if (originalRemindDate < 0) System.currentTimeMillis() else originalRemindDate,
            initialSelectedReminderTime = originalRemindTime,
            onDismiss = { isShowingRemindDatePicker = false },
            onConfirmSelect = { date, hour, min ->
                selectedRemindTime = DateAndTime(date, hour, min)
            }
        )
    }

    if (isShowingColorPicker) {
        ColorSelectorDialog(
            initialValue = originalColor,
            onDismiss = { isShowingColorPicker = false },
            onConfirm = { selectedColor = it },
            isDarkTheme = getIsDarkTheme
        )
    }
}

@Preview
@Composable
fun EditChecklistTitleDialogPreview() {
    TodoListTheme {
        EditChecklistTitleDialog(
            originalTitle = "Checklist",
            onConfirmInput = {},
            onDismiss = {},
            getFormattedRepeatString = { p1, p2 -> "" },
            selectRepeatPeriod = { p1, p2 -> },
            originalRepeatPeriod = "None",
            originalRepeatNum = 0,
            getFormattedRemindString = { p1, p2, p3 -> "" },
            originalRemindDate = 0,
            selectRemindTime = { p1, p2, p3 -> },
            onSelectRemindString = {},
            originalRemindTime = Pair(0, 0),
            originalColor = "",
            selectColor = {},
            getFormattedColorString = { "" },
            getIsDarkTheme = { false }
        )
    }
}

@Composable
fun ChecklistItemElement(
    checklistItemData: ChecklistItemData,
    onCheckChanged: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val decoration = remember(checklistItemData.isDone) {
        if (checklistItemData.isDone)
            TextDecoration.LineThrough
        else
            TextDecoration.None
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checklistItemData.isDone,
                onCheckedChange = { onCheckChanged(checklistItemData.checklistItemId, it) },
            )
            Text(
                text = checklistItemData.content,
                fontSize = 20.sp,
                textDecoration = decoration,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(onClick = { onEditClick(checklistItemData.checklistItemId) })
                    .weight(1f)
            )
            IconButton(onClick = { onDelete(checklistItemData.checklistItemId) }) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
            }
        }

        HorizontalDivider()
    }
}

@Preview
@Composable
fun RepeatEditDialogPreview() {
    TodoListTheme {
        RepeatEditDialog(
            onConfirmInput = { p1, p2 -> }, onDismiss = {})
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChecklistEditScreenPreview() {
    TodoListTheme {
        ChecklistEditScreenContent(
            checklistTitle = "",
            dismissInput = {},
            resetUiState = {},
            onNavigateUp = {},
            cancelAddNewItem = {},
            focusManager = LocalFocusManager.current,
            keyboardController = LocalSoftwareKeyboardController.current,
            isShowingInputBar = false,
            newItemContent = "",
            inputNewItemContent = {},
            onConfirmListRename = {},
            focusRequester = remember { FocusRequester() },
            onConfirmList = {},
            onDeleteList = {},
            onAddNewItem = {},
            onRefreshList = {},
            onDeleteItem = {},
            onCheckChanged = { ar1, ar2 -> },
            onEditClick = {},
            onConfirmItem = {},
            enterState = UiEnterState.None,
            selectRepeatPeriod = { p1, p2 -> },
            selectRemindTime = { p1, p2, p3 -> },
            onSelectRemindString = {},
            getFormattedRepeat = { p1, p2 -> "" },
            getFormattedRemind = { "" },
            getFormattedRepeatString = { p1, p2 -> "" },
            getFormattedRemindString = { p1, p2, p3 -> "" },
            originalColor = "",
            selectColor = {},
            getFormattedColorString = { "" },
            checklistWithDatas = ChecklistWithDatas(ChecklistData(), listOf()),
            getRemindTime = { Pair(0, 0) },
            isShowingTitleDialog = false,
            onDismissTitleDialog = {},
            onOpenTitleDialog = {},
            onCancelAdd = {},
            onShareChecklist = { p1, p2 -> },
            isDarkTheme = { false },
            isNewListConfirmed = { false }
        )
    }
}