package com.sijayyx.todone

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import com.sijayyx.todone.background.alarm.AlarmScheduler
import com.sijayyx.todone.utils.NotificationUtils
import com.sijayyx.todone.data.AppDatabase
import com.sijayyx.todone.data.repository.ChecklistRepository
import com.sijayyx.todone.data.repository.NoteRepository
import com.sijayyx.todone.data.repository.TodoListRepository
import com.sijayyx.todone.data.repository.UserSettingsRepository

private const val PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class TodoListApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()


        NotificationUtils.createNotificationChannel(this)

        container = AppDataContainer(this)
    }
}

interface AppContainer {
    val todoListRepository: TodoListRepository
    val checklistRepository: ChecklistRepository
    val userSettingsRepository: UserSettingsRepository
    val noteRepository: NoteRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    val alarmScheduler = AlarmScheduler(context)
    val workManager = WorkManager.getInstance(context)

    override val todoListRepository: TodoListRepository by lazy {
        TodoListRepository(
            workManager = workManager,
            todoListDao = AppDatabase.getDatabase(context).todoListDao(),
            alarmScheduler = alarmScheduler
        )
    }

    override val checklistRepository: ChecklistRepository by lazy {
        ChecklistRepository(
            workManager = workManager,
            checklistDao = AppDatabase.getDatabase(context).checklistDao(),
            alarmScheduler = alarmScheduler
        )
    }

    override val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(context.dataStore)
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(noteDao = AppDatabase.getDatabase(context).noteDao())
    }
}