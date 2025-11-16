package com.sijayyx.todone.data.repository

import android.util.Log
import com.sijayyx.todone.data.AlarmMessageDao
import com.sijayyx.todone.data.AlarmMessageData
import com.sijayyx.todone.utils.TAG

class AlarmMessageDataRepository(
    private val alarmMessageDao: AlarmMessageDao
) {
    suspend fun insertAlarmMessageData(alarmMessageData: AlarmMessageData) {
        alarmMessageDao.insertAlarmMessageData(alarmMessageData)
        Log.e(
            TAG,
            "AlarmMessageDataRepository: alarmMessageData ${alarmMessageData.hashcode} inserted"
        )
    }

    suspend fun updateAlarmMessageDataByHashcode(
        hashcode: Int,
        message: String,
        alarmTimestamp: Long
    ) =
        alarmMessageDao.updateAlarmMessageDataByHashcode(hashcode, message, alarmTimestamp)

    suspend fun deleteAlarmMessageData(alarmMessageData: AlarmMessageData) =
        alarmMessageDao.deleteAlarmMessageData(alarmMessageData)

    suspend fun getAlarmMessageDataByHashcode(hashcode: Int): AlarmMessageData? =
        alarmMessageDao.getAlarmMessageDataByHashcode(hashcode)

    suspend fun deleteAlarmMessageDataByHashcode(hashcode: Int) {
        alarmMessageDao.deleteAlarmMessageDataByHashcode(hashcode)
        Log.e(
            TAG,
            "AlarmMessageDataRepository: alarmMessageData $hashcode deleted"
        )
    }

    suspend fun getAllAlarmMessageData(): List<AlarmMessageData> =
        alarmMessageDao.getAllAlarmMessageData()

    suspend fun isAlarmMessageDataExists(hashcode: Int): Boolean =
        alarmMessageDao.isAlarmMessageDataExists(hashcode)

    suspend fun isDbEmpty(): Boolean = alarmMessageDao.isDbEmpty()

}