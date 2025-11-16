package com.sijayyx.todone.background.works

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.ChecklistBackgroundDao
import com.sijayyx.todone.data.ChecklistDao
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.KEY_REPEAT_NUM
import com.sijayyx.todone.utils.KEY_REPEAT_PERIOD
import com.sijayyx.todone.utils.KEY_REPEAT_TIME
import com.sijayyx.todone.utils.MINUTES_BEFORE_ALARM
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.calculateMinutesDifferenceFromNow
import com.sijayyx.todone.utils.calculateNextTimeToRepeat
import com.sijayyx.todone.utils.timeBeforeOfLocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChainRepeatWorkerTest {
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
    fun test_chain_repeat_worker_is_working_properly() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

//        val testChecklistData = ChecklistData(
//            checklistName = "New Checklist",
//            createTime = System.currentTimeMillis(),
//            repeatPeriod = RepeatPeriod.Day.content,
//            repeatNum = 1,
//            remindTimestamp = -1,
//            color = NONE_STRING,
//            reminderWorkerId = EMPTY_STRING,
//            repeatWorkerId = EMPTY_STRING
//        )
//
//        val newChecklistId = checklistDao.insertChecklistData(testChecklistData)
//
//        val checklistItemData = ChecklistItemData(
//            checklistId = newChecklistId,
//            content = "TEST",
//            isDone = true
//        )
//
//        val newItemId = checklistDao.insertChecklistItem(checklistItemData)

        val currentTime = System.currentTimeMillis()

        val workData = Data.Builder()
            .putInt(KEY_REPEAT_NUM, 1)
            .putString(KEY_REPEAT_PERIOD, RepeatPeriod.Day.content)
            .putLong(KEY_REPEAT_TIME, currentTime).build()


        val nextTime =
            calculateNextTimeToRepeat(currentTime, 1, RepeatPeriod.Day.content)
        val delayTillNextRemind = calculateMinutesDifferenceFromNow(
            timeBeforeOfLocalDate(nextTime, minutes = MINUTES_BEFORE_ALARM)
        )

        val chainRepeatWorker =
            TestListenableWorkerBuilder<ChainRepeatTestWorker>(context)
                .setInputData(workData)
                .build()

        val result = chainRepeatWorker.doWork()

        val outputData = result.outputData
        val finalChecklistItemData =
            checklistDao.getChecklistItemById(outputData.getLong("KEY_CHECKLIST_ITEM_ID", -1))!!

        assert(result is ListenableWorker.Result.Success)
        assert(outputData.getLong(KEY_REPEAT_TIME, -1) == nextTime)
        assert(finalChecklistItemData.content == "TEST")
    }

    @Test
    fun test_if_failure_when_input_data_invalid() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val currentTime = System.currentTimeMillis()

        val workData = Data.Builder()
            .putInt(KEY_REPEAT_NUM, -1)
            .putString(KEY_REPEAT_PERIOD, RepeatPeriod.Day.content)
            .putLong(KEY_REPEAT_TIME, currentTime).build()

        val chainRepeatWorker =
            TestListenableWorkerBuilder<ChainRepeatWorker>(context)
                .setInputData(workData)
                .build()

        val result = chainRepeatWorker.doWork()

        assert(result is ListenableWorker.Result.Failure)
    }

    @Test
    fun test_is_checklist_database_work_correctly() = runTest {
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

        val checklistItemData = ChecklistItemData(
            checklistId = 1,
            content = "TEST",
            isDone = true
        )

        checklistDao.insertChecklistData(testChecklistData)
        checklistDao.insertChecklistItem(checklistItemData)
        checklistBackgroundDao.resetItemsDoneState(1)

        val finalChecklistData = checklistDao.getChecklistDataByListId(1)
        val finalChecklistItemData = checklistDao.getChecklistItemById(1)

        assert(finalChecklistData != null)
        assert(finalChecklistItemData != null)
        assert(!finalChecklistItemData!!.isDone)
    }

}