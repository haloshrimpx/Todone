package com.sijayyx.todone.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sijayyx.todone.background.alarm.AlarmMessage

/**
 * 存储AlarmMessage
 *
 * @property id 自动生成的数据库索引
 * @property hashcode 存储的AlarmMessage的hashcode，作为查找的依据
 * @property message
 * @property alarmTimestamp
 */
@Entity(tableName = "alarm_messages")
data class AlarmMessageData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hashcode: Int,
    val itemId: Long,
    val alarmType: String,
    val message: String,
    val alarmTimestamp: Long,
) {
    companion object {
        fun convertAlarmMessageToData(alarmMessage: AlarmMessage): AlarmMessageData {
            return alarmMessage.let {
                AlarmMessageData(
                    hashcode = it.hashcode,
                    itemId = it.itemId,
                    alarmType = it.alarmType,
                    message = it.message,
                    alarmTimestamp = it.alarmTimestamp
                )
            }
        }
    }
}