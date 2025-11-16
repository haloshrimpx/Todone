package com.sijayyx.todone.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sijayyx.todone.ui.theme.TodoListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    val radius = 24f
    val borderWidth = 6f
    val colors = CheckboxDefaults.colors()

    Box(
        contentAlignment = Alignment.Center, modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(100))
            .clickable(
                onClick = {
                    onCheckedChange?.invoke(!checked)
                }
            )
    ) {
        Canvas(
            modifier = Modifier
                .size(24.dp)

        ) {
            drawCircle(
                color = if (checked) colors.checkedBorderColor else colors.uncheckedBorderColor,
                radius = radius + borderWidth,
                center = center,
                style = Stroke(width = borderWidth)
            )

            drawCircle(
                color = if (checked) colors.checkedBoxColor else colors.uncheckedBoxColor,
                radius = radius,
                center = center
            )
        }
//        if (checked)
//            Icon(
//                Icons.Filled.Check, contentDescription = null, tint = colors.checkedCheckmarkColor,
//                modifier = Modifier.size(18.dp)
//            )
    }
}

@Preview(showBackground = true)
@Composable
fun RoundCheckboxPreview() {
    TodoListTheme {
        RoundCheckbox(
            checked = false
        )
    }
}