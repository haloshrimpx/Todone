package com.sijayyx.todone.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.NONE_STRING

@Entity(tableName = "checklists")
data class ChecklistData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checklistName: String = EMPTY_STRING,
    val createTime: Long = System.currentTimeMillis(),
    val isHide: Boolean = true,
    val repeatPeriod: String = NONE_STRING,
    val repeatNum: Int = 0,
    val remindTimestamp: Long = -1,
    val color: String = NONE_STRING,
    val reminderWorkerId: String = EMPTY_STRING,
    val repeatWorkerId: String = EMPTY_STRING
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistData::class,
            parentColumns = ["id"],
            childColumns = ["checklistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("checklistId")]
)
data class ChecklistItemData(
    @PrimaryKey(autoGenerate = true) val checklistItemId: Long = 0,
    val checklistId: Long,
    val content: String,
    val isDone: Boolean,
)


data class ChecklistWithDatas(
    @Embedded
    val checklist: ChecklistData,

    @Relation(parentColumn = "id", entityColumn = "checklistId")
    val checklistItems: List<ChecklistItemData>
)



