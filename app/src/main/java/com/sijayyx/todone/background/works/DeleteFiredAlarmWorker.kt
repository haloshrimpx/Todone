package com.sijayyx.todone.background.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.repository.AlarmMessageDataRepository
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.TAG

class DeleteFiredAlarmWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    val alarmMessageDataRepository =
        AlarmMessageDataRepository(AppDatabase.getDatabase(context).alarmMessageDao())

    override suspend fun doWork(): Result {
        val alarmHashcode = inputData.getInt(KEY_ALARM_HASHCODE, -1)

        try {
            alarmMessageDataRepository.deleteAlarmMessageDataByHashcode(hashcode = alarmHashcode)

            Log.e(TAG, "DeleteFiredAlarmWorker: alarm $alarmHashcode has been deleted")
            return Result.success()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "DeleteFiredAlarmWorker: alarm $alarmHashcode delete failed, ${e.stackTraceToString()}"
            )
            return Result.failure()
        }

    }
}