package com.sijayyx.todone.background.works

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.sijayyx.todone.background.alarm.AlarmUtils
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.ChecklistBackgroundDao
import com.sijayyx.todone.data.ChecklistDao
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.KEY_ALARM_HASHCODE
import com.sijayyx.todone.utils.KEY_ALARM_MESSAGE
import com.sijayyx.todone.utils.KEY_ALARM_TIMESTAMP
import com.sijayyx.todone.utils.KEY_ITEM_ID
import com.sijayyx.todone.utils.KEY_REPEAT_NUM
import com.sijayyx.todone.utils.KEY_REPEAT_PERIOD
import com.sijayyx.todone.utils.KEY_WORKER_UUID
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.getDaysEndTimestampAfterNow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ChainReminderWorkerTest {
    private lateinit var database: AppDatabase
    private lateinit var checklistBackgroundDao: ChecklistBackgroundDao
    private lateinit var checklistDao: ChecklistDao
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        checklistBackgroundDao = database.checklistBackgroundDao()
        checklistDao = database.checklistDao()


        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun test_chain_reminder_worker_working_properly() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val testChecklistData = ChecklistData(
            checklistName = "New Checklist",
            createTime = System.currentTimeMillis(),
            repeatPeriod = RepeatPeriod.Day.content,
            repeatNum = 1,
            remindTimestamp = -1,
            color = NONE_STRING,
            reminderWorkerId = EMPTY_STRING,
            repeatWorkerId = EMPTY_STRING
        )

        val newChecklistId = checklistDao.insertChecklistData(testChecklistData)

        val checklistItemData = ChecklistItemData(
            checklistId = newChecklistId,
            content = "TEST",
            isDone = true
        )

        val newItemId = checklistDao.insertChecklistItem(checklistItemData)

        val alarmMessage = AlarmUtils.createAlarmMessage(
            1, AlarmUtils.AlarmType.Checklist,
            getDaysEndTimestampAfterNow(10),
            "Fuck you",
        )

        val inputData = Data.Builder().putInt(KEY_REPEAT_NUM, 1)
            .putString(KEY_REPEAT_PERIOD, RepeatPeriod.Day.content)
            .putLong(KEY_ITEM_ID, newChecklistId)
            .putInt(KEY_ALARM_HASHCODE, alarmMessage.hashcode)
            .putString(KEY_ALARM_MESSAGE, alarmMessage.message)
            .putLong(KEY_ALARM_TIMESTAMP, alarmMessage.alarmTimestamp)
            .build()

        val worker = TestListenableWorkerBuilder<ChainReminderWorker>(context)
            .setInputData(inputData).build()

        val result = worker.doWork()
        val outputData = result.outputData

//            .putLong(KEY_CHECKLIST_ID, checklistId)
//            .putString(KEY_WORKER_UUID, nextWorkUuid)
//            .putLong(KEY_ALARM_TIMESTAMP, nextRemindTime)

        val checklistId = outputData.getLong(KEY_ITEM_ID, -1)
        val newWorkerUuid = outputData.getString(KEY_WORKER_UUID) ?: NONE_STRING

        val finalChecklistData = checklistDao.getChecklistDataById(checklistId)

        assert(result is ListenableWorker.Result.Success)
        assert(checklistId == newChecklistId)
        assert(finalChecklistData?.reminderWorkerId == newWorkerUuid)
    }
}