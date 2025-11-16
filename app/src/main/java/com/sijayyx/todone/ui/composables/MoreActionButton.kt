package com.sijayyx.todone.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MoreActionButton(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    action: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    IconButton(
        onClick = { isExpanded = !isExpanded }
    ) {
        Icon(Icons.Filled.MoreVert, contentDescription = null)
    }

    Box{
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                action { isExpanded = false }
            }
        }
    }

}