package com.sijayyx.todone.ui.navigation

import androidx.navigation.NavController
import com.sijayyx.todone.ui.todo.TodoDisplayType
import com.sijayyx.todone.utils.UiEnterState

sealed class ScreenNavDestination(val route: String) {
    object Todo : ScreenNavDestination(route = "todo_all") {
        fun createRoute(
//            listId: Long? = -1,
//            displayType: TodoDisplayType = TodoDisplayType.All,
        ): String {
            return "todo_all"
        }

        const val startRoute = "todo"
    }

    object TodoSingleList :
        ScreenNavDestination(route = "todo_single_list") {
        fun createRoute(
            listId: Long? = -1
        ): String {
            return "single_list/${listId}/${TodoDisplayType.SpecifiedList}"
        }

        const val startRoute = "single_list/{listId}/{displayType}"
    }

    object TodoToday : ScreenNavDestination(route = "todo_today") {
        const val startRoute = "today"
    }

    object Checklists : ScreenNavDestination(route = "checklists") {
        const val startRoute = "checklist_main"
    }

    object EditChecklist :
        ScreenNavDestination(route = "edit_checklist/{checklistId}/{enterState}") {
        fun createRoute(
            checklistId: Long = -1,
            enterState: UiEnterState = UiEnterState.None
        ): String {
            return "edit_checklist/${checklistId}/${enterState}"
        }
    }

    object Notes : ScreenNavDestination(route = "notes") {
        const val startRoute = "notes_main"
    }

    object NotesDetail : ScreenNavDestination(route = "notes_detail/{noteId}/{enterState}") {
        fun createRoute(id: Long = -1, enterState: UiEnterState = UiEnterState.None): String {
            return "notes_detail/${id}/${enterState}"
        }
    }

    object Settings : ScreenNavDestination(route = "settings")
}

fun NavController.navigate(destination: ScreenNavDestination) {
    this.navigate(destination.route)
}