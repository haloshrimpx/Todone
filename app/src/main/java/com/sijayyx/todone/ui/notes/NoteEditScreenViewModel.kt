package com.sijayyx.todone.ui.notes

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.data.NoteData
import com.sijayyx.todone.data.repository.NoteRepository
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.formatTimestampToReadableString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteEditScreenViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun getNoteFlowById(id: Long) = noteRepository.getNoteDataFlowById(id)

    fun initializeNewNote() {
        viewModelScope.launch {
            val newData = NoteData()

            val newNoteId = noteRepository.insertNote(
                noteData = newData
            )

            _uiState.value = _uiState.value.copy(
                selectedNoteId = newNoteId,
                noteData = newData.copy(id = newNoteId)
            )
        }
    }

    fun getNoteDataById(id: Long) {
        viewModelScope.launch {
            getNoteFlowById(id).collect { noteData ->
                noteData?.let {
                    _uiState.value = _uiState.value.copy(
                        selectedNoteId = id,
                        noteData = it
                    )
                }
            }
        }
    }

    fun inputTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            noteData = uiState.value.noteData.copy(title = title)
        )
    }

    fun inputContent(content: String) {
        _uiState.value = _uiState.value.copy(
            noteData = uiState.value.noteData.copy(content = content)
        )
    }

    fun updateNoteData(callback: () -> Unit) {
        viewModelScope.launch {
            noteRepository.updateNote(
                uiState.value.noteData.copy(
                    id = uiState.value.selectedNoteId,
                    isHide = false
                )
            )
            callback()

            Log.e(TAG, "Note data ${uiState.value.selectedNoteId} is updated!")
        }
    }

    fun setNoteDataHide(callback: () -> Unit, isEmptyData: Boolean = false) {
        viewModelScope.launch {
            val data = uiState.value.noteData.copy(isHide = true)

            noteRepository.updateNote(data)

            if (!isEmptyData)
                noteRepository.addHiddenNote(data.id)

            callback()
            Log.e(TAG, "Hidden note data ${uiState.value.noteData.id}")
        }
    }

    fun shareNote(context: Context) {
        val data = uiState.value.noteData

        val createDate = formatTimestampToReadableString(
            data.createdAt,
            DateFormatters.yearWithShortMonthAndDay(context)
        )

        /**
         * 标题
         * 日期
         *
         * 内容
         */
        val result = "${data.title}\n$createDate\n\n${data.content}"

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, result)
            type = "text/plain"
        }

        context.startActivity(Intent.createChooser(shareIntent, null))

        Log.e(TAG, "Sharing data:\n$result")
    }
}

data class NoteEditScreenUiState(
    val selectedNoteId: Long = -1,
    val noteData: NoteData = NoteData()
)