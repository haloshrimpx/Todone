package com.sijayyx.todone.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(noteData: NoteData): Long

    @Delete
    suspend fun deleteNote(noteData: NoteData)

    @Update
    suspend fun updateNote(noteData: NoteData)

    @Query("SELECT * FROM notes ORDER BY createdAt ASC")
    fun getAllNotes(): Flow<List<NoteData>>

    @Query("SELECT * FROM notes WHERE id=:id")
    fun getNoteDataFlowById(id: Long): Flow<NoteData?>

    @Query("SELECT * FROM notes WHERE id=:id")
    suspend fun getNoteDataById(id: Long): NoteData?

    @Query("DELETE FROM notes WHERE id=:id")
    suspend fun deleteNoteDataById(id: Long)

    @Query("DELETE FROM notes WHERE isHide=1")
    suspend fun deleteHiddenNotes()

    @Query("UPDATE notes SET isHide=:isHide WHERE id=:id")
    suspend fun setNoteHideState(id: Long, isHide : Boolean)
}