package com.sijayyx.todone.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sijayyx.todone.GlobalViewModel
import com.sijayyx.todone.ui.checklists.ChecklistEditScreen
import com.sijayyx.todone.ui.checklists.ChecklistsScreen
import com.sijayyx.todone.ui.notes.NoteEditScreen
import com.sijayyx.todone.ui.notes.NotesScreen
import com.sijayyx.todone.ui.settings.SettingsScreen
import com.sijayyx.todone.ui.todo.TodoDisplayType
import com.sijayyx.todone.ui.todo.TodoScreen
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.UiEnterState
import kotlinx.coroutines.launch

const val uri = "todoapp://notification?"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavigationHost(
    navController: NavHostController,
    globalViewModel: GlobalViewModel,
    modifier: Modifier = Modifier,
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var isGestureEnable by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isGestureEnable,
        drawerContent = {
            DrawerScreen(
                closeDraw = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                navControllerProvider = { navController },
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = ScreenNavDestination.TodoToday.route
        ) {
            navigation(
                route = ScreenNavDestination.Todo.route,
                startDestination = ScreenNavDestination.Todo.startRoute,
            ) {
                //顶级目的地
                composable(
                    route = ScreenNavDestination.Todo.startRoute,
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = "${uri}screen_route=${ScreenNavDestination.Todo.route}"
                        }
                    ),
                ) { backStackEntry ->
                    TodoScreen(
                        setGestureEnable = { isGestureEnable = it },
                        listIdToShow = -1,
                        displayType = TodoDisplayType.All,
                        navController = navController,
                        openDraw = { coroutineScope.launch { drawerState.open() } },
                        permissionViewModel = globalViewModel
                    )
                }
            }

            navigation(
                route = ScreenNavDestination.TodoToday.route,
                startDestination = ScreenNavDestination.TodoToday.startRoute
            ) {
                composable(
                    route = ScreenNavDestination.TodoToday.startRoute,
                ) { backStackEntry ->
                    TodoScreen(
                        setGestureEnable = { isGestureEnable = it },

                        listIdToShow = -1,
                        displayType = TodoDisplayType.Today,
                        navController = navController,
                        openDraw = { coroutineScope.launch { drawerState.open() } },
                        permissionViewModel = globalViewModel
                    )
                }
            }

            navigation(
                route = ScreenNavDestination.TodoSingleList.route,
                startDestination = ScreenNavDestination.TodoSingleList.startRoute
            ) {
                composable(
                    route = ScreenNavDestination.TodoSingleList.startRoute,
                    arguments = listOf(
                        navArgument(name = "listId") {
                            type = NavType.LongType
                            defaultValue = -1
                        },
                    )
                ) { backStackEntry ->

                    val listId = backStackEntry.arguments?.getLong("listId")

                    TodoScreen(
                        setGestureEnable = { isGestureEnable = it },
                        listIdToShow = listId,
                        displayType = TodoDisplayType.SpecifiedList,
                        navController = navController,
                        openDraw = { coroutineScope.launch { drawerState.open() } },
                        permissionViewModel = globalViewModel
                    )
                }

            }

            navigation(
                route = ScreenNavDestination.Checklists.route,
                startDestination = ScreenNavDestination.Checklists.startRoute
            ) {
                composable(
                    route = ScreenNavDestination.Checklists.startRoute,
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern =
                                "${uri}screen_route=${ScreenNavDestination.Checklists.route}"
                        }
                    ),
                ) { backStackEntry ->
                    ChecklistsScreen(
                        setGestureEnable = { isGestureEnable = it },
                        navController = navController,
                        openDraw = { coroutineScope.launch { drawerState.open() } },
                        globalViewModel = globalViewModel
                    )
                }


                //次级目的地
                composable(
                    route = ScreenNavDestination.EditChecklist.route,
                    arguments = listOf(
                        navArgument(name = "checklistId") {
                            type = NavType.LongType
                        },
                        navArgument(name = "enterState") {
                            type = NavType.StringType
                            defaultValue = UiEnterState.None.toString()
                        }
                    ),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(400)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(400)
                        )
                    }
                ) { backStackEntry ->
                    val checklistId = backStackEntry.arguments?.getLong("checklistId")
                    val enterState =
                        UiEnterState.valueOf(
                            backStackEntry.arguments?.getString("enterState") ?: NONE_STRING
                        )

                    ChecklistEditScreen(
                        setGestureEnable = { isGestureEnable = it },
                        checklistId = checklistId,
                        checklistEditEnterState = enterState,
                        globalViewModel = globalViewModel,
                        onNavigateUp = {
                            isGestureEnable = true
                            navController.navigateUp()
                        }
                    )
                }

            }

            navigation(
                route = ScreenNavDestination.Notes.route,
                startDestination = ScreenNavDestination.Notes.startRoute,
            ) {
                composable(
                    route = ScreenNavDestination.Notes.startRoute,

                    ) {
                    NotesScreen(
                        setGestureEnable = { isGestureEnable = it },
                        openDraw = { coroutineScope.launch { drawerState.open() } },
                        navController = navController
                    )
                }

                composable(
                    route = ScreenNavDestination.NotesDetail.route,
                    arguments = listOf(
                        navArgument(name = "noteId") {
                            type = NavType.LongType
                        },
                        navArgument(name = "enterState") {
                            type = NavType.StringType
                            defaultValue = UiEnterState.None.toString()
                        }
                    ),
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(400)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(400)
                        )
                    }
                ) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getLong("noteId")
                    val enterState =
                        UiEnterState.valueOf(
                            backStackEntry.arguments?.getString("enterState") ?: NONE_STRING
                        )

                    NoteEditScreen(
                        setGestureEnable = { isGestureEnable = it },
                        noteId = noteId,
                        enterState = enterState,
                        onNavigateUp = {
                            isGestureEnable = true
                            navController.navigateUp()
                        }
                    )
                }
            }

            composable(
                route = ScreenNavDestination.Settings.route,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        tween(400)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        tween(400)
                    )
                }) {
                SettingsScreen(
                    setGestureEnable = { isGestureEnable = it },
                    navController = navController,
                    globalViewModel
                )
            }
        }
    }
}