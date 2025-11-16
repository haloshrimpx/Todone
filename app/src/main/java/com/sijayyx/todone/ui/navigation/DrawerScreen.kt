package com.sijayyx.todone.ui.navigation

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.composables.EditTodoListDialog
import com.sijayyx.todone.utils.DEFAULT_LIST_NAME
import com.sijayyx.todone.data.TodoListData
import com.sijayyx.todone.ui.about.AboutDialog
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.NONE_STRING
import com.sijayyx.todone.utils.TAG
import kotlinx.coroutines.launch

@Composable
fun DrawerScreen(
    closeDraw: () -> Unit,
    navControllerProvider: () -> NavController,
    modifier: Modifier = Modifier,
    viewModel: DrawerScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val uiState = viewModel.uiState.collectAsState()
    val customLists = viewModel.customLists.collectAsState().value

    var isShowingListAddDialog by rememberSaveable { mutableStateOf(false) }
    var isShowingAboutDialog by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val currentBackStackEntry = navControllerProvider().currentBackStackEntryAsState().value
//        remember(navController) { navController.currentBackStackEntry?.destination?.route }

    Log.e(TAG, "DrawerScreen composed!")

    DrawerContent(
        getMainScreenSelectedState = {

            it == currentBackStackEntry?.destination?.route
        },
        getListScreenSelectedState = {
            val argumentId = currentBackStackEntry?.arguments?.getLong("listId")

            Log.e(TAG, "Argument id = $argumentId, list id = $it")

            it == argumentId
        },
        updateSelectedId = { viewModel.updateSelectedListId(it) },
        selectedListId = uiState.value.selectedListId,
        initialListValue = uiState.value.listName,
        initialListIcon = uiState.value.listIcon,
        initialListColor = uiState.value.listIcon,
        onDismiss = { isShowingListAddDialog = false },
        onAddNewList = { isShowingListAddDialog = true },
        onConfirmAdd = { name, color, icon ->
            coroutineScope.launch {
                val targetId = viewModel.confirmAddList(name, color, icon)

                //创建后导航到新的列表
                navControllerProvider().navigate(
                    ScreenNavDestination.TodoSingleList.createRoute(
                        targetId,
                    )
                ) {
                    popUpTo(navControllerProvider().graph.findStartDestination().id) {
                        saveState = true
                    }
                }
                viewModel.resetUiState()
            }

        },
        navController = navControllerProvider(),
        closeDraw = closeDraw,
        isShowingListAddDialog = isShowingListAddDialog,
        customLists = customLists,
        onEditList = {
//            viewModel.setEditInputState()
            isShowingListAddDialog = true
            viewModel.editList(it)
        },
        isShowingAboutDialog = isShowingAboutDialog,
        onAboutDialogDismiss = { isShowingAboutDialog = false },
        showAboutDialog = {
            isShowingAboutDialog = true
            closeDraw()
        }
    )
}

@Composable
fun DrawerContent(
    getMainScreenSelectedState: (String) -> Boolean,
    getListScreenSelectedState: (Long) -> Boolean,
    isShowingAboutDialog: Boolean,
    showAboutDialog: () -> Unit,
    onAboutDialogDismiss: () -> Unit,
    selectedListId: Long,
    updateSelectedId: (Long) -> Unit,
    initialListValue: String,
    initialListColor: String,
    initialListIcon: String,
    onDismiss: () -> Unit,
    onConfirmAdd: (name: String, color: String, icon: String) -> Unit,
    navController: NavController,
    closeDraw: () -> Unit,
    isShowingListAddDialog: Boolean,
    onAddNewList: () -> Unit,
    onEditList: (Long) -> Unit,
    modifier: Modifier = Modifier,
    customLists: List<TodoListData> = listOf(),
) {
    if (isShowingListAddDialog) {
        EditTodoListDialog(
            dialogTitle = stringResource(R.string.dialog_title_add_new_list),
            initialTextValue = initialListValue,
            onDismiss = onDismiss,
            onConfirm = { name, color, icon ->
                onConfirmAdd(name, color, icon)
            },
            initialColor = initialListColor,
            initialIcon = initialListIcon,
        )
    }

    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp),

        ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.W300
                )
            }


            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            DrawerNavigationButton(
                leadingIcon = Icons.Filled.WbSunny, label = stringResource(R.string.title_today),
                onClick = {
                    closeDraw()
                    updateSelectedId(-1)

                    val route = ScreenNavDestination.TodoToday.route
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                selected = getMainScreenSelectedState(ScreenNavDestination.TodoToday.startRoute)
            )
            DrawerNavigationButton(
                leadingIcon = Icons.Filled.Today, label = stringResource(R.string.title_todo),
                onClick = {
                    closeDraw()
                    updateSelectedId(-1)

                    val route = ScreenNavDestination.Todo.createRoute()

                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                selected = getMainScreenSelectedState(ScreenNavDestination.Todo.startRoute)
            )
            DrawerNavigationButton(
                leadingIcon = Icons.Filled.Checklist,
                label = stringResource(R.string.title_checklists),
                onClick = {
                    closeDraw()
                    updateSelectedId(-1)

                    val route = ScreenNavDestination.Checklists.route
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                selected = getMainScreenSelectedState(ScreenNavDestination.Checklists.startRoute)
            )
            DrawerNavigationButton(
                leadingIcon = Icons.Filled.NoteAlt,
                label = stringResource(R.string.title_notes),
                onClick = {
                    closeDraw()
                    updateSelectedId(-1)

                    val route = ScreenNavDestination.Notes.route

                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                selected = getMainScreenSelectedState(ScreenNavDestination.Notes.startRoute)
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            //自定义Lists
            Column {
                customLists.filter { list ->
                    list.listName != DEFAULT_LIST_NAME
                }.forEach { list ->
                    key(list.id) {
                        DrawerNavigationClickableButton(
                            id = list.id,
                            onLongClick = {
                                onEditList(it)
                                closeDraw()
                            },
                            leadingIcon = Icons.AutoMirrored.Filled.List,
                            leadingEmoji = list.listIcon,
                            label = list.listName,
                            onClick = {
                                updateSelectedId(list.id)
                                navController.navigate(
                                    ScreenNavDestination.TodoSingleList.createRoute(listId = list.id)
                                ) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                                closeDraw()
                            },
                            selected = getListScreenSelectedState(list.id),
                        )
                    }
                }
            }

            DrawerNavigationButton(
                leadingIcon = Icons.Filled.Add,
                label = stringResource(R.string.add_list),
                onClick = {
                    onAddNewList()
                    closeDraw()
                }
            )

            Spacer(Modifier.weight(1f))
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            DrawerNavigationButton(
                leadingIcon = Icons.Filled.Settings,
                label = stringResource(R.string.title_settings),
                onClick = {
                    closeDraw()
                    navController.navigate(ScreenNavDestination.Settings.route)
                }
            )
            DrawerNavigationButton(
                leadingIcon = Icons.Filled.Info,
                label = stringResource(R.string.title_about),
                onClick = showAboutDialog
            )
            Spacer(Modifier.padding(vertical = 8.dp))
        }
    }

    if (isShowingAboutDialog) {
        AboutDialog(onDismiss = onAboutDialogDismiss)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerNavigationButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingEmoji: String? = null,
    textColor: Color? = null,
    selected: Boolean = false,
) {

    NavigationDrawerItem(
        label = { Text(text = label) },
        selected = selected,
        icon = {
            if (leadingEmoji != null && leadingEmoji != NONE_STRING)
                Text(
                    leadingEmoji,
                    textAlign = TextAlign.Center,

                    modifier = Modifier.size(24.dp)
                )
            else if (leadingIcon != null)
                Icon(leadingIcon, contentDescription = null)

        },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedTextColor = textColor ?: MaterialTheme.colorScheme.onBackground,
        ),
        onClick = onClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigationDrawerClickableItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(),
) {
    Surface(
        shape = RoundedCornerShape(100),
        color = colors.containerColor(selected).value,
        modifier =
            modifier
                .semantics { role = Role.Tab }
                .heightIn(min = 56.dp)
                .combinedClickable(
//                    onLongClick = onLongClick,
                    onClick = onClick
                )
                .fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val iconColor = colors.iconColor(selected).value
                CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
                Spacer(Modifier.width(12.dp))
            }
            Box(Modifier.weight(1f)) {
                val labelColor = colors.textColor(selected).value
                CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                val badgeColor = colors.badgeColor(selected).value
                CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DrawerNavigationClickableButton(
    id: Long,
    label: String,
    onClick: () -> Unit,
    onLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingEmoji: String? = null,
    textColor: Color? = null,
    selected: Boolean = false,
    trailingContent: String? = null,
) {
    NavigationDrawerClickableItem(
        label = {
            Row {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f)
                )
                if (trailingContent != null)
                    Text(text = trailingContent)
            }

        },
        selected = selected,
        icon = {
            if (leadingIcon != null && leadingEmoji == NONE_STRING)
                Icon(leadingIcon, contentDescription = null)
            else if (leadingEmoji != null)
                Text(leadingEmoji)
        },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedTextColor = textColor ?: MaterialTheme.colorScheme.onBackground,
        ),
        onClick = onClick,
        onLongClick = { onLongClick(id) },
        modifier = modifier
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true
)
@Composable
fun NavigationClickableItemPreview() {
    TodoListTheme {
        Column {
            DrawerNavigationClickableButton(
                label = "Test",
                onClick = {},
                onLongClick = {},
                id = 1,
                selected = false
            )
        }

    }
}

@Preview
@Composable
fun DrawerContentPreview() {
    TodoListTheme {
        DrawerContent(
            initialListValue = "",
            onDismiss = {},
            onConfirmAdd = { p1, p2, p3 -> },
            navController = rememberNavController(),
            closeDraw = {},
            isShowingListAddDialog = false,
            onAddNewList = {},
            onEditList = {},
            selectedListId = 0,
            updateSelectedId = {},
            initialListColor = NONE_STRING,
            initialListIcon = NONE_STRING,
            isShowingAboutDialog = false,
            showAboutDialog = {},
            onAboutDialogDismiss = {},
            getMainScreenSelectedState = { false },
            getListScreenSelectedState = { false },
            customLists = listOf(
                TodoListData(
                    listName = "Test",
                ),
                TodoListData(
                    listName = "Goo",
                )
            )
        )
    }
}
