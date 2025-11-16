package com.sijayyx.todone.ui.todo

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sijayyx.todone.ui.theme.TodoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(modifier: Modifier = Modifier, onNavigateUp: () -> Unit = {}) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Add Todo", fontSize = 26.sp) },
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
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Filled.Done, contentDescription = null)
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
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InputField(label = "Todo")
                InputField(label = "Date")
                InputField(label = "Description")

//                Spacer(Modifier.weight(1f))
//                EditButton(
//                    label = "Save", modifier = Modifier
//                        .wrapContentHeight()
//                        .fillMaxWidth()
//                        .padding(horizontal = 52.dp)
//                )
//                EditButton(
//                    label = "Cancel", modifier = Modifier
//                        .wrapContentHeight()
//                        .fillMaxWidth()
//                        .padding(horizontal = 52.dp)
//                )
//                Spacer(Modifier.weight(0.4f))
            }


        }
    }
}

@Composable
fun EditButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = {},
        modifier = modifier
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun InputField(
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = "",
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
fun TodoEditScreenPreview() {
    TodoListTheme {
        TodoEditScreen()
    }
}