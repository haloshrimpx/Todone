package com.sijayyx.todone.background.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.utils.INTENT_EXTRA_ITEM_ID
import com.sijayyx.todone.utils.INTENT_EXTRA_NOTIFICATION_ID
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoDoneActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //点按Todo类型通知上的"DONE"，启动这个广播接收器
        //获得PendingIntent传入的ID信息，将Todo的状态设置为已完成

        val pendingResult = goAsync()

        try {
            CoroutineScope(Dispatchers.IO).launch {
                context?.let {


                    //删除通知
                    val notificationId = intent?.getIntExtra(INTENT_EXTRA_NOTIFICATION_ID, -1) ?: -1
                    if (notificationId > 0) {
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        notificationManager.cancel(notificationId)
                    }

                    val todoId = intent?.getLongExtra(INTENT_EXTRA_ITEM_ID, -1)
                        ?: throw Exception("TodoDoneActionReceiver: No valid item id provided!!!")
                    val todoDao = AppDatabase.getDatabase(context).todoListDao()

                    todoDao.updateTodoItemDoneById(todoId, true)

                    Log.e(TAG, "TodoDoneActionReceiver: Todo item $todoId has set to done!")
                }
            }

        } catch (e: Exception) {
            Log.e(
                TAG,
                "TodoDoneActionReceiver: Todo item Done set FAILED! ${e.stackTraceToString()}"
            )
        } finally {
            pendingResult.finish()
        }

    }
}