package com.sijayyx.todone.ui.checklists

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.R
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.data.ChecklistWithDatas
import com.sijayyx.todone.data.repository.ChecklistRepository
import com.sijayyx.todone.ui.composables.ColorOptions
import com.sijayyx.todone.ui.todo.RemindOptions
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.combineDateAndTime
import com.sijayyx.todone.utils.formatTimestampToReadableString
import com.sijayyx.todone.utils.getHourAndMinuteFromTimestamp
import com.sijayyx.todone.utils.getTimesLaterTimestampFromNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChecklistEditScreenViewModel(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChecklistEditUiState())
    val uiState = _uiState.asStateFlow()

    var cachedSelectedList = mutableMapOf<Long, ChecklistWithDatas>()

    fun updateRepeatPeriod(num: Int, period: String) {
        _uiState.value = _uiState.value.copy(
            selectedRepeatPeriod = period,
            selectedRepeatNum = num
        )

        Log.i(TAG, "update repeat: num = $num, period = $period")
    }

    fun updateRemindTime(dateMillisLocal: Long, hour: Int, min: Int) {
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
            selectedRemindTime =
                combineDateAndTime(
                    dateMillisLocal,
                    hour,
                    min
                ),
        )
    }


    fun updateRemindTime(remindTime: String) {
        _uiState.value = _uiState.value.copy(
            selectedRemindTime = formatRemindTimeToTimestamp(remindTime)
        )
    }

    fun updateSelectedColor(colorString: String) {
        _uiState.value = _uiState.value.copy(
            selectedColor = colorString
        )
    }

    private fun formatRemindTimeToTimestamp(remindTime: String): Long {
        val timestamp = when (remindTime) {
            RemindOptions.OneHourLater.content -> getTimesLaterTimestampFromNow(hours = 1)
            RemindOptions.TomorrowMorning.content -> combineDateAndTime(
                date = getTimesLaterTimestampFromNow(1),
                hour = 9,
                min = 0
            )

            else -> -1
        }

        return timestamp
    }

    fun checkColorString(string: String, fallbackString: String, context: Context): String {

        val localizedColorName = when (string) {
            ColorOptions.Red.colorName -> context.getString(R.string.color_red)
            ColorOptions.Green.colorName -> context.getString(R.string.color_green)
            ColorOptions.Blue.colorName -> context.getString(R.string.color_blue)
            ColorOptions.Yellow.colorName -> context.getString(R.string.color_yellow)
            ColorOptions.Purple.colorName -> context.getString(R.string.color_purple)
            ColorOptions.Pink.colorName -> context.getString(R.string.color_pink)
            else -> fallbackString

        }

        return localizedColorName
    }

    fun formatTimestampToTimePair(timestamp: Long): Pair<Int, Int> {
        return if (timestamp > 0) getHourAndMinuteFromTimestamp(timestamp) else Pair(0, 0)
    }

    fun formatReminderTimestampToString(
        context: Context,
        timestamp: Long,
        fallbackString: String,// = "Reminder"
    ): String {
        return com.sijayyx.todone.utils.formatReminderTimestampToString(
            context,
            timestamp,
            fallbackString
        )
    }

    fun formatReminderTimestampToString(
        context: Context,
        date: Long,
        hour: Int,
        min: Int,
        fallbackString: String, //Reminder
    ): String {

        try {
            if (date > 0) {
                val ts = combineDateAndTime(date, hour, min)
                return com.sijayyx.todone.utils.formatReminderTimestampToString(
                    context,
                    ts,
                    fallbackString
                )
            } else
                return fallbackString

        } catch (_: Exception) {
            return fallbackString
        }

    }

    fun refreshItemsDoneState() {
        viewModelScope.launch {
            checklistRepository.resetItemsDoneState(uiState.value.selectedChecklistId)
        }
    }

    fun confirmNewListHideState() {
        _uiState.value = _uiState.value.copy(isNewListConfirmed = true)
    }

    //暂时弃用
    fun confirmListAdding() {
        viewModelScope.launch {

            val data = checklistRepository.getChecklistDataById(uiState.value.selectedChecklistId)

            data?.let {
                //TODO 不知道有没有bug
                checklistRepository.updateChecklistData(
                    data.copy(
                        checklistName = uiState.value.checklistTitle,
                        isHide = false,
                        repeatPeriod = uiState.value.selectedRepeatPeriod,
                        repeatNum = uiState.value.selectedRepeatNum,
                        remindTimestamp = uiState.value.selectedRemindTime,
                        color = uiState.value.selectedColor
                    )
                )
            }


            confirmNewListHideState()
        }
    }

    fun setListHide(checklistData: ChecklistData?) {
        viewModelScope.launch {
            if (checklistData != null) {
                checklistRepository.updateChecklistHiddenState(
                    checklistData.id,
                    isHide = true
                )

                _uiState.value = _uiState.value.copy(
                    isNewListConfirmed = false
                )

                checklistRepository.addHiddenChecklist(checklistData.id)
            }
        }
    }

    fun deleteChecklistItem(id: Long) {
        viewModelScope.launch {
            checklistRepository.deleteChecklistItemById(id)
        }
    }

    fun deleteChecklistData() {
        viewModelScope.launch {
            checklistRepository.deleteChecklistById(uiState.value.selectedChecklistId)
        }
    }

    fun updateItemDoneState(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            checklistRepository.updateChecklistItemDoneById(id, isDone)
        }
    }

    fun getChecklistWithData(id: Long?): Flow<ChecklistWithDatas?> {
        val data = checklistRepository.getSingleChecklistByListId(id ?: -1)

        //缓存数据，以在界面删除时维持界面状态
        viewModelScope.launch {
            data.collect {
                if (it != null && id !== null) {
                    cachedSelectedList = mutableMapOf(id to it)
                    Log.e(TAG, "cached list success! id = $id")
                }
            }
        }

        return data
    }

    fun getChecklistDataAndItemsById(id: Long) {
        viewModelScope.launch {
            checklistRepository.getSingleChecklistByListId(id).collect { data ->

                data?.let {
                    _uiState.value = _uiState.value.copy(
                        selectedChecklistId = id,
                        checklistTitle = it.checklist.checklistName,
                        items = it.checklistItems,
                        isNewListConfirmed = !it.checklist.isHide,
                        selectedRepeatNum = it.checklist.repeatNum,
                        selectedRepeatPeriod = it.checklist.repeatPeriod,
                        selectedRemindTime = it.checklist.remindTimestamp,
                        selectedColor = it.checklist.color,
                        selectedRemindWorkerId = it.checklist.reminderWorkerId,
                        selectedRepeatWorkerId = it.checklist.repeatWorkerId
                    )
                }

            }
        }
    }

    fun onEditListItem(id: Long) {
        viewModelScope.launch {
            val item = checklistRepository.getChecklistItemById(id)

            _uiState.value = _uiState.value.copy(
                itemInputState = ChecklistItemInputState.Edit,
                itemContent = item?.content ?: "",
                isDone = item?.isDone ?: false,
                selectedItemId = id,
            )
        }
    }

    fun initializeChecklistData() {
        //如果没有checklistId提供，就新建一个
        viewModelScope.launch {
            if (!checklistRepository.isChecklistDataExists(uiState.value.selectedChecklistId)) {
                _uiState.value = _uiState.value.copy(
                    selectedChecklistId = checklistRepository.insertChecklistData(ChecklistData())
                )
                Log.e(
                    TAG,
                    "NEW LIST ID = ${_uiState.value.selectedChecklistId}"
                )
            }
            getChecklistDataAndItemsById(uiState.value.selectedChecklistId)
        }
    }

    fun updateItemData() {
        viewModelScope.launch {
            checklistRepository.updateChecklistItem(
                ChecklistItemData(
                    checklistItemId = uiState.value.selectedItemId,
                    checklistId = uiState.value.selectedChecklistId,
                    content = uiState.value.itemContent,

                    isDone = uiState.value.isDone
                )
            )

            onEndInput()
            Log.e(TAG, "current selected id = ${uiState.value.selectedItemId}")
        }
    }

    fun onSetAddState() {
        _uiState.value = _uiState.value.copy(
            itemInputState = ChecklistItemInputState.Add,
            isNewListConfirmed = false
        )
    }

    fun addChecklistItem() {
        viewModelScope.launch {
            if (checklistRepository.isChecklistDataExists(uiState.value.selectedChecklistId)) {
                checklistRepository.insertChecklistItemData(
                    ChecklistItemData(
                        checklistId = uiState.value.selectedChecklistId,
                        content = uiState.value.itemContent,
                        isDone = false,
                    )
                )
                Log.e(
                    "MainActivity",
                    "ADD NEW ITEM \"${uiState.value.itemContent}\" AT LIST ${uiState.value.selectedChecklistId}"
                )
            }

            onEndInput()
        }
    }

    fun inputNewItemContent(content: String) {
        _uiState.update { newState ->
            newState.copy(itemContent = content)
        }
    }

    fun updateChecklistData(title: String) {
        viewModelScope.launch {
            _uiState.update { newState ->
                newState.copy(checklistTitle = title)
            }

            val data = checklistRepository.getChecklistDataById(uiState.value.selectedChecklistId)

            //TODO 根据ID动态更新
            data?.let {
                checklistRepository.updateChecklistData(
                    it.copy(
                        id = uiState.value.selectedChecklistId,
                        checklistName = title,
                        isHide = false,
                        repeatPeriod = uiState.value.selectedRepeatPeriod,
                        repeatNum = uiState.value.selectedRepeatNum,
                        remindTimestamp = uiState.value.selectedRemindTime,
                        reminderWorkerId = uiState.value.selectedRemindWorkerId,
                        repeatWorkerId = uiState.value.selectedRepeatWorkerId,
                        color = uiState.value.selectedColor,
                    )
                )
            }
        }
    }

    fun onEndInput() {
        _uiState.update { newState ->
            newState.copy(
                itemContent = "",
                selectedItemId = -1,
                selectedRepeatNum = 0,
                selectedRepeatPeriod = NONE_STRING,
                selectedRemindTime = -1,
                selectedColor = NONE_STRING,
                itemInputState = ChecklistItemInputState.None,
                selectedRemindWorkerId = EMPTY_STRING,
                selectedRepeatWorkerId = EMPTY_STRING
            )
        }
    }

    fun resetUiState() {
        _uiState.value = ChecklistEditUiState()
    }

    fun shareChecklist(
        context: Context,
        checklistData: ChecklistData,
        items: List<ChecklistItemData>
    ) {
        viewModelScope.launch {
            /**
             * 标题
             *
             * 【】内容
             * 【】内容
             * 【】内容
             */
            val _isDone: (Boolean) -> String = {
                if (it) "☑" else "☐"
            }
            var resultString = "${checklistData.checklistName}\n\n"

            items.forEach {
                resultString += "${_isDone(it.isDone)} ${it.content}\n"
            }

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resultString.trimIndent())
                type = "text/plain"
            }

            context.startActivity(Intent.createChooser(shareIntent, null))

            Log.e(TAG, "Sharing data, total ${items.size} elements:\n$resultString")
        }
    }
}

data class ChecklistEditUiState(
    val itemInputState: ChecklistItemInputState = ChecklistItemInputState.None,

    val itemContent: String = "",

    val checklistTitle: String = EMPTY_STRING,

    val items: List<ChecklistItemData> = listOf(),

    val selectedChecklistId: Long = -1,
    val selectedItemId: Long = -1,

    val isNewListConfirmed: Boolean = false,
    val isDone: Boolean = false,
    val selectedRepeatNum: Int = 0,
    val selectedRepeatPeriod: String = NONE_STRING,
    val selectedRemindTime: Long = -1,
    val selectedColor: String = NONE_STRING,
    val selectedRemindWorkerId: String = EMPTY_STRING,
    val selectedRepeatWorkerId: String = EMPTY_STRING
)

data class DateAndTime(val date: Long = -1, val hour: Int = 0, val min: Int = 0)

object DateAndTimeSaver : Saver<DateAndTime, Any> {
    override fun restore(value: Any): DateAndTime? {
        return try {
            val map = value as? Map<*, *> ?: return null
            DateAndTime(
                date = map["date"] as? Long ?: -1,
                hour = map["hour"] as? Int ?: 0,
                min = map["min"] as? Int ?: 0
            )
        } catch (_: Exception) {
            null
        }
    }

    override fun SaverScope.save(value: DateAndTime): Any? {
        return mapOf(
            "date" to value.date,
            "hour" to value.hour,
            "min" to value.min
        )
    }

}

enum class ChecklistItemInputState {
    None,
    Add,
    Edit
}


enum class RepeatPeriod(val content: String) {
    Day(content = "Day"),
    Week(content = "Week"),
    Month(content = "Month"),
    Year(content = "Year"),
    None(content = NONE_STRING)
}