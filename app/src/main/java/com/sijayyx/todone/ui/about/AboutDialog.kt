package com.sijayyx.todone.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.theme.TodoListTheme

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appIconFront = painterResource(R.drawable.ic_launcher_foreground)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .drawWithContent {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFFF093FB),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                    drawContent()
                }
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = Color.White)
                    ) {

                        Image(
                            painter = appIconFront,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).scale(1.4f),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.app_name), fontSize = 24.sp)
                    Text("v1.0")
                }
                Spacer(Modifier.height(16.dp))
                Column {
                    Text(stringResource(R.string.dev_by_sijayyx), fontSize = 12.sp)
                    Text(stringResource(R.string.email_address), fontSize = 12.sp)
                }
            }

        }
    }
}

@Preview
@Composable
private fun AboutDialogPreview() {
    TodoListTheme {

        AboutDialog(onDismiss = {})


    }
}
