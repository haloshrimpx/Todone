package com.sijayyx.todone.data.repository

import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import com.sijayyx.todone.background.alarm.AlarmMessage
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.utils.NotificationUtils
import com.sijayyx.todone.background.works.OneTimeReminderWorker
import com.sijayyx.todone.data.TodoItemData
import com.sijayyx.todone.data.TodoListDao
import com.sijayyx.todone.data.TodoListData
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.FunctionResult
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.KEY_ALARM_MESSAGE
import com.sijayyx.todone.utils.KEY_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.KEY_ALARM_TYPE
import com.sijayyx.todone.utils.KEY_ITEM_ID
import com.sijayyx.todone.utils.MINUTES_BEFORE_ALARM
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.SCHEDULE_ALARM_HOUR_THRESHOLD
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.TAG_TODO_ALARM_WORKER
import com.sijayyx.todone.utils.calculateHoursDifferenceFromNow
import com.sijayyx.todone.utils.calculateMinutesDifferenceFromNow
import com.sijayyx.todone.utils.checkIsFutureTimestamp
import com.sijayyx.todone.utils.timeBeforeOfLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit

class TodoListRepository(
    private val workManager: WorkManager,
    private val todoListDao: TodoListDao,
    private val alarmScheduler: AlarmScheduler
) {
    fun canScheduleAlarms() = alarmScheduler.canScheduleAlarms()

    suspend fun insertTodoListData(todoListData: TodoListData): Long =
        todoListDao.insertTodoListData(todoListData)

    suspend fun insertTodoItemData(todoItemData: TodoItemData): Long {
        val createdId = todoListDao.insertTodoItemData(todoItemData)
        val item = createTodoItemAlarmOrUpdateWorker(createdId, todoItemData)

        if (item.reminderWorkerId != NONE_STRING && item.reminderWorkerId != EMPTY_STRING) {
            updateTodoItemData(
                item.copy(todoItemId = createdId),
                isCreateAlarm = false
            )
            Log.e(
                TAG,
                "WorkManager: created uuid $item for todoItemData \"${todoItemData.content}\""
            )
        }

        return createdId
    }

    suspend fun updateTodoListData(todoListData: TodoListData) =
        todoListDao.updateTodoListData(todoListData)

    suspend fun updateTodoItemData(todoItemData: TodoItemData, isCreateAlarm: Boolean = true) {

        if (isCreateAlarm) {
            val item = createTodoItemAlarmOrUpdateWorker(
                itemId = todoItemData.todoItemId,
                todoItemData = todoItemData
            )

            todoListDao.updateTodoItemData(item)
        } else {
            todoListDao.updateTodoItemData(todoItemData)
        }

        Log.e(
            TAG,
            "TodoListRepository: updated todoItemData of content \"${todoItemData.content}\""
        )
    }

    suspend fun updateTodoItemDoneById(id: Long, isDone: Boolean) {
        var item = getTodoItemDataById(id)?.copy(isDone = isDone)

        if (item != null) { //isDone被修改为是或否，对alarm做对应判断
            item = if (isDone) {
                cancelAlarmOrWorker(item)
            } else {
                createTodoItemAlarmOrUpdateWorker(id, item)
            }

            todoListDao.updateTodoItemData(item)
        }
        Log.e(TAG, "update todoItemDone: $isDone")
//        todoListDao.updateTodoItemDoneById(id, isDone)
    }

    suspend fun updateTodoItemById(id: Long, content: String, selectedListId: Long) =
        todoListDao.updateTodoItemContentById(id, content, selectedListId)

    suspend fun updateTodoItemToListById(itemId: Long, listId: Long) =
        todoListDao.updateTodoItemToListById(itemId, listId)

    suspend fun deleteTodoListData(todoListData: TodoListData) =
        todoListDao.deleteTodoListData(todoListData)

    suspend fun deleteTodoItemData(todoItemData: TodoItemData) =
        todoListDao.deleteTodoItemData(todoItemData)

    suspend fun deleteTodoItemDataById(id: Long) {
        val item = getTodoItemDataById(id)
        item?.let {
            cancelAlarmOrWorker(it)
            todoListDao.deleteTodoItemData(it)
            Log.e(
                TAG,
                "TodoItem $id has been deleted, content = ${item.content}, workerId = ${item.reminderWorkerId}"
            )
        }
    }

    suspend fun deleteTodoListDataById(id: Long) =
        todoListDao.deleteTodoListDataById(id)

    suspend fun getTodoItemDataById(itemId: Long): TodoItemData? =
        todoListDao.getTodoItemDataById(itemId)

    suspend fun getTodoListDataById(listId: Long): TodoListData? =
        todoListDao.getTodoListDataById(listId)

    fun getTodoListDataFlowById(id: Long): Flow<TodoListData?> =
        todoListDao.getTodoListDataFlowById(id)

    suspend fun getTodoListDataByName(name: String): TodoListData? =
        todoListDao.getTodoListDataByName(name)

    suspend fun isTodoListExists(listName: String): Boolean =
        todoListDao.isTodoListExists(listName)

    suspend fun isTodoListExists(id: Long): Boolean =
        todoListDao.isTodoListExists(id)

    fun getTodoItemsDataByListId(listId: Long): Flow<List<TodoItemData>> =
        todoListDao.getTodoItemsDataByListId(listId)

    fun getSingleListWithItems(id: Long): Flow<Map<TodoListData, List<TodoItemData>>> =
        todoListDao.getSingleListWithItems(id)

    fun getTodoListsWithItems(): Flow<Map<TodoListData, List<TodoItemData>>> =
        todoListDao.getTodoListsWithItems()

    fun getAllTodoItems(): Flow<List<TodoItemData>> =
        todoListDao.getAllTodoItems()

    fun getAllTodoLists(): Flow<List<TodoListData>> =
        todoListDao.getAllTodoLists()

    suspend fun getTodoListsWithNamePattern(
        originalName: String,
        pattern: String
    ): List<TodoListData> =
        todoListDao.getTodoListsWithNamePattern(originalName, pattern)

    //重名检测，生成流水号
    suspend fun checkSameListName(originalName: String): String {
        return withContext(Dispatchers.IO) {
            // 查找所有可能的重名项目（包含原始名称和带数字的）
            val pattern = "$originalName (%)"
            val existingItems = getTodoListsWithNamePattern(originalName, pattern)

            // 提取现有的数字并找到下一个可用的数字
            val nextNumber = findNextAvailableNumber(originalName, existingItems)

            if (nextNumber >= 1) {
                "$originalName ($nextNumber)"
            } else {
                originalName
            }
        }
    }

    private fun findNextAvailableNumber(
        originalName: String,
        existingLists: List<TodoListData>
    ): Int {
        val numbers = mutableListOf<Int>()

        existingLists.forEach { data ->
            when {
                //如果只存在一个重名的，认为使用了数字0
                data.listName == originalName -> numbers.add(0)
                data.listName.startsWith("$originalName (") && data.listName.endsWith(")") -> {
                    val numberPart = data.listName.removePrefix("$originalName (").removeSuffix(")")
                    numberPart.toIntOrNull()?.let { numbers.add(it) }
                }
            }
        }

        var nextNumber = 0
        while (numbers.contains(nextNumber)) {
            nextNumber++
        }
        return nextNumber
    }

    /**
     * 当添加/修改一个TodoItem时，如果设置了reminder，就把它添加到后台中以创建提醒
     *
     * @param itemId TodoItem的ID
     * @param todoItemData TodoItem具体的数据
     * @return 如果成功创建Worker，则返回添加了UUID的TodoData，否则返回它本身
     */
    private suspend fun createTodoItemAlarmOrUpdateWorker(
        itemId: Long,
        todoItemData: TodoItemData
    ): TodoItemData {

        //尝试取消已经存在的任务
        var todoData = cancelAlarmOrWorker(todoItemData)

        if (
            NotificationUtils.checkValidNotificationTimestamp(todoItemData.reminderStamp)
            && checkIsFutureTimestamp(todoItemData.reminderStamp)
            && !todoItemData.isDone
        ) {
            val alarmMessage = AlarmUtils.createAlarmMessage(
                itemId,
                AlarmUtils.AlarmType.TodoItem,
                todoItemData.reminderStamp, //检查reminderStamp
                todoItemData.content
            )

            //如果提醒时间与当前时间大于阈值，就添加后台任务
            if (calculateHoursDifferenceFromNow(todoItemData.reminderStamp) > SCHEDULE_ALARM_HOUR_THRESHOLD) {
                val uuid = enqueueOnetimeReminderWorker(alarmMessage)
                todoData = todoData.copy(reminderWorkerId = uuid)
            }
            //如果小于阈值，就创建alarm
            else {
                alarmScheduler.scheduleOrUpdateAlarm(alarmMessage)
            }
        } else {
            Log.i(
                TAG,
                "item is done = ${todoItemData.isDone} or alarm time invalid: ${todoItemData.reminderStamp}"
            )
        }

        return todoData
    }

    /**
     * 将alarmMessage添加到后台中去
     *
     * @param alarmMessage 通知信息
     * @return 添加的任务的UUID
     */
    private fun enqueueOnetimeReminderWorker(alarmMessage: AlarmMessage): String {
        //开始执行的延迟，从当前到AlarmTimestamp前30min的时间差
        val delay = calculateMinutesDifferenceFromNow(
            timeBeforeOfLocalDate(alarmMessage.alarmTimestamp, minutes = MINUTES_BEFORE_ALARM)
        )

        val onetimeReminderWorker =
            OneTimeWorkRequestBuilder<OneTimeReminderWorker>()
//                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .setInputData(createDataForReminderSetter(alarmMessage))
                .addTag(TAG_TODO_ALARM_WORKER)
                .build()

        val uuid = onetimeReminderWorker.id.toString()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uuid,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = onetimeReminderWorker
        )
        Log.e(
            TAG,
            "WorkManager: work of alarm message ${alarmMessage.hashcode} is enqueued, will set at $delay minutes from now"
        )
        return uuid
    }

    private fun updateOneTimeReminderWorker(uuid: String, alarmMessage: AlarmMessage): String {
        try {
            //开始执行的延迟，从当前到AlarmTimestamp前30min的时间差
            val delay = calculateMinutesDifferenceFromNow(
                timeBeforeOfLocalDate(alarmMessage.alarmTimestamp, minutes = MINUTES_BEFORE_ALARM)
            )

            val onetimeReminderWorker =
                OneTimeWorkRequestBuilder<OneTimeReminderWorker>()
//                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInitialDelay(delay, TimeUnit.MINUTES)
                    .setInputData(createDataForReminderSetter(alarmMessage))
                    .setId(UUID.fromString(uuid))
                    .addTag("alarm")
                    .build()

            workManager.updateWork(
                request = onetimeReminderWorker
            )
            Log.e(
                TAG,
                "WorkManager: work $uuid of alarm message ${alarmMessage.hashcode} is updated, will set at $delay minutes from now"
            )
            return onetimeReminderWorker.id.toString()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "WorkManager: work $uuid of alarm message ${alarmMessage.hashcode} update failed, ${e.stackTraceToString()}"
            )
            return uuid
        }

    }

    /**
     * 构造为worker传入的alarmMessage信息，以在WorkRequest内获取
     *
     * @param alarmMessage 需要构造的信息
     * @return 构造出的数据
     */
    private fun createDataForReminderSetter(alarmMessage: AlarmMessage): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putInt(KEY_ALARM_HASHCODE, alarmMessage.hashcode)
            .putString(KEY_ALARM_MESSAGE, alarmMessage.message)
            .putLong(KEY_ALARM_TIMESTAMP, alarmMessage.alarmTimestamp)
            .putLong(KEY_ITEM_ID, alarmMessage.itemId)
            .putString(KEY_ALARM_TYPE, alarmMessage.alarmType)
        return dataBuilder.build()
    }

    /**
     * 尝试取消TodoItem对应的Alarm或者Worker
     *
     * @param todoItemData
     * @return 如果成功取消了Worker，则返回被清空reminderWorkerId的TodoItem，否则返回它本身
     */
    private suspend fun cancelAlarmOrWorker(todoItemData: TodoItemData): TodoItemData {
        try {
            val alarmMessage = AlarmUtils.createAlarmMessage(
                itemId = todoItemData.todoItemId,
                alarmType = AlarmUtils.AlarmType.TodoItem,
                message = todoItemData.content,
                alarmTimestamp = todoItemData.reminderStamp
            )
            var finalResult: FunctionResult
            var todoData = todoItemData
            val isContainMessage = alarmScheduler.isAlarmMessageDataExists(alarmMessage.hashcode)
            val isWorkerIdExists =
                todoItemData.reminderWorkerId != EMPTY_STRING && todoItemData.reminderWorkerId != NONE_STRING

            //检测是否存在AlarmManager
            if (isContainMessage) {
                alarmScheduler.cancelAlarm(alarmMessage)
            }

            if (isWorkerIdExists) {
                try {
                    val result =
                        workManager.cancelUniqueWork(todoItemData.reminderWorkerId).result

                    when (result.get()) {
                        is Operation.State.SUCCESS -> {
                            Log.e(
                                TAG,
                                "WorkManager: work cancellation of ${todoItemData.content} success"
                            )
                            finalResult = FunctionResult.Success
                            todoData = todoData.copy(reminderWorkerId = EMPTY_STRING)
                        }

                        else -> {
                            Log.e(
                                TAG,
                                "WorkManager: work cancellation result ${result.get()}"
                            )
                            finalResult = FunctionResult.Failure
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "WorkManager: cancel work by todoItemData ${todoItemData.content} has failed, because ${e.message}"
                    )
                    finalResult = FunctionResult.Failure
                }
            } else {
                finalResult = FunctionResult.Failure
            }
            Log.e(
                TAG,
                "TodoListRepository: Reminder cancellation ended with a result $finalResult,\n " +
                        "   isWorkerIdExists = $isWorkerIdExists, isContainsMessage = $isContainMessage,\n " +
                        "   target TodoItem is ${todoItemData.content}, WorkerId = ${todoItemData.reminderWorkerId}"
            )

            return todoData
        } catch (e: Exception) {
            Log.e(
                TAG,
                "TodoListRepository: cancel alarm or worker errored, ${e.stackTraceToString()}"
            )
            return todoItemData
        }


    }
}