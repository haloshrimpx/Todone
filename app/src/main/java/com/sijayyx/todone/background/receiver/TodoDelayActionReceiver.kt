package com.sijayyx.todone.background.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.repository.TodoListRepository
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.INTENT_EXTRA_ITEM_ID
import com.sijayyx.todone.utils.INTENT_EXTRA_NOTIFICATION_ID
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.formatTimestampToReadableString
import com.sijayyx.todone.utils.timeAfterOfLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoDelayActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //点按Todo通知上的"DELAY", 将通知时间推迟到明天，并重新设置WorkManager的Work, 和TodoRepository的一样

        val pendingIntent = goAsync()

        try {
            context?.let {
                val todoId = intent?.getLongExtra(INTENT_EXTRA_ITEM_ID, -1) ?: -1
                val alarmTimestamp = intent?.getLongExtra(INTENT_EXTRA_ALARM_TIMESTAMP, -1) ?: -1
                val notificationId = intent?.getIntExtra(INTENT_EXTRA_NOTIFICATION_ID, -1) ?: -1

                if (todoId < 0 || alarmTimestamp < 0) {
                    Log.e(
                        TAG,
                        "TodoDelayActionReceiver: No valid todoId $todoId or alarmTimestamp $alarmTimestamp in the intent!!!"
                    )
                    return
                }

                //删除通知
                if (notificationId > 0) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    notificationManager.cancel(notificationId)
                }

                val todoRepository = TodoListRepository(
                    workManager = WorkManager.getInstance(it),
                    todoListDao = AppDatabase.getDatabase(it).todoListDao(),
                    alarmScheduler = AlarmScheduler(it)
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val todoItemData = todoRepository.getTodoItemDataById(todoId)
                        ?: throw Exception("TodoDelayActionReceiver: No valid todo item data for $todoId!!!")

                    val alarmTimestampTomorrow = timeAfterOfLocalDate(
                        dateLocal = todoItemData.reminderStamp,
                        days = 1
                    )

                    todoRepository.updateTodoItemData(todoItemData.copy(reminderStamp = alarmTimestampTomorrow))

                    Log.e(
                        TAG,
                        "TodoDelayActionReceiver: Delay action for todo item $todoId success, will remind at ${
                            formatTimestampToReadableString(
                                alarmTimestampTomorrow,
                                DateFormatters.fullDate()
                            )
                        }"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TodoDelayActionReceiver: Delay action failed!!! ${e.stackTraceToString()}")
        } finally {
            pendingIntent.finish()
        }
    }
}