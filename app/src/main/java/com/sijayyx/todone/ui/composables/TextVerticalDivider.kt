package com.sijayyx.todone.ui.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun TextVerticalDivider(modifier: Modifier = Modifier, size: Int = 12) {
    Text(
        "|",
        lineHeight = size.sp,
        fontSize = size.sp,
        modifier = modifier
    )
}