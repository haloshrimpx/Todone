package com.sijayyx.todone.background.works.todo

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.TodoItemData
import com.sijayyx.todone.data.TodoListDao
import com.sijayyx.todone.data.TodoListData
import com.sijayyx.todone.data.repository.TodoListRepository
import com.sijayyx.todone.ui.todo.TodoDisplayType
import com.sijayyx.todone.ui.todo.TodoScreen
import com.sijayyx.todone.ui.todo.TodoScreenViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

//    @get:Rule
//    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
//        *getAllDangerousPermissions()
//    )
//
//    private fun getAllDangerousPermissions(): Array<String> {
//        return arrayOf(
//            Manifest.permission.POST_NOTIFICATIONS,
//            Manifest.permission.SCHEDULE_EXACT_ALARM
//        )
//    }

    private lateinit var database: AppDatabase
    private lateinit var todoListRepository: TodoListRepository
    private lateinit var todoScreenViewModel: TodoScreenViewModel
    private lateinit var todoListDao: TodoListDao
    private lateinit var workManager: WorkManager
    private lateinit var alarmScheduler: AlarmScheduler

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        todoListDao = database.todoListDao()
        alarmScheduler = AlarmScheduler(context)
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        todoListRepository = TodoListRepository(
            workManager,
            todoListDao,
            alarmScheduler
        )

        todoScreenViewModel = TodoScreenViewModel(
            todoListRepository
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_todo_screen() {
        composeTestRule.setContent {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            TodoScreen(
                drawerState = drawerState,
                closeDraw = {},
                listIdToShow = -1,
                displayType = TodoDisplayType.All,
                openDraw = {},
                navController = rememberNavController(),
                permissionViewModel = viewModel(factory = AppViewModelProvider.factory)
            )
        }

        val inputText = "WCNM 114514"

        //添加
        composeTestRule.onNodeWithContentDescription("Todo add FAB").performClick()
        composeTestRule.onNodeWithContentDescription("Bottom Input Field").assertIsDisplayed()
            .performTextInput(inputText)
        composeTestRule.onNodeWithText(inputText).assertExists()

        composeTestRule.onNodeWithContentDescription("Select deadline").assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("Tomorrow").performClick()
        composeTestRule.onNodeWithContentDescription("Select reminder").assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("One hour later").performClick()

        composeTestRule.onNodeWithContentDescription("Confirm Input Button").assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithContentDescription("Bottom Input Bar")
                    .assertIsNotDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("In Progress").assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Deadline IconTextRow"))
            .assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Reminder IconTextRow"))
            .assertIsDisplayed()

        //修改
        val inputText2 = "Fuck your mom"

        composeTestRule.onNodeWithContentDescription("Todo Card $inputText").assertIsDisplayed()
            .performClick()
        composeTestRule.onNode(hasContentDescription("Bottom Input Field") and hasText(inputText))
            .assertIsDisplayed()
            .performTextClearance()
        composeTestRule.onNode(hasContentDescription("Bottom Input Field"))
            .performTextInput(inputText2)

        composeTestRule.onNodeWithText(inputText2).assertExists()
        composeTestRule.onNodeWithContentDescription("Select deadline").assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("None").performClick()
        composeTestRule.onNodeWithContentDescription("Select reminder").assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("None").performClick()
        composeTestRule.onNodeWithText("Deadline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reminder").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Confirm Input Button").assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithContentDescription("Bottom Input Bar")
                    .assertIsNotDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNode(hasContentDescription("Deadline IconTextRow"))
            .assertIsNotDisplayed()
        composeTestRule.onNode(hasContentDescription("Reminder IconTextRow"))
            .assertIsNotDisplayed()

        //删除
        composeTestRule.onNodeWithContentDescription("Todo Card $inputText2").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Todo Card $inputText2 Swipe")
            .performTouchInput {
                swipeLeft()
            }
        composeTestRule.waitUntilDoesNotExist(hasContentDescription("Todo Card $inputText2"), 3000)
    }

    @Test
    fun test_add_todo_list() {
        composeTestRule.setContent {
//            AppNavigationHost(
//                navController = rememberNavController(),
//                globalViewModel = viewModel(factory = AppViewModelProvider.factory)
//            )

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()
            TodoScreen(
                drawerState = drawerState,
                closeDraw = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                listIdToShow = -1,
                displayType = TodoDisplayType.All,
                openDraw = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                },
                navController = rememberNavController(),
                permissionViewModel = viewModel(factory = AppViewModelProvider.factory)
            )

            LaunchedEffect(drawerState) {
                if (drawerState.isClosed)
                    drawerState.open()
            }
        }
//        composeTestRule.onNodeWithContentDescription("Todo Screen -1 Drawer").performTouchInput {
//            swipeRight()
//        }

        composeTestRule.onNodeWithText("Add List").assertIsDisplayed()
            .performClick()

        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("Add List")
                    .assertIsNotDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithContentDescription("Todo List Dialog").assertIsDisplayed()

        val listInputName = "Test List"
        composeTestRule.onNodeWithContentDescription("Add List Input Field").performTextClearance()
        composeTestRule.onNodeWithContentDescription("Add List Input Field")
            .performTextInput(listInputName)

        composeTestRule.onNodeWithText(listInputName).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add List Cancel Button").performClick()
        composeTestRule.onNodeWithContentDescription("Todo List Dialog").assertIsNotDisplayed()

    }

    @Test
    fun test_modify_todo_item_list() {
        val listName = "Test List"
        val itemContent = "HAHAHA"

        runBlocking {
            val todoList = TodoListData(
                listName = listName
            )
            val todoListId = todoListDao.insertTodoListData(todoList)

            val todoItem = TodoItemData(
                todoListId = todoListId,
                content = itemContent
            )

            val todoItemId = todoListDao.insertTodoItemData(todoItem)
        }

        composeTestRule.setContent {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            TodoScreen(
                drawerState = drawerState,
                closeDraw = {},
                listIdToShow = -1,
                displayType = TodoDisplayType.All,
                openDraw = {},
                navController = rememberNavController(),
                viewModel = todoScreenViewModel,
                permissionViewModel = viewModel(factory = AppViewModelProvider.factory)
            )
        }

        //改成默认列表
        composeTestRule.onNodeWithContentDescription("Todo Card $itemContent").assertIsDisplayed()
            .performClick()
        composeTestRule.onNode(hasContentDescription("Bottom Input Field") and hasText(itemContent))
            .assertIsDisplayed()
        composeTestRule.onNode(hasText(listName) and hasClickAction())
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("None").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Plan").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Confirm Input Button").assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("In Progress").assertIsDisplayed()

        //改成指定列表
        composeTestRule.onNodeWithContentDescription("Todo Card $itemContent").assertIsDisplayed()
            .performClick()
        composeTestRule.onNode(hasContentDescription("Bottom Input Field") and hasText(itemContent))
            .assertIsDisplayed()
        composeTestRule.onNode(hasText("Plan") and hasClickAction())
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNode(hasText(listName) and hasClickAction()).assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithContentDescription("Confirm Input Button").assertIsDisplayed()
            .performClick()

//        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText(listName) and hasNoClickAction()).assertIsDisplayed()

    }
}