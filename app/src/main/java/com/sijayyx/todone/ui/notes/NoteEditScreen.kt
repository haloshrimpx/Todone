package com.sijayyx.todone.ui.notes

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sijayyx.todone.AppViewModelProvider
import com.sijayyx.todone.R
import com.sijayyx.todone.data.NoteData
import com.sijayyx.todone.ui.composables.DeleteConfirmDialog
import com.sijayyx.todone.ui.composables.IconTextRow
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.DateFormatters
import com.sijayyx.todone.utils.TAG
import com.sijayyx.todone.utils.UiEnterState
import com.sijayyx.todone.utils.formatTimestampToReadableString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NoteEditScreen(
    setGestureEnable: (Boolean) -> Unit,
    noteId: Long?,
    enterState: UiEnterState,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditScreenViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(
        noteId,
        enterState
    ) {
        if (enterState == UiEnterState.Edit && noteId != null && noteId > 0) {
            viewModel.getNoteDataById(noteId)
        } else if (enterState == UiEnterState.Add) {
            viewModel.initializeNewNote()
        } else {
            onNavigateUp()
        }

        setGestureEnable(false)
    }

    NoteEditScreenContent(
        enterState = enterState,
        noteData = uiState.value.noteData,
        onInputTitle = { viewModel.inputTitle(it) },
        onInputContent = { viewModel.inputContent(it) },
        keyboardController = keyboardController,
        onNavigateUp = {
            if (uiState.value.noteData.title.isEmpty() && uiState.value.noteData.content.isEmpty())
                viewModel.setNoteDataHide({}, true)
            else
                viewModel.updateNoteData {}

            onNavigateUp()
            Log.e(TAG, "Navigate up from note edit screen")
        },
        onShareNote = {
            viewModel.shareNote(context = context)
        },
        onDeleteNote = {
            val isEmptyData =
                uiState.value.noteData.title.isEmpty() && uiState.value.noteData.content.isEmpty()
            viewModel.setNoteDataHide(onNavigateUp, isEmptyData)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun NoteEditScreenContent(
    enterState: UiEnterState,
    noteData: NoteData,
    keyboardController: SoftwareKeyboardController?,
    onNavigateUp: () -> Unit,
    onInputTitle: (String) -> Unit,
    onInputContent: (String) -> Unit,
    onDeleteNote: () -> Unit,
    onShareNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isShowingDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val characterCount = remember(noteData) { noteData.content.length }
    val titleValue = remember(noteData) { noteData.title }
    val contentValue = remember(noteData) { noteData.content }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val titleBringIntoViewRequester = remember { BringIntoViewRequester() }
    val contentBringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current

    BackHandler {
        onNavigateUp()
    }

    if (isShowingDeleteConfirmDialog) {
        DeleteConfirmDialog(
            deleteInfo = stringResource(R.string.this_note_will_be_deleted_forever),
            onDismiss = { isShowingDeleteConfirmDialog = false },
            onConfirm = onDeleteNote
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (enterState == UiEnterState.Add) stringResource(R.string.add_note)
                        else stringResource(R.string.edit_note),
                        fontSize = 26.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateUp()
                        keyboardController?.hide()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },

                actions = {
                    IconButton(
                        //分享
                        enabled = (titleValue.isNotEmpty() || contentValue.isNotEmpty()),
                        onClick = onShareNote
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                    }
                    IconButton(
                        enabled = (titleValue.isNotEmpty() || contentValue.isNotEmpty()),
                        //删除
                        onClick = { isShowingDeleteConfirmDialog = true }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }
                }
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .imePadding() // 为键盘留出空间
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
            ) {
                BasicTextField(
                    value = titleValue,
                    onValueChange = {
                        if (it.length < 100)
                            onInputTitle(it)
                        coroutineScope.launch {
                            delay(100)
                            titleBringIntoViewRequester.bringIntoView()
                        }
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (titleValue.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.placeholder_title),
                                    fontWeight = FontWeight.W400,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    fontSize = 32.sp
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(titleBringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    // 当获得焦点时，滚动到视图

                                    delay(500)
                                    titleBringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                        .animateContentSize()
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(verticalArrangement = Arrangement.Center) {
                    IconTextRow(
                        text = formatTimestampToReadableString(
                            noteData.createdAt,
                            DateFormatters.simpleYearMonthDayTime(context)
                        ),
                        icon = Icons.Filled.AccessTime
                    )
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .height(14.dp)
                            .padding(horizontal = 8.dp)
                    )
                    IconTextRow(
                        text = pluralStringResource(
                            R.plurals.count_characters,
                            characterCount,
                            characterCount
                        ),
//                        icon = Icons.Filled.AccessTime
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    maxLines = 50,
                    value = contentValue,
                    onValueChange = {
                        onInputContent(it)
                        coroutineScope.launch {
                            delay(100)
                            contentBringIntoViewRequester.bringIntoView()
                        }
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(350),
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (contentValue.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.placeholder_content),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight(350),
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .heightIn(min = 256.dp)
                        .fillMaxSize()
                        .bringIntoViewRequester(contentBringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                coroutineScope.launch {
                                    // 当获得焦点时，滚动到视图

                                    delay(500)
                                    contentBringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                )
            }
        }
    }
}

@Preview
@Composable
private fun NoteEditScreenPreview() {
    TodoListTheme {
        NoteEditScreenContent(
            keyboardController = null,
            onNavigateUp = {},
            onInputTitle = {},
            onInputContent = {},
            noteData = NoteData(
                title = "Anything 4 u",
                content = "I'll sell my soul sell my guitar,\n\n" +
                        "Sleep in the back of your beat up car,\n\n" +
                        "Girl just tell me what i have to do\n\n" +
                        "I'll do anything for you."
            ),
            enterState = UiEnterState.None,
            onDeleteNote = {},
            onShareNote = {},
        )
    }
}