package com.sijayyx.todone.data.repository

import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sijayyx.todone.background.alarm.AlarmMessage
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.background.works.ChainReminderWorker
import com.sijayyx.todone.background.works.OneTimeReminderWorker
import com.sijayyx.todone.background.works.ChainRepeatWorker
import com.sijayyx.todone.data.ChecklistDao
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.KEY_ALARM_MESSAGE
import com.sijayyx.todone.utils.KEY_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.KEY_ALARM_TYPE
import com.sijayyx.todone.utils.KEY_ITEM_ID
import com.sijayyx.todone.utils.KEY_REPEAT_NUM
import com.sijayyx.todone.utils.KEY_REPEAT_PERIOD
import com.sijayyx.todone.utils.KEY_REPEAT_TIME
import com.sijayyx.todone.utils.MINUTES_BEFORE_ALARM
import com.sijayyx.todone.utils.NotificationUtils
import com.sijayyx.todone.utils.SCHEDULE_ALARM_HOUR_THRESHOLD
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.TAG_CHECKLIST_ALARM_WORKER
import com.sijayyx.todone.utils.calculateHoursDifferenceFromNow
import com.sijayyx.todone.utils.calculateMinutesDifferenceFromNow
import com.sijayyx.todone.utils.calculateNextTimeToRepeat
import com.sijayyx.todone.utils.checkIsFutureTimestamp
import com.sijayyx.todone.utils.timeBeforeOfLocalDate
import com.sijayyx.todone.utils.timeAfterOfLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class ChecklistRepository(
    private val workManager: WorkManager,
    private val checklistDao: ChecklistDao,
    private val alarmScheduler: AlarmScheduler
) {
    private val _deleteStagingChecklists = MutableStateFlow(setOf<Long>())
    val deleteStagingChecklists = _deleteStagingChecklists.asStateFlow()

    fun addHiddenChecklist(id: Long) {
        _deleteStagingChecklists.value = _deleteStagingChecklists.value.plus(id)
    }

    fun addHiddenChecklists(checklists: Collection<Long>) {
        _deleteStagingChecklists.value = _deleteStagingChecklists.value.plus(checklists)
    }

    fun clearHiddenStagingChecklists() {
        _deleteStagingChecklists.value = emptySet()
    }

    suspend fun updateChecklistHiddenState(id: Long, isHide: Boolean) =
        checklistDao.updateChecklistHiddenState(id, isHide)

    fun canScheduleAlarms() = alarmScheduler.canScheduleAlarms()

    suspend fun insertChecklistItemData(checklistItemData: ChecklistItemData): Long =
        checklistDao.insertChecklistItem(checklistItemData)

    suspend fun updateChecklistData(checklistData: ChecklistData) {
        var data = checklistData

        data = cancelAlarmOrWorks(data)

        //当Checklist被标记为Hide时，它将会被删除，所以不创建新的后台任务
        if (!data.isHide)
            data = createAlarmOrWorks(data)

        return checklistDao.updateChecklistData(data)
    }

    suspend fun updateChecklistItem(checklistItemData: ChecklistItemData) =
        checklistDao.updateChecklistItem(checklistItemData)

    suspend fun resetItemsDoneState(listId: Long) =
        checklistDao.resetItemsDoneState(listId)

    suspend fun updateChecklistItemDoneById(id: Long, isDone: Boolean) =
        checklistDao.updateChecklistItemDoneById(id, isDone)

    suspend fun updateChecklistItemContentById(id: Long, content: String) =
        checklistDao.updateChecklistItemContentById(id, content)

    suspend fun deleteHiddenChecklists() =
        checklistDao.clearHiddenChecklists()

    suspend fun insertChecklistData(checklistData: ChecklistData): Long =
        checklistDao.insertChecklistData(checklistData)

    suspend fun deleteChecklistItemById(id: Long) {
        checklistDao.deleteChecklistItemById(id)
    }


    suspend fun deleteChecklistById(listId: Long) {
        val checklistData = getChecklistDataById(listId)
        checklistData?.let {
            cancelAlarmOrWorks(checklistData)
        }
        checklistDao.deleteChecklistById(listId)
    }

    suspend fun isChecklistDataExists(id: Long) =
        checklistDao.isChecklistExists(id)

    fun getAllChecklists(): Flow<Map<ChecklistData, List<ChecklistItemData>>> =
        checklistDao.getAllChecklists()

    suspend fun getChecklistDataById(id: Long) =
        checklistDao.getChecklistDataByListId(id)

    suspend fun getChecklistItemById(id: Long) =
        checklistDao.getChecklistItemById(id)

    fun getChecklistItemsByListId(listId: Long) =
        checklistDao.getChecklistItemByListId(listId)

    fun getSingleChecklistByListId(listId: Long) =
        checklistDao.getSingleChecklistByListId(listId)

    private suspend fun cancelAlarmOrWorks(checklistData: ChecklistData): ChecklistData {
        val alarmMessage = AlarmUtils.createAlarmMessage(
            itemId = checklistData.id,
            alarmType = AlarmUtils.AlarmType.Checklist,
            message = checklistData.checklistName,
            alarmTimestamp = checklistData.remindTimestamp
        )
        var data = checklistData
        val isContainMessage = alarmScheduler.isAlarmMessageDataExists(alarmMessage.hashcode)

        //检测是否存在AlarmManager
        if (isContainMessage) {
            alarmScheduler.cancelAlarm(alarmMessage)
            Log.e(
                TAG,
                "AlarmScheduler: Cancel alarm of ${alarmMessage.message} success"
            )
        }

        //尝试取消Repeat
        try {
            workManager.cancelUniqueWork(checklistData.repeatWorkerId)
            workManager.cancelUniqueWork(checklistData.reminderWorkerId)

            data = data.copy(
                reminderWorkerId = EMPTY_STRING,
                repeatWorkerId = EMPTY_STRING
            )

            Log.e(
                TAG,
                "WorkManager: Cancelled Reminder and/or Repeat work for Checklist ${checklistData.checklistName}"
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "WorkManager: cancel work of checklist data ${checklistData.checklistName} failed, ${e.stackTraceToString()}"
            )
            return checklistData
        }

        return data
    }

    /**
     * 为ChecklistData设置后台或者前台工作
     *
     * 如果有Repeat，设置重复的Worker
     * 如果没有Repeat，只有Reminder，设置单次提醒
     * 如果都没有，直接返回原来的ChecklistData
     *
     * 设置重复Worker时
     * - 如果设置了Reminder，则以Reminder为起点开始重复
     *     - 当有Reminder时，如果初次的时差小于阈值，则先创建一个Alarm，
     *       再创建delay为 初次alarmTimestamp + 重复周期 的PeriodicWorker
     * - 如果没有设置Reminder，则以创建时间为起点开始重复
     * - Reminder和Worker总是在一起重复
     *
     *  @return 如果创建了Worker，将uuid写入ChecklistData
     */
    private suspend fun createAlarmOrWorks(checklistData: ChecklistData): ChecklistData {
        var data = checklistData

        val alarmMessage = AlarmUtils.createAlarmMessage(
            checklistData.id,
            AlarmUtils.AlarmType.Checklist,
            message = "You have a checklist: ${checklistData.checklistName}",
            alarmTimestamp = checklistData.remindTimestamp
        )

        if (data.repeatNum > 0) {
            //设置重复Worker
            //如果设置了Reminder
            if (checkIsFutureTimestamp(data.remindTimestamp)
                && NotificationUtils.checkValidNotificationTimestamp(data.remindTimestamp)
            ) {

                val initialDelay =
                    if (calculateHoursDifferenceFromNow(data.remindTimestamp) > SCHEDULE_ALARM_HOUR_THRESHOLD) {
                        //以RepeatTime为InitialDelay，创建Repeat和Reminder的Worker
                        calculateMinutesDifferenceFromNow(
                            timeBeforeOfLocalDate(
                                alarmMessage.alarmTimestamp,
                                minutes = MINUTES_BEFORE_ALARM
                            )
                        )
                    } else {
                        //设置Alarm后创建 +1重复周期的Worker
                        alarmScheduler.scheduleOrUpdateAlarm(alarmMessage)
                        //加过一个周期的timestamp
                        when (checklistData.repeatPeriod) {
                            RepeatPeriod.Day.content -> timeAfterOfLocalDate(
                                checklistData.remindTimestamp,
                                days = checklistData.repeatNum.toLong()
                            )

                            RepeatPeriod.Week.content -> timeAfterOfLocalDate(
                                checklistData.remindTimestamp,
                                weeks = checklistData.repeatNum.toLong()
                            )

                            RepeatPeriod.Month.content -> timeAfterOfLocalDate(
                                checklistData.remindTimestamp,
                                months = checklistData.repeatNum.toLong()
                            )

                            RepeatPeriod.Year.content -> timeAfterOfLocalDate(
                                checklistData.remindTimestamp,
                                years = checklistData.repeatNum.toLong()
                            )

                            else -> {
                                Log.e(
                                    TAG,
                                    "AlarmScheduler: Invalid repeat period for checklistData ${checklistData.checklistName}"
                                )
                                checklistData.remindTimestamp
                            }
                        }
                    }

                val chainReminderWorker =
                    OneTimeWorkRequestBuilder<ChainReminderWorker>()
                        .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                        .setInputData(
                            createDataForChainRemindWorker(
                                checklistData = data,
                                alarmMessage = alarmMessage
                            )
                        )
                        .addTag(TAG_CHECKLIST_ALARM_WORKER)
                        .build()
                val reminderWorkerUuid = chainReminderWorker.id.toString()

                val chainRepeatWorker = OneTimeWorkRequestBuilder<ChainRepeatWorker>()
                    .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                    .setInputData(
                        createDataForRepeatWorker(
                            checklistData = data,
                            timeToRepeat = alarmMessage.alarmTimestamp
                        )
                    )
                    .addTag(TAG_CHECKLIST_ALARM_WORKER)
                    .build()
                val repeatWorkerUuid = chainRepeatWorker.id.toString()

                workManager.enqueueUniqueWork(
                    uniqueWorkName = reminderWorkerUuid,
                    existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                    request = chainReminderWorker
                )

                workManager.enqueueUniqueWork(
                    uniqueWorkName = repeatWorkerUuid,
                    existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                    request = chainRepeatWorker
                )

                data = data.copy(
                    repeatWorkerId = repeatWorkerUuid,
                    reminderWorkerId = reminderWorkerUuid
                )

                Log.e(
                    TAG,
                    "WorkManager: Work of Repeat and Remind of alarm message ${alarmMessage.hashcode} is enqueued, will set at $initialDelay minutes from now"
                )
            } else {
                try {
                    //没设置Reminder，以创建时间为起点开始重复
                    //计算下一周期作为开始时间
                    val nextRepeatTime = calculateNextTimeToRepeat(
                        checklistData.createTime,
                        checklistData.repeatNum,
                        checklistData.repeatPeriod
                    )
                    val initialDelay = calculateMinutesDifferenceFromNow(nextRepeatTime)

                    val chainRepeatWorker =
                        OneTimeWorkRequestBuilder<ChainRepeatWorker>()
                            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                            .setInputData(createDataForRepeatWorker(checklistData, nextRepeatTime))
                            .addTag(TAG_CHECKLIST_ALARM_WORKER)
                            .build()
                    val repeatWorkerUuid = chainRepeatWorker.id.toString()

                    workManager.enqueueUniqueWork(
                        uniqueWorkName = repeatWorkerUuid,
                        existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                        request = chainRepeatWorker
                    )

                    data = data.copy(repeatWorkerId = repeatWorkerUuid)
                    Log.e(
                        TAG,
                        "WorkManager: Work of Repeat only of alarm message ${alarmMessage.hashcode} is enqueued, will set at $initialDelay minutes from now"
                    )
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "WorkManager: Work of Repeat only of alarm message ${alarmMessage.hashcode} enqueued failed! ${e.stackTraceToString()}"
                    )
                }

            }

        } else {
            //设置单次Worker或者Alarm
            //设置了reminder
            if (NotificationUtils.checkValidNotificationTimestamp(data.remindTimestamp)) {
                if (calculateHoursDifferenceFromNow(data.remindTimestamp) > SCHEDULE_ALARM_HOUR_THRESHOLD) {
                    //设置单次Worker
                    //开始执行的延迟，从当前到AlarmTimestamp前30min的时间差
                    val delay = calculateMinutesDifferenceFromNow(
                        timeBeforeOfLocalDate(
                            alarmMessage.alarmTimestamp,
                            minutes = MINUTES_BEFORE_ALARM
                        )
                    )

                    val onetimeReminderWorker =
                        OneTimeWorkRequestBuilder<OneTimeReminderWorker>()
                            .setInitialDelay(delay, TimeUnit.MINUTES)
                            .setInputData(createDataForOneTimeRemindWorker(alarmMessage))
                            .addTag(TAG_CHECKLIST_ALARM_WORKER)
                            .build()
                    val uuid = onetimeReminderWorker.id.toString()

                    workManager.enqueueUniqueWork(
                        uniqueWorkName = uuid,
                        existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                        request = onetimeReminderWorker
                    )

                    data = data.copy(reminderWorkerId = uuid)
                    Log.e(
                        TAG,
                        "WorkManager: one time work of alarm message ${alarmMessage.hashcode} is enqueued, will set at $delay minutes from now"
                    )

                } else {
                    //设置Alarm
                    alarmScheduler.scheduleOrUpdateAlarm(alarmMessage)
                }
            } else {
                //都没设置
                Log.e(
                    TAG,
                    "ChecklistRepository: No valid remind and repeat time set for checklist ${checklistData.checklistName}"
                )
            }
        }

        return data
    }

    private fun createDataForRepeatWorker(checklistData: ChecklistData, timeToRepeat: Long): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putLong(KEY_ITEM_ID, checklistData.id)
            .putInt(KEY_REPEAT_NUM, checklistData.repeatNum)
            .putString(KEY_REPEAT_PERIOD, checklistData.repeatPeriod)
            .putLong(KEY_REPEAT_TIME, timeToRepeat).build()
        return dataBuilder.build()
    }

    private fun createDataForOneTimeRemindWorker(alarmMessage: AlarmMessage): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putInt(KEY_ALARM_HASHCODE, alarmMessage.hashcode)
            .putString(KEY_ALARM_MESSAGE, alarmMessage.message)
            .putLong(KEY_ALARM_TIMESTAMP, alarmMessage.alarmTimestamp)
            .putLong(KEY_ITEM_ID, alarmMessage.itemId)
            .putString(KEY_ALARM_TYPE, alarmMessage.alarmType)
        return dataBuilder.build()
    }

    private fun createDataForChainRemindWorker(
        checklistData: ChecklistData,
        alarmMessage: AlarmMessage
    ): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putInt(KEY_REPEAT_NUM, checklistData.repeatNum)
            .putString(KEY_REPEAT_PERIOD, checklistData.repeatPeriod)
            .putLong(KEY_ITEM_ID, checklistData.id)
            .putInt(KEY_ALARM_HASHCODE, alarmMessage.hashcode)
            .putString(KEY_ALARM_MESSAGE, alarmMessage.message)
            .putLong(KEY_ALARM_TIMESTAMP, alarmMessage.alarmTimestamp)
        return dataBuilder.build()

    }

//    private fun calculateRepeatPeriod(num: Int, period: String): Pair<Int, TimeUnit> {
//        return when (period) {
//            ChecklistRepeatPeriod.Day.content -> Pair(num, TimeUnit.DAYS)
//
//            ChecklistRepeatPeriod.Week.content -> Pair(num * 7, TimeUnit.DAYS)
//
////            ChecklistRepeatPeriod.Month.content ->
//
//                ChecklistRepeatPeriod.Year.content ->
//
//            ->
//
//            else ->
//        }
//    }
}