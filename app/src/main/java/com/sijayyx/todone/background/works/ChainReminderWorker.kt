package com.sijayyx.todone.background.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sijayyx.todone.background.alarm.AlarmMessage
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.KEY_ALARM_MESSAGE
import com.sijayyx.todone.utils.KEY_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.KEY_ITEM_ID
import com.sijayyx.todone.utils.KEY_REPEAT_NUM
import com.sijayyx.todone.utils.KEY_REPEAT_PERIOD
import com.sijayyx.todone.utils.KEY_WORKER_UUID
import com.sijayyx.todone.utils.MINUTES_BEFORE_ALARM
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.TAG_CHECKLIST_ALARM_WORKER
import com.sijayyx.todone.utils.calculateMinutesDifferenceFromNow
import com.sijayyx.todone.utils.calculateNextTimeToRepeat
import com.sijayyx.todone.utils.timeBeforeOfLocalDate
import java.util.concurrent.TimeUnit

class ChainReminderWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    /**
     * 传入：重复周期数，重复周期单位，提醒时间，ChecklistId
     * 设置提醒时间的Alarm后,将提醒时间加上重复周期,存入Checklist数据库
     */
    val workManager = WorkManager.getInstance(ctx)
    val alarmScheduler = AlarmScheduler(context = ctx)
    val checklistBackgroundDao = AppDatabase.getDatabase(ctx).checklistBackgroundDao()

    override suspend fun doWork(): Result {
        return try {
            val checklistId = inputData.getLong(KEY_ITEM_ID, -1)
            val repeatNum = inputData.getInt(KEY_REPEAT_NUM, -1)
            val repeatPeriod = inputData.getString(KEY_REPEAT_PERIOD) ?: NONE_STRING

            val hashCode: Int = inputData.getInt(KEY_ALARM_HASHCODE, -1)
            val message: String = inputData.getString(KEY_ALARM_MESSAGE) ?: NONE_STRING
            val remindTimestamp: Long = inputData.getLong(KEY_ALARM_TIMESTAMP, -1)

            if (checklistId < 0) {
                Log.e(
                    TAG,
                    "PeriodicReminderWorker: Invalid Checklist ID for message \"$message\" !"
                )
                return Result.failure()
            } else if (remindTimestamp < 0) {
                Log.e(
                    TAG,
                    "PeriodicReminderWorker: Invalid Timestamp for message \"$message\" !"
                )
                return Result.failure()
            }

            val alarmMessage = AlarmMessage(
                hashcode = hashCode,
                itemId = checklistId,
                alarmType = AlarmUtils.AlarmType.Checklist.content,
                message = message,
                alarmTimestamp = remindTimestamp
            )

            try {
                alarmScheduler.scheduleOrUpdateAlarm(alarmMessage)
            } catch (e: Exception) {
                Log.e(TAG, "AlarmScheduler: Permission denied for reminder worker!")
            }

            //设置下一次的提醒时间, 提交下一次的任务
            val nextRemindTime: Long =
                calculateNextTimeToRepeat(alarmMessage.alarmTimestamp, repeatNum, repeatPeriod)
            checklistBackgroundDao.setChecklistRemindTime(checklistId, nextRemindTime)

            val nextAlarmMessage = alarmMessage.copy(alarmTimestamp = nextRemindTime)

            val nextWorkUuid = scheduleNextTimeWork(
                nextAlarmMessage = nextAlarmMessage,
                workData = createDataForChainRemindWorker(
                    repeatNum = repeatNum,
                    repeatPeriod = repeatPeriod,
                    checklistId = checklistId,
                    alarmMessage = nextAlarmMessage
                ),
                workManager = workManager
            )

            checklistBackgroundDao.setChecklistReminderWorkerId(checklistId, nextWorkUuid)

            val outputData = Data.Builder()
                .putLong(KEY_ITEM_ID, checklistId)
                .putString(KEY_WORKER_UUID, nextWorkUuid)
                .putLong(KEY_ALARM_TIMESTAMP, nextRemindTime)
                .build()

            Result.success(outputData)
        } catch (e: Exception) {

            Log.e(
                TAG,
                "PeriodicReminderWorker: work failed, ${e.stackTraceToString()} "
            )
            Result.failure()
        }
    }

    private fun scheduleNextTimeWork(
        nextAlarmMessage: AlarmMessage,
        workData: Data,
        workManager: WorkManager
    ): String {

        val delayTillNextRemind = calculateMinutesDifferenceFromNow(
            timeBeforeOfLocalDate(nextAlarmMessage.alarmTimestamp, minutes = MINUTES_BEFORE_ALARM)
        )

        val chainReminderWorkRequest =
            OneTimeWorkRequestBuilder<ChainReminderWorker>()
                .setInitialDelay(delayTillNextRemind, TimeUnit.MINUTES)
                .setInputData(workData)
                .addTag(TAG_CHECKLIST_ALARM_WORKER)
                .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = "remind_${nextAlarmMessage.hashcode}",
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = chainReminderWorkRequest
        )

        return chainReminderWorkRequest.id.toString()
    }

    private fun createDataForChainRemindWorker(
        repeatNum: Int,
        repeatPeriod: String,
        checklistId: Long,
        alarmMessage: AlarmMessage
    ): Data {
        val dataBuilder = Data.Builder()
        dataBuilder.putInt(KEY_REPEAT_NUM, repeatNum)
            .putString(KEY_REPEAT_PERIOD, repeatPeriod)
            .putLong(KEY_ITEM_ID, checklistId)
            .putInt(KEY_ALARM_HASHCODE, alarmMessage.hashcode)
            .putString(KEY_ALARM_MESSAGE, alarmMessage.message)
            .putLong(KEY_ALARM_TIMESTAMP, alarmMessage.alarmTimestamp)
        return dataBuilder.build()

    }
}