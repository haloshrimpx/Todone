package com.sijayyx.todone

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sijayyx.todone.ui.checklists.ChecklistEditScreenViewModel
import com.sijayyx.todone.ui.checklists.ChecklistScreenViewModel
import com.sijayyx.todone.ui.navigation.DrawerScreenViewModel
import com.sijayyx.todone.ui.notes.NoteEditScreenViewModel
import com.sijayyx.todone.ui.notes.NotesScreenViewModel
import com.sijayyx.todone.ui.settings.SettingsScreenViewModel
import com.sijayyx.todone.ui.todo.TodoEditScreenViewModel
import com.sijayyx.todone.ui.todo.TodoScreenViewModel

object AppViewModelProvider {
    val factory = viewModelFactory {
        initializer {
            TodoScreenViewModel(
                todoListApplication().container.todoListRepository,
            )
        }

        initializer {
            TodoEditScreenViewModel()
        }

        initializer {
            ChecklistScreenViewModel(todoListApplication().container.checklistRepository)
        }

        initializer {
            ChecklistEditScreenViewModel(todoListApplication().container.checklistRepository)
        }

        initializer {
            DrawerScreenViewModel(todoListApplication().container.todoListRepository)
        }

        initializer {
            SettingsScreenViewModel(todoListApplication().container.userSettingsRepository)
        }

        initializer {
            GlobalViewModel(todoListApplication().container.userSettingsRepository)
        }

        initializer {
            NotesScreenViewModel(
                todoListApplication().container.noteRepository,
                todoListApplication().container.userSettingsRepository
            )
        }

        initializer {
            NoteEditScreenViewModel(todoListApplication().container.noteRepository)
        }
    }


}

fun CreationExtras.todoListApplication(): TodoListApplication {
    return (this[APPLICATION_KEY] as TodoListApplication)
}
