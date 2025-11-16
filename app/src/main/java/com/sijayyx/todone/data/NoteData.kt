package com.sijayyx.todone.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sijayyx.todone.utils.EMPTY_STRING

@Entity(tableName = "notes")
data class NoteData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String = EMPTY_STRING,
    val content: String = EMPTY_STRING,
    val isHide: Boolean = true,
)