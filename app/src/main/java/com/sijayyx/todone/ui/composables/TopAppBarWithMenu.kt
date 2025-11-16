package com.sijayyx.todone.ui.composables

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithMenu(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable (() -> Unit) = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 26.sp,
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
//        scrollBehavior = scrollBehavior,
//        colors = TopAppBarDefaults.mediumTopAppBarColors(scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer)
    )
}