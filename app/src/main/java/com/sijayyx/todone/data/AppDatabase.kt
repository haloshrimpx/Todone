package com.sijayyx.todone.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 1,
    exportSchema = false,
    entities = [ChecklistData::class, ChecklistItemData::class, TodoListData::class,
        TodoItemData::class, AlarmMessageData::class, NoteData::class]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun todoListDao(): TodoListDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun checklistBackgroundDao(): ChecklistBackgroundDao
    abstract fun alarmMessageDao(): AlarmMessageDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}