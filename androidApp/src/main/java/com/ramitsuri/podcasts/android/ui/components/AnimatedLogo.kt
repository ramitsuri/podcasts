package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AnimatedLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    size: Dp = 48.dp,
    isAnimating: Boolean = false,
) {
    val defaultHeights = listOf(0.2f, 0.6f, 1.0f, 0.6f, 0.2f)
    var heights by remember { mutableStateOf(defaultHeights) }

    LaunchedEffect(isAnimating) {
        if (!isAnimating) {
            heights = defaultHeights
        }
        while (isAnimating) {
            val newHeights = heights.drop(1) + (Random.nextFloat() * 0.9f + 0.1f)
            heights = newHeights
            delay(150)
        }
    }

    val animatedHeights =
        heights.map { targetHeight ->
            val animatedValue by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 80, easing = LinearEasing),
            )
            animatedValue
        }

    Canvas(modifier = modifier.size(size)) {
        val barWidth = size.toPx() * 0.08f
        val spacing = size.toPx() * 0.12f
        val centerY = size.toPx() / 2

        animatedHeights.forEachIndexed { index, heightRatio ->
            val barHeight = size.toPx() * heightRatio
            val x = (barWidth + spacing) * index + spacing

            drawRoundRect(
                color = color,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2),
            )
        }
    }
}

@ThemePreview
@Composable
private fun PreviewAnimatedLogo() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            var animating by remember { mutableStateOf(false) }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedLogo(isAnimating = animating)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { animating = !animating }) {
                Text(if (animating) "Stop" else "Start")
            }
        }
    }
}
