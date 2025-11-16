package com.sijayyx.todone.background.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.sijayyx.todone.background.receiver.AlarmNotificationReceiver
import com.sijayyx.todone.background.receiver.DeviceBootReceiver
import com.sijayyx.todone.data.AlarmMessageData
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.repository.AlarmMessageDataRepository
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.FunctionResult
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_HASHCODE
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_TYPE
import com.sijayyx.todone.utils.INTENT_EXTRA_ITEM_ID
import com.sijayyx.todone.utils.INTENT_EXTRA_MESSAGE
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.checkIsFutureTimestamp
import com.sijayyx.todone.utils.formatTimestampToReadableString
import java.lang.Exception
import java.util.Objects

object AlarmUtils {
    enum class AlarmType(val content: String) {
        None("None"),
        TodoItem("TodoItem"),
        Checklist("Checklist");
    }

    data class AlarmHash(val id: Int, val alarmType: AlarmType) {
        override fun hashCode(): Int {
            val result = Objects.hashCode(id.toString() + alarmType.toString())
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AlarmHash

            return id == other.id && alarmType == other.alarmType
        }
    }


    //提醒设置
    fun createAlarmMessage(
        itemId: Long,
        alarmType: AlarmType,
        alarmTimestamp: Long,
        message: String
    ): AlarmMessage {
        val result = AlarmMessage(
            itemId = itemId,
            alarmType = alarmType.toString(),
            hashcode = createHashCodeForAlarm(itemId.toInt(), alarmType),
            message = "You have a todo now:${message}",
            alarmTimestamp = alarmTimestamp,
        )

        return result
    }

    fun convertAlarmMessageFromData(alarmMessageData: AlarmMessageData): AlarmMessage {
        return alarmMessageData.let {
            AlarmMessage(
                hashcode = it.hashcode,
                itemId = it.itemId,
                alarmType = it.alarmType,
                message = it.message,
                alarmTimestamp = it.alarmTimestamp
            )
        }
    }

    fun createHashCodeForAlarm(id: Int, type: AlarmType): Int {

        val result = AlarmHash(id, type).hashCode()
        Log.e(
            TAG, "created hash code for alarm: $result, id = $id, type = $type\n"
        )

        val stackTrace = Thread.currentThread().stackTrace
        stackTrace.forEachIndexed { index, element ->
            if (index > 2) { // 跳过 getStackTrace 和 logStackTrace 本身
                Log.d(
                    TAG,
                    "   at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
                )
            }
        }
        return result
    }
}

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val alarmRepository = AlarmMessageDataRepository(
        AppDatabase.getDatabase(context).alarmMessageDao()
    )
    val bootReceiver = ComponentName(context, DeviceBootReceiver::class.java)
    val alarmNotificationReceiver = ComponentName(context, AlarmNotificationReceiver::class.java)

    fun canScheduleAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    suspend fun restoreFromAlarmDb() {
        try {
            var logText = "Restored alarms from database when boot complete: \n"

            val storedData = alarmRepository.getAllAlarmMessageData()
            storedData.forEach {
                val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
                    putExtra(INTENT_EXTRA_MESSAGE, it.message)
                    putExtra(INTENT_EXTRA_ALARM_HASHCODE, it.hashcode)
                    putExtra(INTENT_EXTRA_ALARM_TYPE, it.alarmType)
                    putExtra(INTENT_EXTRA_ITEM_ID, it.itemId)
                    putExtra(INTENT_EXTRA_ALARM_TIMESTAMP, it.alarmTimestamp)
                }

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    it.alarmTimestamp,
                    PendingIntent.getBroadcast(
                        context,
                        it.hashcode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )

                logText = logText + "   alarm ${it.hashcode}, message = ${it.message}\n"
            }

            Log.i(TAG, logText)
        } catch (e: kotlin.Exception) {
            Log.i(TAG, "AlarmScheduler: restore data failed\n ${e.stackTraceToString()}")
        }
    }

    /**
     * 创建或更新一个Alarm，同时存进Repository
     *
     * @param alarmMessage
     * @return 函数是否成功执行完毕
     */
    suspend fun scheduleOrUpdateAlarm(alarmMessage: AlarmMessage): FunctionResult {
        try {
            //无效消息保护
            if (!checkIsFutureTimestamp(alarmMessage.alarmTimestamp)) {
                Log.e(
                    TAG,
                    "AlarmScheduler: Invalid alarm message! timestamp=${alarmMessage.alarmTimestamp}"
                )
                return FunctionResult.Failure
            }

            val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
                putExtra(INTENT_EXTRA_MESSAGE, alarmMessage.message)
                putExtra(INTENT_EXTRA_ALARM_HASHCODE, alarmMessage.hashcode)
                putExtra(INTENT_EXTRA_ALARM_TYPE, alarmMessage.alarmType)
                putExtra(INTENT_EXTRA_ITEM_ID, alarmMessage.itemId)
                putExtra(INTENT_EXTRA_ALARM_TIMESTAMP, alarmMessage.alarmTimestamp)
            }
            //添加数据库
            if (!alarmRepository.isAlarmMessageDataExists(alarmMessage.hashcode)) {
                alarmRepository.insertAlarmMessageData(
                    AlarmMessageData.convertAlarmMessageToData(
                        alarmMessage
                    )
                )
            } else {
                alarmMessage.let {
                    alarmRepository.updateAlarmMessageDataByHashcode(
                        it.hashcode,
                        it.message,
                        it.alarmTimestamp
                    )
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmMessage.alarmTimestamp,
                PendingIntent.getBroadcast(
                    context,
                    alarmMessage.hashcode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            setAlarmNotificationReceiver(true)
            setBootReceiver(true)

            Log.e(
                TAG,
                "Alarm set at ${alarmMessage.alarmTimestamp} | ${
                    formatTimestampToReadableString(
                        alarmMessage.alarmTimestamp,
                        DateFormatters.fullDate()
                    )
                }"
            )
            return FunctionResult.Success

        } catch (e: SecurityException) {
            Log.e(TAG, "AlarmScheduler: alarm schedule failed: ${e.stackTraceToString()}")
            return FunctionResult.Failure
        }
    }

    suspend fun cancelAlarm(itemId: Long, type: AlarmUtils.AlarmType): FunctionResult {
        try {
            val hashCode = AlarmUtils.createHashCodeForAlarm(itemId.toInt(), type)

            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    hashCode,
                    Intent(context, AlarmNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            alarmRepository.deleteAlarmMessageDataByHashcode(hashCode)

            Log.e(TAG, "alarm canceled, id=$hashCode")
            return FunctionResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "cancel failed: ${e.stackTraceToString()}")
            return FunctionResult.Failure
        }

    }

    /**
     * 根据Alarm的Hashcode取消可能存在的Alarm
     *
     * @param alarmMessage
     * @return
     */
    suspend fun cancelAlarm(alarmMessage: AlarmMessage): FunctionResult {
        try {
            val hashCode = alarmMessage.hashcode

            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    hashCode,
                    Intent(context, AlarmNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            alarmRepository.deleteAlarmMessageDataByHashcode(hashCode)

            if (alarmRepository.isDbEmpty()) {
                setAlarmNotificationReceiver(false)
                setBootReceiver(false)
            }

            Log.e(TAG, "alarm canceled, id=$hashCode")
            return FunctionResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "cancel failed: ${e.stackTraceToString()}")
            return FunctionResult.Failure
        }

    }

    private fun setBootReceiver(isEnabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            bootReceiver,
            if (isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Log.e(TAG, "AlarmScheduler: boot receiver enable state has been set to $isEnabled")
    }

    private fun setAlarmNotificationReceiver(isEnabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            alarmNotificationReceiver,
            if (isEnabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Log.e(
            TAG,
            "AlarmScheduler: alarm notification receiver enable state has been set to $isEnabled"
        )
    }

    //数据库操作
    suspend fun isAlarmMessageDataExists(hashCode: Int): Boolean =
        alarmRepository.isAlarmMessageDataExists(hashCode)

    suspend fun getAlarmMessageData(hashcode: Int) =
        alarmRepository.getAlarmMessageDataByHashcode(hashcode)

}