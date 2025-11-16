package com.sijayyx.todone.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao : ChecklistBackgroundDao {
    @Insert
    suspend fun insertChecklistData(checklistData: ChecklistData): Long

    @Update
    suspend fun updateChecklistData(checklistData: ChecklistData)

    @Delete
    suspend fun deleteChecklistData(checklistData: ChecklistData)

    @Insert
    suspend fun insertChecklistItem(checklistItemData: ChecklistItemData): Long

    @Update
    suspend fun updateChecklistItem(checklistItemData: ChecklistItemData)

    @Query("UPDATE checklist_items SET isDone = :isDone WHERE checklistItemId = :id")
    suspend fun updateChecklistItemDoneById(id: Long, isDone: Boolean)

    @Query("UPDATE checklists SET isHide=:isHide WHERE id=:id")
    suspend fun updateChecklistHiddenState(id: Long, isHide: Boolean)

    @Query("UPDATE checklist_items SET content = :content WHERE checklistItemId = :id")
    suspend fun updateChecklistItemContentById(id: Long, content: String)

    @Query("DELETE FROM checklists WHERE isHide = 1")
    suspend fun clearHiddenChecklists()

    @Delete
    suspend fun deleteChecklistItem(checklistItemData: ChecklistItemData)

    @Query("DELETE FROM checklist_items WHERE checklistItemId = :id")
    suspend fun deleteChecklistItemById(id: Long)

    @Query("DELETE FROM checklists WHERE id =:listId")
    suspend fun deleteChecklistById(listId: Long)

    @Query("SELECT * FROM checklist_items WHERE checklistId = :listId ORDER BY checklistItemId ASC")
    fun getChecklistItemByListId(listId: Long): Flow<List<ChecklistItemData>>

    @Query("SELECT * FROM checklist_items WHERE checklistItemId = :id")
    suspend fun getChecklistItemById(id: Long): ChecklistItemData?

    @Query("SELECT * FROM checklists WHERE id = :listId")
    suspend fun getChecklistDataByListId(listId: Long): ChecklistData?

    @Transaction
    @Query("SELECT * FROM checklists WHERE id = :listId")
    fun getSingleChecklistByListId(listId: Long): Flow<ChecklistWithDatas?>

    @Query(
        "SELECT * FROM checklists " +
                "LEFT JOIN checklist_items ON checklist_items.checklistId = checklists.id " +
                "ORDER BY id ASC"
    )
    fun getAllChecklists(): Flow<Map<ChecklistData, List<ChecklistItemData>>>

    @Query("SELECT * FROM checklist_items ORDER BY checklistItemId ASC")
    fun getAllChecklistItems(): Flow<List<ChecklistItemData>>
}
