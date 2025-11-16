package com.sijayyx.todone.data.repository

import android.util.Log
import com.sijayyx.todone.data.NoteDao
import com.sijayyx.todone.data.NoteData
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoteRepository(private val noteDao: NoteDao) {
    private val _deleteStagingNotes = MutableStateFlow(setOf<Long>())
    val deleteStagingNotes = _deleteStagingNotes.asStateFlow()

    fun addHiddenNote(id: Long) {
        _deleteStagingNotes.value = _deleteStagingNotes.value.plus(id)
        Log.e(TAG, "NoteRepository: Add note $id to notesToDelete")
    }

    /**
     * 批量添加
     *
     * @param notes
     */
    fun addHiddenNotes(notes: Collection<Long>) {
        _deleteStagingNotes.value = _deleteStagingNotes.value.plus(notes)
    }

    fun removeHiddenNote(id: Long) {
        _deleteStagingNotes.value = _deleteStagingNotes.value.minus(id)
        Log.e(TAG, "NoteRepository: Remove note $id to notesToDelete")
    }

    fun clearHiddenNotes() {
        _deleteStagingNotes.value = setOf()
        Log.e(TAG, "NoteRepository: Clear notesToDelete")
    }

    suspend fun insertNote(noteData: NoteData) = noteDao.insertNote(noteData)

    suspend fun deleteNote(noteData: NoteData) = noteDao.deleteNote(noteData)

    suspend fun updateNote(noteData: NoteData) {
        noteDao.updateNote(noteData)

        Log.e(TAG, "NoteRepository: Updated note data ${noteData.id}, isHide = ${noteData.isHide}")
    }

    fun getAllNotes() = noteDao.getAllNotes()

    suspend fun getNoteDataById(id: Long) = noteDao.getNoteDataById(id)

    fun getNoteDataFlowById(id: Long) = noteDao.getNoteDataFlowById(id)

    suspend fun deleteNoteDataById(id: Long) = noteDao.deleteNoteDataById(id)

    suspend fun deleteHiddenNotes() = noteDao.deleteHiddenNotes()

    suspend fun setNoteHideState(id: Long, isHide: Boolean) = noteDao.setNoteHideState(id, isHide)
}