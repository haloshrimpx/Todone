package com.sijayyx.todone.background.receiver

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.sijayyx.todone.R
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.background.works.DeleteFiredAlarmWorker
import com.sijayyx.todone.ui.navigation.ScreenNavDestination
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_HASHCODE
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.INTENT_EXTRA_ALARM_TYPE
import com.sijayyx.todone.utils.INTENT_EXTRA_ITEM_ID
import com.sijayyx.todone.utils.INTENT_EXTRA_MESSAGE
import com.sijayyx.todone.utils.INTENT_EXTRA_NOTIFICATION_ID
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.NotificationUtils
import com.sijayyx.todone.utils.TAG

class AlarmNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //ItemId和AlarmType都不是发送通知的必须参数
        val alarmType = try {
            AlarmUtils.AlarmType.valueOf(
                intent?.getStringExtra(INTENT_EXTRA_ALARM_TYPE)
                    ?: AlarmUtils.AlarmType.None.content
            )
        } catch (e: IllegalArgumentException) {
            AlarmUtils.AlarmType.None
        }
        val message = intent?.getStringExtra(INTENT_EXTRA_MESSAGE) ?: return
        val alarmHashcode = intent.getIntExtra(INTENT_EXTRA_ALARM_HASHCODE, -1)
        val itemId = intent.getLongExtra(INTENT_EXTRA_ITEM_ID, -1)
        val alarmTimestamp = intent.getLongExtra(INTENT_EXTRA_ALARM_TIMESTAMP, -1)

        context?.let { it ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "AlarmNotificationReceiver: Permission denied!")
                    return
                }
            }

            fireNotification(
                context = it,
                hashcode = alarmHashcode,
                message = message,
                alarmTimestamp = alarmTimestamp,
                itemId = itemId,
                alarmType = alarmType
            )

            val workManager = WorkManager.getInstance(it)
            setDeleteAlarmWorker(workManager, alarmHashcode)
        }
    }

    private fun fireNotification(
        context: Context,
        hashcode: Int,
        message: String,
        alarmTimestamp: Long,
        itemId: Long,
        alarmType: AlarmUtils.AlarmType
    ) {
        // 创建 Deep Link Intent
        val screenRoute = when (alarmType) {
            AlarmUtils.AlarmType.None -> ScreenNavDestination.Todo.route
            AlarmUtils.AlarmType.TodoItem -> ScreenNavDestination.Todo.route
            AlarmUtils.AlarmType.Checklist -> ScreenNavDestination.Checklists.route
        }

        val deeplinkUri = "todoapp://notification?screen_route=$screenRoute".toUri()

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            deeplinkUri,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // 创建 PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            hashcode,
            deepLinkIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = hashcode

        val builder = if (alarmType == AlarmUtils.AlarmType.TodoItem) {

            val doneActionIntent = Intent(
                context, TodoDoneActionReceiver::class.java
            ).apply {
                putExtra(INTENT_EXTRA_ITEM_ID, itemId)
                putExtra(INTENT_EXTRA_NOTIFICATION_ID, notificationId)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                context,
                hashcode,
                doneActionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val delayActionIntent = Intent(
                context, TodoDelayActionReceiver::class.java
            ).apply {
                putExtra(INTENT_EXTRA_ITEM_ID, itemId)
                putExtra(INTENT_EXTRA_ALARM_TIMESTAMP, alarmTimestamp)
                putExtra(INTENT_EXTRA_NOTIFICATION_ID, notificationId)
            }
            val delayPendingIntent = PendingIntent.getBroadcast(
                context,
                hashcode,
                delayActionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            NotificationCompat.Builder(context, NotificationUtils.ALARM_CHANNEL_ID)
                .setContentTitle("Alarm Demo")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "DONE", donePendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "DELAY", delayPendingIntent)
                .setAutoCancel(true)

        } else {
            NotificationCompat.Builder(context, NotificationUtils.ALARM_CHANNEL_ID)
                .setContentTitle("Alarm Demo")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        //传入ID, 在Action中取消
        notificationManager.notify(notificationId, builder.build())
    }

    /**
     * 完成后移除数据库中的Alarm数据
     *
     * @param workManager
     * @param alarmHashcode
     */
    private fun setDeleteAlarmWorker(workManager: WorkManager, alarmHashcode: Int) {
        val inputData = Data.Builder().putInt(KEY_ALARM_HASHCODE, alarmHashcode).build()

        val deleteFiredAlarmWorker = OneTimeWorkRequestBuilder<DeleteFiredAlarmWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(inputData)
            .build()

        workManager.enqueue(deleteFiredAlarmWorker)

        Log.e(TAG, "AlarmNotificationReceiver: Delete alarm worker enqueued!")
    }
}