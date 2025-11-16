package com.sijayyx.todone.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.data.NoteData
import com.sijayyx.todone.data.repository.NoteRepository
import com.sijayyx.todone.data.repository.UserSettingsRepository
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesScreenViewModel(
    private val noteRepository: NoteRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val deleteStagingNotes = noteRepository.deleteStagingNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = setOf()
    )

    val noteList: StateFlow<List<NoteData>> = noteRepository.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf()
    )

    val isGridView = userSettingsRepository.isNotesViewGrid.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun deleteHiddenNotes() {
        viewModelScope.launch {
            noteRepository.clearHiddenNotes()
            noteRepository.deleteHiddenNotes()
        }
    }

    fun setNotesVisible() {
        viewModelScope.launch {
            noteRepository.deleteStagingNotes.value.forEach {
                noteRepository.setNoteHideState(it, false)
            }
            Log.e(TAG, "NotesScreenViewModel: Restored ${noteRepository.deleteStagingNotes.value.size} notes.")
            noteRepository.clearHiddenNotes()

        }
    }

    fun setNotesHide(dataIds: Collection<Long>) {
        viewModelScope.launch {
            noteRepository.addHiddenNotes(dataIds)

            dataIds.forEach {
                noteRepository.setNoteHideState(it, true)
            }
        }
    }

    fun setViewMode(isGridView: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setNoteViewMode(isGridView)
        }
    }
}