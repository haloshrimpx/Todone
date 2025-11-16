package com.sijayyx.todone.ui.composables

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex

@Composable
fun Scrim(color: Color, onDismissRequest: () -> Unit, visible: Boolean) {
    if (color.isSpecified) {
        val alpha by
        animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = TweenSpec())
        val dismissSheet =
            if (visible) {
                Modifier
                    .pointerInput(onDismissRequest) { detectTapGestures { onDismissRequest() } }
                    .semantics(mergeDescendants = true) {
                        traversalIndex = 1f
                        onClick {
                            onDismissRequest()
                            true
                        }
                    }
            } else {
                Modifier
            }
        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissSheet)
        ) {
            drawRect(color = color, alpha = alpha.coerceIn(0f, 1f))
        }
    }
}