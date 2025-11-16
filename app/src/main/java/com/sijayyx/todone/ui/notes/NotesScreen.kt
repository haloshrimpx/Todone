package com.sijayyx.todone.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.R
import com.sijayyx.todone.data.NoteData
import com.sijayyx.todone.ui.composables.DeleteConfirmDialog
import com.sijayyx.todone.ui.composables.EditableScreen
import com.sijayyx.todone.ui.composables.EditableScreenState
import com.sijayyx.todone.ui.composables.TopAppBarWithMenu
import com.sijayyx.todone.ui.composables.rememberEditableScreenState
import com.sijayyx.todone.ui.navigation.ScreenNavDestination
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.UiEnterState
import com.sijayyx.todone.utils.formatTimestampToReadableString
import com.sijayyx.todone.utils.getFirstLine
import com.sijayyx.todone.utils.removeFirstLine
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    setGestureEnable: (Boolean) -> Unit,
    openDraw: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NotesScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val deleteStagingNotes = viewModel.deleteStagingNotes.collectAsState().value

    val noteList = viewModel.noteList.collectAsState().value
    val isGridView = viewModel.isGridView.collectAsState().value
    val editableScreenState = rememberEditableScreenState(
        targetElementList = noteList.map { it.id }
    )

    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(deleteStagingNotes) {
        coroutineScope.launch {
            if (deleteStagingNotes.isNotEmpty()) {

                //销毁上一次出现的snackbar
                snackbarHostState.currentSnackbarData?.dismiss()

                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.items_deleted_successfully),
                    actionLabel = context.getString(R.string.undo),
                    duration = SnackbarDuration.Short,
                    withDismissAction = true
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> viewModel.setNotesVisible()
                    SnackbarResult.Dismissed -> viewModel.deleteHiddenNotes()
                }

            }
        }

    }

    LaunchedEffect(editableScreenState.isInEditMode) {
        setGestureEnable(!editableScreenState.isInEditMode)
    }

    NotesScreenContent(
        editableScreenState = editableScreenState,
        isGridView = isGridView,
        onViewModeChanged = {
            viewModel.setViewMode(!isGridView)
        },

        noteList = noteList,
        onDeleteEditItems = {
            coroutineScope.launch {
                viewModel.setNotesHide(editableScreenState.selectedEditItems)
                editableScreenState.exitEditMode()
            }
        },

        openDraw = openDraw,
        onCardClick = {
            navController.navigate(
                ScreenNavDestination.NotesDetail.createRoute(
                    id = it,
                    enterState = UiEnterState.Edit
                )
            )
        },
        onAddNewNote = {
            navController.navigate(ScreenNavDestination.NotesDetail.createRoute(enterState = UiEnterState.Add))
        },
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreenContent(
    editableScreenState: EditableScreenState,
    isGridView: Boolean,
    onViewModeChanged: () -> Unit,
    noteList: List<NoteData>,
    onCardClick: (Long) -> Unit,
    onAddNewNote: () -> Unit,
    openDraw: () -> Unit,
    onDeleteEditItems: () -> Unit,

    snackbarHostState: SnackbarHostState,

    modifier: Modifier = Modifier
) {
    var isShowEditDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val displayNotes = remember(noteList) {
        derivedStateOf { noteList.filter { !it.isHide } }
    }

    if (isShowEditDeleteDialog) {
        DeleteConfirmDialog(
            title = stringResource(R.string.title_confirm_deletion),
            deleteInfo = pluralStringResource(
                R.plurals.notes_delete_confirm_message,
                editableScreenState.selectionCount
            ),
            onDismiss = { isShowEditDeleteDialog = false },
            onConfirm = { onDeleteEditItems() }
        )
    }

    EditableScreen(
        editableScreenState = editableScreenState,
        onDeleteSelectedItem = { isShowEditDeleteDialog = true },
        snackBarHost = {
            SnackbarHost(snackbarHostState)
        },
        normalModeTopAppBar = {
            //正常模式
            TopAppBarWithMenu(
                title = stringResource(R.string.title_notes),
                navigationIcon = {
                    IconButton(onClick = openDraw) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onViewModeChanged) {
                        Icon(
                            imageVector = if (isGridView)
                                Icons.Filled.GridView
                            else
                                Icons.Outlined.ViewAgenda,
//                                    Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !editableScreenState.isInEditMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = onAddNewNote,
                    modifier = Modifier
                        .padding(8.dp)
                        .semantics {
                            contentDescription = "Notes add FAB"
                        },
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyVerticalStaggeredGrid(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                columns =
                    if (isGridView) StaggeredGridCells.Adaptive(128.dp)
                    else StaggeredGridCells.Fixed(1),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(displayNotes.value) {
                    key(it.id) {
                        NoteCard(
                            isGridView = isGridView,
                            noteData = it,
                            onCardClick = onCardClick,

                            isInEditMode = editableScreenState.isInEditMode,
                            isEditSelected = editableScreenState.getSelectedState(it.id),
                            onSelectInEditMode = editableScreenState::trySelectItem,
                            onCardLongClick = editableScreenState::enterEditMode,
                            modifier = Modifier.animateItem()
                        )
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    isGridView: Boolean,
    isInEditMode: Boolean,
    isEditSelected: Boolean,
    onSelectInEditMode: (id: Long, isSelect: Boolean) -> Unit,
    onCardLongClick: (Long) -> Unit,
    noteData: NoteData,
    onCardClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val titleValue = remember(noteData) {
        derivedStateOf {
            noteData.title.ifEmpty {
                noteData.content.getFirstLine().trimIndent()
            }
        }
    }

    val contentValue = remember(noteData) {
        derivedStateOf {
            if (noteData.title.isEmpty())
                noteData.content.removeFirstLine().trimIndent()
            else
                noteData.content.trimIndent()
        }
    }

    Card(
        modifier = modifier
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    if (!isInEditMode && !isEditSelected)
                        onCardClick(noteData.id)
                    else
                        onSelectInEditMode(noteData.id, isEditSelected)
                },
                onLongClick = { onCardLongClick(noteData.id) }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.animateContentSize())
            {
                if (isInEditMode) {
                    Checkbox(
                        checked = isEditSelected,
                        onCheckedChange = {
                            onSelectInEditMode(
                                noteData.id,
                                isEditSelected
                            )
                        }
                    )
                } else
                    Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    titleValue.value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text(
                    contentValue.value,
                    fontSize = 14.sp,
                    maxLines = if (isGridView) 128 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.heightIn(min = 0.dp, max = 256.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatTimestampToReadableString(
                        noteData.createdAt,
                        DateFormatters.simpleYearMonthDay(context)
                    ),
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


@Preview
@Composable
private fun NoteCardPreview() {
    TodoListTheme {
        NoteCard(
            noteData = NoteData(
                title = "Note Title wda  awd awdawfawgaw  agag a awf awfaww",
                content = "provides support for Jetpack Compose applications.\n" +
                        "You can navigate between composables while taking advantage of the Navigation component's infrastructure and features.\n" +
                        "When implementing navigation in an app" +
                        "\n\n\n\n\n\n, implement a navigation host, graph, and controller. For more information, see the Navigation overview.",
            ),
            onCardClick = {}, modifier = Modifier
                .width(250.dp)
                .height(350.dp)
                .wrapContentHeight(),
            isInEditMode = false,
            isEditSelected = false,
            onSelectInEditMode = { p1, p2 -> },
            onCardLongClick = {},
            isGridView = false
        )
    }
}

@Preview
@Composable
private fun NotesScreenPreview() {
    TodoListTheme {
        NotesScreenContent(
            openDraw = {},
            onAddNewNote = {},
            onCardClick = {},
            noteList = listOf(
                NoteData(
                    title = "Note Title",
                    content = "provides support for Jetpack Compose applications.\n" +
                            "You can navigate between composables while taking advantage of the Navigation component's infrastructure and features.\n" +
                            "When implementing navigation in an app, implement a navigation host, graph, and controller. For more information, see the Navigation overview.",
                    isHide = false
                ),
                NoteData(
                    title = "Note Title",
                    content = "navigation host, graph, and controller. For more information, see the Navigation overview.",
                    isHide = false
                )
            ),

            onDeleteEditItems = {},
            isGridView = false,
            onViewModeChanged = {},
            editableScreenState = rememberEditableScreenState(),
            snackbarHostState = SnackbarHostState(),
        )
    }
}