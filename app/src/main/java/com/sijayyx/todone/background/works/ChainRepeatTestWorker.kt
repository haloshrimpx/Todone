package com.sijayyx.todone.background.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.ChecklistBackgroundDao
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.KEY_ITEM_ID
import com.sijayyx.todone.utils.KEY_REPEAT_NUM
import com.sijayyx.todone.utils.KEY_REPEAT_PERIOD
import com.sijayyx.todone.utils.KEY_REPEAT_TIME
import com.sijayyx.todone.utils.MINUTES_BEFORE_ALARM
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.TAG_CHECKLIST_ALARM_WORKER
import com.sijayyx.todone.utils.calculateMinutesDifferenceFromNow
import com.sijayyx.todone.utils.calculateNextTimeToRepeat
import com.sijayyx.todone.utils.timeBeforeOfLocalDate
import java.util.concurrent.TimeUnit

class ChainRepeatTestWorker(
    context: Context, params: WorkerParameters,
) :
    CoroutineWorker(context, params) {

    val checklistBackgroundDao: ChecklistBackgroundDao =
        AppDatabase.getDatabase(context).checklistBackgroundDao()
    val checklistDao = AppDatabase.getDatabase(context).checklistDao()
    val workManager: WorkManager = WorkManager.getInstance(context)

    override suspend fun doWork(): Result {
        try {

            val testChecklistData = ChecklistData(
                checklistName = "New Checklist",
                createTime = System.currentTimeMillis(),
                repeatPeriod = RepeatPeriod.Day.content,
                repeatNum = 1,
                remindTimestamp = -1,
                color = NONE_STRING,
                reminderWorkerId = EMPTY_STRING,
                repeatWorkerId = EMPTY_STRING
            )

            val newChecklistId = checklistDao.insertChecklistData(testChecklistData)

            val checklistItemData = ChecklistItemData(
                checklistId = newChecklistId,
                content = "TEST",
                isDone = true
            )

            val newItemId = checklistDao.insertChecklistItem(checklistItemData)

            val repeatNum = inputData.getInt(KEY_REPEAT_NUM, -1)
            val repeatPeriod = inputData.getString(KEY_REPEAT_PERIOD) ?: NONE_STRING
            val currentRepeatTime = inputData.getLong(KEY_REPEAT_TIME, -1)


            checklistBackgroundDao.resetItemsDoneState(newChecklistId)

            val nextTime = calculateNextTimeToRepeat(currentRepeatTime, repeatNum, repeatPeriod)
            val workData =
                createDataForRepeatWorker(repeatNum, repeatPeriod, nextTime, newChecklistId)

            val nextWorkUuid = scheduleNextTimeWork(nextTime, workData, workManager)

            checklistBackgroundDao.setChecklistRepeatWorkerId(newChecklistId, nextWorkUuid)

            Log.e(TAG, "ChainRepeatWorker: Work successfully fired")

            val finalChecklistItemData1 = checklistDao.getChecklistItemById(newItemId)!!
            Log.e(TAG, "FINAL CHECKLIST ITEM DATA DONE STATE = ${finalChecklistItemData1.isDone}")

            val outputData = Data.Builder().putLong(KEY_REPEAT_TIME, nextTime)
                .putLong("KEY_CHECKLIST_ITEM_ID", newItemId).build()

            return Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "ChainRepeatWorker: Work failed!!! ${e.stackTraceToString()}")
            return Result.failure()
        }

    }

    private fun scheduleNextTimeWork(
        nextTime: Long,
        workData: Data,
        workManager: WorkManager
    ): String {
        val delayTillNextRemind = calculateMinutesDifferenceFromNow(
            timeBeforeOfLocalDate(nextTime, minutes = MINUTES_BEFORE_ALARM)
        )

        val chainReminderWorkRequest =
            OneTimeWorkRequestBuilder<ChainRepeatWorker>()
                .setInitialDelay(delayTillNextRemind, TimeUnit.MINUTES)
                .setInputData(workData)
                .addTag(TAG_CHECKLIST_ALARM_WORKER)
                .build()

        val uuid = chainReminderWorkRequest.id.toString()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uuid,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = chainReminderWorkRequest
        )

        return uuid
    }

    private fun createDataForRepeatWorker(
        repeatNum: Int,
        repeatPeriod: String,
        repeatTime: Long,
        checklistId: Long
    ): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putLong(KEY_ITEM_ID, checklistId)
            .putInt(KEY_REPEAT_NUM, repeatNum)
            .putString(KEY_REPEAT_PERIOD, repeatPeriod)
            .putLong(KEY_REPEAT_TIME, repeatTime).build()
        return dataBuilder.build()
    }
}