package com.sijayyx.todone.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sijayyx.todone.ui.theme.TodoListTheme
import com.sijayyx.todone.utils.NONE_STRING

object IconOption {
    val optionList = IconOptions.entries.toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSelectorLazyRow(
    currentSelectOption: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        item {
            OptionNoneItem(
                borderColor = MaterialTheme.colorScheme.surfaceTint,
                iconColor = MaterialTheme.colorScheme.secondary,
                isSelected = NONE_STRING == currentSelectOption,
                onOptionClick = { onSelect(it) }
            )
        }

        items(IconOption.optionList) {
            IconOptionItem(
                content = it.iconName,
                borderColor = MaterialTheme.colorScheme.surfaceTint,
                isSelected = it.iconName == currentSelectOption,
                onClick = onSelect
            )
        }
    }
}


@Composable
fun IconOptionItem(
    content: String,
    borderColor: Color,
    isSelected: Boolean,
    onClick: (String) -> Unit,
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
                        onClick(content)
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

        Text(content, fontSize = 26.sp)
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
fun IconSelectorPreview() {
    TodoListTheme {
        IconSelectorLazyRow(
            currentSelectOption = NONE_STRING,
            onSelect = {}
        )
    }
}

enum class IconOptions(val iconName: String) {
    Briefcase("💼"),
    Calendar("📆"),
    Fire("🔥"),
    ShoppingCart("🛒"),
    JetPlane("✈️"),
    Car("🚗"),
    TravelMap("🗺️"),
    AlarmClock("⏰"),
    Alert("⚠️"),
    Heart("❤️"),
    Medicine("💊"),
    Hospital("🏥"),
    House("🏠"),
    Star("⭐"),
    Shining("✨"),
    Books("📚"),
    Telephone("☎️"),
    Cake("🎂")
}