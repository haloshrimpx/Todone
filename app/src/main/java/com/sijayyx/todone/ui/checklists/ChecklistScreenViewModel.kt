package com.sijayyx.todone.ui.checklists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.data.ChecklistData
import com.sijayyx.todone.data.ChecklistItemData
import com.sijayyx.todone.data.repository.ChecklistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChecklistScreenViewModel(
    private val checklistRepository: ChecklistRepository,
) : ViewModel() {

    val deleteStagingChecklists = checklistRepository.deleteStagingChecklists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = setOf()
    )

    val allChecklists: StateFlow<Map<ChecklistData, List<ChecklistItemData>>> =
        checklistRepository.getAllChecklists().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = mapOf()
        )

    fun canScheduleAlarms() = checklistRepository.canScheduleAlarms()

    fun updateItemDoneState(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            checklistRepository.updateChecklistItemDoneById(id, isDone)
        }
    }

    fun deleteHiddenChecklists() {
        viewModelScope.launch {
            checklistRepository.deleteHiddenChecklists()
            checklistRepository.clearHiddenStagingChecklists()
        }
    }

    fun deleteChecklistDataById(id: Long) {
        viewModelScope.launch {
            checklistRepository.deleteChecklistById(id)
        }
    }

    fun setChecklistsVisible() {
        viewModelScope.launch {
            checklistRepository.deleteStagingChecklists.value.forEach {
                checklistRepository.updateChecklistHiddenState(it, false)
            }
            checklistRepository.clearHiddenStagingChecklists()
        }
    }

    fun setChecklistsHidden(dataIds: Collection<Long>) {
        viewModelScope.launch {
            dataIds.forEach {
                checklistRepository.updateChecklistHiddenState(it, true)
            }
            checklistRepository.addHiddenChecklists(dataIds)
        }
    }

}
