package com.sijayyx.todone.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun InputFieldBottomBar(
    textValue: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    inputFieldLabel: String = "",
    leadingAction: @Composable () -> Unit = {},
    trailingAction: @Composable (canConfirm: Boolean) -> Unit = {},
    bottomActions: @Composable RowScope .() -> Unit = {},
) {
    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .semantics {
                isTraversalGroup = true
                contentDescription = "Bottom Input Bar"
            },
        contentAlignment = Alignment.Companion.BottomCenter
    ) {
        Scrim(
            color = Color.Companion.Black.copy(alpha = 0.5f),
            onDismissRequest = onDismiss,
            visible = true
        )
        Surface(
            modifier = Modifier.Companion
//            .imePadding()
//            .navigationBarsPadding()
                .fillMaxWidth()
                .wrapContentHeight(),
//        color = Color.Transparent
        ) {
            Column {
                Spacer(Modifier.Companion.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    modifier = Modifier.Companion.padding(4.dp)
                ) {
                    leadingAction()

                    TextField(
                        value = textValue,
                        label = { Text(inputFieldLabel) },
                        onValueChange = onValueChange,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Companion.Transparent,
                            unfocusedIndicatorColor = Color.Companion.Transparent,
                            disabledIndicatorColor = Color.Companion.Transparent,
                            errorIndicatorColor = Color.Companion.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Companion.Done),
                        modifier = Modifier.Companion
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .semantics { contentDescription = "Bottom Input Field" }
                            .animateContentSize(),
                    )
                    trailingAction(!textValue.isEmpty())
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),


                    ) {
                    bottomActions()
                }
            }
        }
    }
}

@Preview
@Composable
fun InputFieldBottomBarPreview() {
    TodoListTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            InputFieldBottomBar(
                textValue = "",
                onValueChange = {},
                focusRequester = remember { FocusRequester() },
                inputFieldLabel = "New Todo",
                leadingAction = {
                    IconButton(onClick = {

                    }) {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                trailingAction = {
                    IconButton(onClick = {

                    }) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                bottomActions = {
                    DropdownIconTextButton("Plan", icon = Icons.Filled.Checklist)
                    DropdownIconTextButton("Deadline", icon = Icons.Filled.DateRange)
                    DropdownIconTextButton("Reminder", icon = Icons.Filled.AddAlert)
                },
                onDismiss = {}
            )
        }

    }
}
