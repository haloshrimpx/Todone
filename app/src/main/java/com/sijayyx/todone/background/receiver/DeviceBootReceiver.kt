package com.sijayyx.todone.background.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED" && context != null) {
            val pendingResult = goAsync()
            val alarmScheduler = AlarmScheduler(context)

            try {
                CoroutineScope(Dispatchers.IO).launch {
                    alarmScheduler.restoreFromAlarmDb()
                }
            } catch (e: Exception) {
                Log.e(TAG, "DeviceBootReceiver: restore alarms failed, ${e.stackTraceToString()}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}