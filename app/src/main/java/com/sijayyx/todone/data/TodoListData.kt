package com.sijayyx.todone.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.NONE_STRING


@Entity(tableName = "todo_lists")
data class TodoListData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listName: String = EMPTY_STRING,
    val listColor: String = NONE_STRING,
    val listIcon: String = NONE_STRING,
    val createAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "todo_list_items",
    foreignKeys = [
        ForeignKey(
            entity = TodoListData::class,
            parentColumns = ["id"],
            childColumns = ["todoListId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("todoListId")]
)
data class TodoItemData(
    @PrimaryKey(autoGenerate = true) val todoItemId: Long = 0,
    val todoListId: Long,
    val deadlineStamp: Long = -1,
    val reminderStamp: Long = -1,
    val content: String = "",
    val isDone: Boolean = false,
    val reminderWorkerId: String = EMPTY_STRING,//如果添加了worker调度提醒任务，就记录对应的workerId
    val createAt: Long = System.currentTimeMillis()
)