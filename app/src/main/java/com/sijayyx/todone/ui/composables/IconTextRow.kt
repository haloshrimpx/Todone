package com.sijayyx.todone.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconTextRow(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    hideIfEmpty: Boolean = false,
    size: Int = 12,
) {
    if (!(hideIfEmpty && text.isEmpty())) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.wrapContentSize()) {
            if (icon !=null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(size.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(text = text, fontSize = size.sp, lineHeight = size.sp)
//            Spacer(Modifier.width(8.dp))
        }
    }
}