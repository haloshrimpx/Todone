package com.sijayyx.todone.background.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sijayyx.todone.background.alarm.AlarmMessage
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.utils.FunctionResult
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.KEY_ALARM_MESSAGE
import com.sijayyx.todone.utils.KEY_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.KEY_ALARM_TYPE
import com.sijayyx.todone.utils.KEY_ITEM_ID
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG

class OneTimeReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    val alarmScheduler = AlarmScheduler(context = context)

    override suspend fun doWork(): Result {
        return setAlarm()
    }

    private suspend fun setAlarm(): Result {
        return try {
            val itemId = inputData.getLong(KEY_ITEM_ID, -1)
            val hashCode: Int = inputData.getInt(KEY_ALARM_HASHCODE, -1)
            val message: String = inputData.getString(KEY_ALARM_MESSAGE) ?: NONE_STRING
            val remindTimestamp: Long = inputData.getLong(KEY_ALARM_TIMESTAMP, -1)
            val alarmType: String =
                inputData.getString(KEY_ALARM_TYPE) ?: AlarmUtils.AlarmType.None.content

            if (itemId < 0)
                return Result.failure()

            val alarmMessage = AlarmMessage(
                hashcode = hashCode,
                itemId = itemId,
                alarmType = alarmType,
                message = message,
                alarmTimestamp = remindTimestamp
            )

            val result = alarmScheduler.scheduleOrUpdateAlarm(alarmMessage)

            if (result == FunctionResult.Success)
                Result.success()
            else
                Result.failure()

        } catch (throwable: Throwable) {
            Log.e(TAG, "ReminderSetterWorker: work failed, ${throwable.stackTraceToString()}")
            Result.failure()
        }
    }
}