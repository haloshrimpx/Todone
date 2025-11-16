package com.sijayyx.todone.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sijayyx.todone.data.TodoListData
import com.sijayyx.todone.data.repository.TodoListRepository
import com.sijayyx.todone.utils.EMPTY_STRING
import com.sijayyx.todone.utils.NONE_STRING
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawerScreenViewModel(private val todoListRepository: TodoListRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DrawerContentUiState())
    val uiState = _uiState.asStateFlow()

    val customLists: StateFlow<List<TodoListData>> =
        todoListRepository.getAllTodoLists().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf()
        )

    suspend fun confirmAddList(listName: String, color: String, icon: String): Long {
        return todoListRepository.insertTodoListData(
            TodoListData(
                listName = todoListRepository.checkSameListName(listName),
                listColor = color,
                listIcon = icon
            )
        )
    }

    fun updateSelectedListId(id: Long) {
        _uiState.update { newState ->
            newState.copy(selectedListId = id)
        }
    }

    fun editList(id: Long) {
        viewModelScope.launch {
            val listData = todoListRepository.getTodoListDataById(id)

            if (listData != null) {
                _uiState.update { newState ->
                    newState.copy(
                        listName = listData.listName,
                        listColor = listData.listColor,
                        listIcon = listData.listIcon,
                        selectedListId = id
                    )
                }
            }
        }
    }

    fun resetUiState() {
        _uiState.value = DrawerContentUiState()
    }
}

data class DrawerContentUiState(
    val listName: String = EMPTY_STRING,
    val listColor: String = NONE_STRING,
    val listIcon: String = NONE_STRING,
    val selectedListId: Long = -1,
    val navigateTargetId: Long = -1
)

