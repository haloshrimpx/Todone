package com.sijayyx.todone.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.NONE_STRING

@Stable
object ColorOption {

    val RedDark = Color(0xFFB64242)
    val RedLight = Color(0xFFFF4747)
    val GreenDark = Color(0xFF247a46)
    val GreenLight = Color(0xFF4AF38E)
    val BlueDark = Color(0xFF2f6cbb)
    val BlueLight = Color(0xFF64A6FF)
    val YellowDark = Color(0xFFB88C15)
    val YellowLight = Color(0xFFFFE64C)
    val PurpleDark = Color(0xFF7c2cb9)
    val PurpleLight = Color(0xFFBA68FF)
    val PinkDark = Color(0xFFAD428A)
    val PinkLight = Color(0xFFFF7AD3)

    @Stable
    val optionsLight = mapOf(
        ColorOptions.Red.colorName to RedLight,
        ColorOptions.Green.colorName to GreenLight,
        ColorOptions.Blue.colorName to BlueLight,
        ColorOptions.Yellow.colorName to YellowLight,
        ColorOptions.Purple.colorName to PurpleLight,
        ColorOptions.Pink.colorName to PinkLight
    )

    @Stable
    val optionsDark = mapOf(
        ColorOptions.Red.colorName to RedDark,
        ColorOptions.Green.colorName to GreenDark,
        ColorOptions.Blue.colorName to BlueDark,
        ColorOptions.Yellow.colorName to YellowDark,
        ColorOptions.Purple.colorName to PurpleDark,
        ColorOptions.Pink.colorName to PinkDark
    )


    fun formatStringToColor(string: String, isDarkTheme: Boolean): Color? {
        return if (isDarkTheme) optionsDark[string] else optionsLight[string]
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelectorDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isDarkTheme: () -> Boolean,
    modifier: Modifier = Modifier
) {
    val options = remember(isDarkTheme()) {
        derivedStateOf {
            if (isDarkTheme()) ColorOption.optionsDark
            else ColorOption.optionsLight
        }
    }.value

    var currentSelectedColor by rememberSaveable { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.ColorLens, contentDescription = null) },
        title = { Text(stringResource(R.string.select_a_color)) },
        text = {
            ColorSelectorRow(
                currentSelectedColor = currentSelectedColor,
                onSelectChange = { currentSelectedColor = it },
                options = options
            )
        },
        confirmButton = {
            IconButton(
                onClick = {
                    onConfirm(currentSelectedColor)
                    onDismiss()
                }
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun ColorSelectorRow(
    currentSelectedColor: String,
    onSelectChange: (String) -> Unit,
    options: Map<String, Color>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {

        OptionNoneItem(
            borderColor = MaterialTheme.colorScheme.surfaceTint,
            iconColor = MaterialTheme.colorScheme.secondary,
            isSelected = NONE_STRING == currentSelectedColor,
            onOptionClick = { onSelectChange(it) }
        )

        options.keys.forEach { it ->
            key(it) {
                ColorOptionItem(
                    mainColor = options[it] ?: return,
                    borderColor = MaterialTheme.colorScheme.surfaceTint,
                    isSelected = it == currentSelectedColor,
                    colorName = it,
                    onOptionClick = { onSelectChange(it) }
                )
            }

        }
    }
}

@Composable
fun ColorOptionItem(
    mainColor: Color,
    colorName: String,
    borderColor: Color,
    isSelected: Boolean,
    onOptionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val radius = 42f
    val borderWidth = 8f

    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = modifier
                .size((borderWidth + radius).dp)
                .clip(RoundedCornerShape(100))
                .clickable(
                    onClick = {
                        onOptionClick(colorName)
                    }
                )
        ) {
            if (isSelected) {
                drawCircle(
                    color = borderColor,
                    radius = radius + borderWidth,
                    center = center,
                    style = Stroke(width = borderWidth)
                )
            }

            drawCircle(
                color = mainColor,
                radius = radius,
                center = center
            )
        }
    }


}

@Composable
fun OptionNoneItem(
    borderColor: Color,
    iconColor: Color,
    isSelected: Boolean,
    onOptionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val radius = 42f
    val borderWidth = 8f

    Box(contentAlignment = Alignment.Center) {
        Canvas(
            modifier = modifier
                .size((borderWidth + radius).dp)
                .clip(RoundedCornerShape(100))
                .clickable(
                    onClick = {
                        onOptionClick(
                            NONE_STRING
                        )
                    }
                )
        ) {
            if (isSelected) {
                drawCircle(
                    color = borderColor,
                    radius = radius + borderWidth,
                    center = center,
                    style = Stroke(width = borderWidth)
                )
            }
        }

        Icon(
            Icons.Filled.NotInterested, contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
@Preview(
    showSystemUi = false
)
fun ColorSelectorPreview() {
    TodoListTheme {
        ColorSelectorDialog(
            onDismiss = {},
            initialValue = NONE_STRING,
            onConfirm = {},
            isDarkTheme = { false }
        )
    }
}

enum class ColorOptions(val colorName: String) {
    None(colorName = NONE_STRING),
    Red(colorName = "Red"),
    Green(colorName = "Green"),
    Blue(colorName = "Blue"),
    Yellow(colorName = "Yellow"),
    Purple(colorName = "Purple"),
    Pink(colorName = "Pink")
}
