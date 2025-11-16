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

class ChainRepeatWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    /**
     * 重置Checklist的完成状态
     */

    val checklistBackgroundDao = AppDatabase.getDatabase(context).checklistBackgroundDao()
    val workManager = WorkManager.getInstance(context)

    override suspend fun doWork(): Result {
        try {
            val checklistId = inputData.getLong(KEY_ITEM_ID, -1)
            val repeatNum = inputData.getInt(KEY_REPEAT_NUM, -1)
            val repeatPeriod = inputData.getString(KEY_REPEAT_PERIOD) ?: NONE_STRING
            val currentRepeatTime = inputData.getLong(KEY_REPEAT_TIME, -1)

            if (checklistId < 0 || repeatNum < 0 || currentRepeatTime < 0)
                return Result.failure()

            checklistBackgroundDao.resetItemsDoneState(checklistId)

            val nextTime = calculateNextTimeToRepeat(currentRepeatTime, repeatNum, repeatPeriod)
            val workData = createDataForRepeatWorker(repeatNum, repeatPeriod, nextTime, checklistId)

            val nextWorkUuid = scheduleNextTimeWork(nextTime, workData, workManager)

            checklistBackgroundDao.setChecklistRepeatWorkerId(checklistId, nextWorkUuid)

            Log.e(TAG, "ChainRepeatWorker: Work successfully fired")

            return Result.success(workData)
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

        val chainRepeatWorker =
            OneTimeWorkRequestBuilder<ChainRepeatWorker>()
                .setInitialDelay(delayTillNextRemind, TimeUnit.MINUTES)
                .setInputData(workData)
                .addTag(TAG_CHECKLIST_ALARM_WORKER)
                .build()

        val uuid = chainRepeatWorker.id.toString()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uuid,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = chainRepeatWorker
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