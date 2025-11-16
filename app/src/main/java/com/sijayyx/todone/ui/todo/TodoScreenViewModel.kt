package com.sijayyx.todone.ui.todo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.utils.DEFAULT_LIST_NAME
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.checkTimeInToday
import com.sijayyx.todone.utils.combineDateAndTime
import com.sijayyx.todone.data.TodoItemData
import com.sijayyx.todone.data.TodoListData
import com.sijayyx.todone.data.repository.TodoListRepository
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.UiEnterState
import com.sijayyx.todone.utils.formatDeadlineTimestampToString
import com.sijayyx.todone.utils.formatReminderTimestampToString
import com.sijayyx.todone.utils.formatTimestampToReadableString
import com.sijayyx.todone.utils.getDaysEndTimestampAfterNow
import com.sijayyx.todone.utils.getHourAndMinuteFromTimestamp
import com.sijayyx.todone.utils.getTimesLaterTimestampFromNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoScreenViewModel(
    private val todoListRepository: TodoListRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoScreenUiState())
    val uiState = _uiState.asStateFlow()
    var cachedSelectedList = mutableMapOf<Long, TodoListData>()

    fun canScheduleAlarms() = todoListRepository.canScheduleAlarms()

    fun getSelectedListById(id: Long?): Flow<TodoListData?> {
        val data = todoListRepository.getTodoListDataFlowById(id ?: -1)

        viewModelScope.launch {
            data.collect {
                if (it != null && id != null) {
                    _uiState.value = _uiState.value.copy(
                        selectedListId = id
                    )
                    cachedSelectedList = mutableMapOf(id to it)
                    Log.e(TAG, "TodoScreenViewModel: todo list $id data collected!")
                }
            }
        }

        return data
    }

    val allTodoLists: StateFlow<Map<TodoListData, List<TodoItemData>>> =
        todoListRepository.getTodoListsWithItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = mapOf()
            )

    val allTodoListDatas: StateFlow<List<TodoListData>> =
        todoListRepository.getAllTodoLists().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )

    val allTodoItems: StateFlow<List<TodoItemData>> =
        todoListRepository.getAllTodoItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf()
            )


    private fun getListDataById(
        id: Long,
        fallbackAction: () -> Unit = {}
    ): StateFlow<Map<TodoListData, List<TodoItemData>>> {

        val result = todoListRepository.getSingleListWithItems(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = mapOf()
            )

        if (result.value.isEmpty())
            fallbackAction()

        return result
    }

    suspend fun isListNotExists(id: Long): Boolean {
        return !todoListRepository.isTodoListExists(id)
    }

    fun getTodoItemDatasById(id: Long): StateFlow<List<TodoItemData>> {
        return todoListRepository.getTodoItemsDataByListId(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )
    }

    fun checkTimestampInToday(ts: Long): Boolean {
        return checkTimeInToday(ts)
    }

    fun formatReminderTimestamp(
        context: Context,
        timestamp: Long,
        fallbackString: String
    ): String {
        return formatReminderTimestampToString(context, timestamp, fallbackString)
    }

    fun formatDeadlineTimestamp(
        context: Context,
        timestamp: Long,
        fallbackString: String
    ): String {
        return formatDeadlineTimestampToString(context, timestamp, fallbackString)
    }


    fun updateInputContent(content: String) {
        _uiState.update { newState ->
            newState.copy(itemContent = content)
        }
    }

    fun updateSelectedList(name: String) {
        _uiState.update { newState ->
            newState.copy(
                selectedListName =
                    if (name == NONE_STRING) DEFAULT_LIST_NAME else name
            )
        }
    }

    fun onRenameSelectedList(
        selectedList: TodoListData,
        newName: String,
        color: String,
        icon: String,
        isRenamed: Boolean
    ) {
        viewModelScope.launch {
            todoListRepository.updateTodoListData(
                todoListData = selectedList.copy(
                    listName = if (isRenamed) todoListRepository.checkSameListName(newName) else newName,
                    listColor = color,
                    listIcon = icon
                )
            )
        }

    }

    fun onDeleteSelectedList(selectedList: TodoListData) {
        viewModelScope.launch {
            todoListRepository.deleteTodoListData(selectedList)
        }
    }

    fun updateSelectedDeadline(deadline: String) {
        Log.e(TAG, "update deadline str: $deadline")
        _uiState.update { newState ->
            newState.copy(selectedDeadline = formatDeadlineToTimestamp(deadline))
        }
    }

    fun updateSelectedDeadline(deadline: Long?) {

        Log.e(TAG, "update deadline long: $deadline")
        _uiState.update { newState ->
            newState.copy(selectedDeadline = deadline ?: -1)
        }
    }

    fun updateSelectedRemindTime(dateMillisLocal: Long, hour: Int, min: Int) {
        Log.i(
            TAG,
            "update remind of local time: ${
                formatTimestampToReadableString(
                    combineDateAndTime(dateMillisLocal, hour, min),
                    DateFormatters.fullDate()
                )
            }"
        )
        _uiState.value = _uiState.value.copy(
            selectedReminder =
                combineDateAndTime(
                    dateMillisLocal,
                    hour,
                    min
                ),
        )
    }

    fun updateSelectedRemindTime(remindTimeLocal: String) {
        _uiState.value = _uiState.value.copy(
            selectedReminder = formatReminderToTimestamp(remindTimeLocal)
        )

    }

    fun formatTimeFromTimestamp(timestamp: Long): Pair<Int, Int> {
        Log.i(
            TAG, "format time from timestamp:$timestamp, of date ${
                formatTimestampToReadableString(
                    timestamp,
                    DateFormatters.fullDate()
                )
            } as pair of " +
                    "${getHourAndMinuteFromTimestamp(timestamp).first}, " +
                    "${getHourAndMinuteFromTimestamp(timestamp).second}"
        )
        return if (timestamp > 0) getHourAndMinuteFromTimestamp(timestamp) else Pair(9, 0)
    }

    private fun formatDeadlineToTimestamp(deadline: String): Long {
        val timestamp = when (deadline) {
            TodoDeadline.Today.content -> getDaysEndTimestampAfterNow(0)

            TodoDeadline.Tomorrow.content -> getDaysEndTimestampAfterNow(1)

            TodoDeadline.InTwoDays.content -> getDaysEndTimestampAfterNow(2)

            TodoDeadline.NextWeek.content -> getDaysEndTimestampAfterNow(7)

            else -> -1 //TODO 格式化
        }

        return timestamp
    }

    private fun formatReminderToTimestamp(reminder: String): Long {
        val timestamp = when (reminder) {
            RemindOptions.OneHourLater.content -> getTimesLaterTimestampFromNow(hours = 1)
            RemindOptions.TomorrowMorning.content -> combineDateAndTime(
                date = getTimesLaterTimestampFromNow(days = 1),
                hour = 9,
                min = 0
            )

            else -> -1
        }

        return timestamp
    }

    fun setAddInputState(listId: Long?, displayType: TodoDisplayType = TodoDisplayType.All) {
        viewModelScope.launch {
            resetUiState()

            _uiState.update { newState ->
                newState.copy(
                    textInputState = UiEnterState.Add,

                    selectedListName = if (listId != null) {
                        todoListRepository.getTodoListDataById(listId)?.listName
                            ?: DEFAULT_LIST_NAME
                    } else DEFAULT_LIST_NAME,
                )
            }

            if (displayType == TodoDisplayType.Today)
                updateSelectedDeadline(TodoDeadline.Today.content)
        }
    }

    fun onEndInput() {
        _uiState.update { newState ->
            newState.copy(
                textInputState = UiEnterState.None,
                selectedReminder = -1,
                selectedDeadline = -1,
                selectedWorkerId = NONE_STRING,
                selectedListName = NONE_STRING
            )
        }
    }

    fun setEditInputState() {
        _uiState.update { newState ->
            newState.copy(textInputState = UiEnterState.Edit)
        }
    }

    fun onEditItem(id: Long) {
        viewModelScope.launch {
            _uiState.update { newState ->
                newState.copy(
                    selectedItemId = id
                )
            }

            val itemData = todoListRepository.getTodoItemDataById(id)

            if (itemData != null) {
                _uiState.update { newState ->
                    newState.copy(
                        itemContent = itemData.content,
                        selectedListName =
                            todoListRepository.getTodoListDataById(itemData.todoListId)?.listName
                                ?: DEFAULT_LIST_NAME,
                        selectedDeadline = itemData.deadlineStamp,
                        selectedReminder = itemData.reminderStamp,
                        selectedWorkerId = itemData.reminderWorkerId
                    )
                }
            } else {
                Log.e(TAG, "todo item $id not exist!")
            }
        }
    }

    fun updateItemData() {
        viewModelScope.launch {
            val todoData = todoListRepository.getTodoItemDataById(uiState.value.selectedItemId)
            if (todoData != null) {
                if (!todoListRepository.isTodoListExists(uiState.value.selectedListName))
                    todoListRepository.insertTodoListData(TodoListData(listName = uiState.value.selectedListName))


                todoListRepository.updateTodoItemData(
                    todoItemData = todoData.copy(
                        content = uiState.value.itemContent,
                        todoListId = todoListRepository.getTodoListDataByName(uiState.value.selectedListName)?.id
                            ?: 0,
                        deadlineStamp = _uiState.value.selectedDeadline,
                        reminderStamp = _uiState.value.selectedReminder,
                    )
                )
            } else {
                Log.e(TAG, "todo data is null and cannot be updated")
            }
            resetUiState()
        }
    }


    fun setTodoItemDoneState(isDone: Boolean, id: Long) {
        viewModelScope.launch {
            todoListRepository.updateTodoItemDoneById(id, isDone)
        }
    }

    fun deleteTodoItemData(id: Long) {
        viewModelScope.launch {
            todoListRepository.deleteTodoItemDataById(id)
        }
    }

    fun deleteTodoItemDataList(dataIds: Collection<Long>) {
        viewModelScope.launch {
            dataIds.forEach {
                todoListRepository.deleteTodoItemDataById(it)
            }
        }
    }

    fun moveItemsToList(items: Collection<Long>, targetId: Long) {
        viewModelScope.launch {
            val listId = if (targetId < 0) {
                todoListRepository.getTodoListDataByName(DEFAULT_LIST_NAME).let {
                    it?.id
                        ?: todoListRepository.insertTodoListData(TodoListData(listName = DEFAULT_LIST_NAME))
                }
            } else
                targetId

            items.forEach {
                todoListRepository.updateTodoItemToListById(it, listId)
            }
            Log.e(TAG, "Updated ${items.size} items to list id = $listId")
        }
    }

    fun addNewItem() {
        viewModelScope.launch {
            val selectedListName = uiState.value.selectedListName
            if (!todoListRepository.isTodoListExists(selectedListName))
                todoListRepository.insertTodoListData(TodoListData(listName = selectedListName))

            todoListRepository.insertTodoItemData(
                TodoItemData(
                    todoListId = todoListRepository.getTodoListDataByName(selectedListName)!!.id,
                    content = _uiState.value.itemContent,
                    deadlineStamp = _uiState.value.selectedDeadline,
                    reminderStamp = _uiState.value.selectedReminder,
                    isDone = false
                )
            )
            resetUiState()
        }
    }

    fun resetUiState() {
        _uiState.value = TodoScreenUiState()
    }

    //Fallback: Task
    fun checkSelectedListName(name: String, fallbackString: String): String {
        return if (name == DEFAULT_LIST_NAME || name == NONE_STRING || name.isEmpty())
            fallbackString else name
    }

    //Reminder
    fun checkSelectedRemindTime(context: Context, timestamp: Long, fallbackString: String): String {
        return if (timestamp <= 0) fallbackString
        else formatReminderTimestamp(context, timestamp, fallbackString)
    }

    /**
     * 分享选定的TodoItems
     * 接收Context用来格式化日期，并启动ShareSheet
     * 接受目标ID的集合，以遍历出数据
     *
     * 格式：
     * <图标> <内容> (截止日期)
     *
     * 最后一行的换行符将被去除
     *
     * @param context
     * @param targetList
     */
    fun startSharing(context: Context, targetList: Collection<Long>) {
        viewModelScope.launch {
            val dataMap = allTodoItems.value.associateBy { it.todoItemId }
//            val dataList = listOf<TodoItemData>()
            var resultString: String = EMPTY_STRING
            val _isDone: (Boolean) -> String = {
                if (it) "☑" else "☐"
            }
            val _dueTime: (Long) -> String = {
                if (it > 0)
                    formatTimestampToReadableString(
                        it,
                        DateFormatters.yearWithShortMonthAndDay(context)
                    )
                else EMPTY_STRING
            }

            targetList.forEach { id ->
                dataMap[id]?.let { data ->
                    // ☑ content appendData \n
                    resultString += "${_isDone(data.isDone)} ${data.content} ${_dueTime(data.deadlineStamp)}\n"
//                    dataList.plus(data)
                }
            }

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resultString.trimIndent())
                type = "text/plain"
            }

            context.startActivity(Intent.createChooser(shareIntent, null))

            Log.e(TAG, "Sharing data, total ${dataMap.size} elements:\n$resultString")
        }
    }
}

data class TodoScreenUiState(
    val textInputState: UiEnterState = UiEnterState.None,
    val itemContent: String = EMPTY_STRING,
    val selectedListName: String = DEFAULT_LIST_NAME,
    val selectedListId: Long = -1,
    val selectedDeadline: Long = -1,
    val selectedReminder: Long = -1,
    val selectedItemId: Long = -1,
    val selectedWorkerId: String = NONE_STRING
)

//屏幕显示什么东西
enum class TodoDisplayType {
    All, //显示所有列表，包括today，自定义，done
    SpecifiedList, // 仅显示自定义列表和done
    Today // 仅显示today和done
}

enum class TodoDeadline(val content: String) {
    None(NONE_STRING),
    Today("Today"),
    Tomorrow("Tomorrow"),
    InTwoDays("In two days"),
    NextWeek("Next week")
}

enum class RemindOptions(val content: String) {
    None(NONE_STRING),
    OneHourLater("One hour later"),
    TomorrowMorning("Tomorrow morning (09:00)")
}

