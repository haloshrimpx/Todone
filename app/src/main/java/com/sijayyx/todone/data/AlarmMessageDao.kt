package com.sijayyx.todone.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

//存储已添加的alarms

@Dao
interface AlarmMessageDao {
    @Insert
    suspend fun insertAlarmMessageData(alarmMessageData: AlarmMessageData)

    @Update
    suspend fun updateAlarmMessageData(alarmMessageData: AlarmMessageData)

    @Query("UPDATE alarm_messages SET message =:message, alarmTimestamp = :alarmTimestamp WHERE hashcode = :hashcode")
    suspend fun updateAlarmMessageDataByHashcode(hashcode: Int, message: String, alarmTimestamp: Long)

    @Delete
    suspend fun deleteAlarmMessageData(alarmMessageData: AlarmMessageData)

    @Query("SELECT * FROM alarm_messages WHERE hashcode = :hashcode")
    suspend fun getAlarmMessageDataByHashcode(hashcode: Int): AlarmMessageData?

    @Query("DELETE FROM alarm_messages WHERE hashcode = :hashcode")
    suspend fun deleteAlarmMessageDataByHashcode(hashcode: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM alarm_messages WHERE hashcode = :hashcode)")
    suspend fun isAlarmMessageDataExists(hashcode: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM alarm_messages)")
    suspend fun isDbEmpty(): Boolean

    @Query("SELECT * FROM alarm_messages")
    suspend fun getAllAlarmMessageData(): List<AlarmMessageData>
}