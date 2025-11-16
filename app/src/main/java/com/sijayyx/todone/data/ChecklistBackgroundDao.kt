package com.sijayyx.todone.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ChecklistBackgroundDao {
    @Query("UPDATE checklist_items SET isDone = 0 WHERE checklistId = :listId")
    suspend fun resetItemsDoneState(listId: Long)

    @Query("UPDATE checklists SET reminderWorkerId = :workerId WHERE id = :listId")
    suspend fun setChecklistReminderWorkerId(listId: Long, workerId: String)

    @Query("UPDATE checklists SET repeatWorkerId = :workerId WHERE id = :listId")
    suspend fun setChecklistRepeatWorkerId(listId: Long,workerId: String)

    @Query("UPDATE checklists SET remindTimestamp = :remindTime WHERE id = :listId")
    suspend fun setChecklistRemindTime(listId: Long, remindTime: Long)

    @Query("SELECT * FROM checklists WHERE id = :listId")
    suspend fun getChecklistDataById(listId: Long): ChecklistData?

    @Query("SELECT EXISTS(SELECT 1 FROM checklists WHERE id = :id)")
    suspend fun isChecklistExists(id: Long): Boolean
}