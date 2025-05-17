package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.ramitsuri.podcasts.utils.LogHelper
import com.ramitsuri.podcasts.utils.imageRequest

@Composable
fun Image(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds,
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    Box {
        AsyncImage(
            model =
                LocalContext.current
                    .imageRequest(url)
                    .crossfade(true)
                    .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            onSuccess = {
                isLoading = false
            },
            onError = {
                LogHelper.v("AsyncImage", "Error loading image: $url, error: ${it.result.throwable}")
                isLoading = false
                isError = true
            },
        )
        if (isLoading || isError) {
            Placeholder(modifier, isLoading)
        }
    }
}

@Composable
private fun Placeholder(
    modifier: Modifier,
    isLoading: Boolean,
) {
    val color = Color(0xFF535454)
    Box(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .let {
                    if (isLoading) {
                        it.background(shimmerBrush(color))
                    } else {
                        it.background(color.copy(alpha = 0.3f))
                    }
                }
                .then(modifier),
    )
}

@Composable
private fun shimmerBrush(color: Color): ShaderBrush {
    val transition = rememberInfiniteTransition(label = "shimmer transition")
    val offset by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000),
                ),
            label = "shimmer offset",
        )
    return remember(offset) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val widthOffset = size.width * offset
                val heightOffset = size.height * offset
                return LinearGradientShader(
                    colors = listOf(color.copy(alpha = 0.1f), color.copy(alpha = 0.3f), color.copy(alpha = 0.1f)),
                    from = Offset(widthOffset, heightOffset),
                    to = Offset(widthOffset + size.width, heightOffset + size.height),
                    tileMode = TileMode.Mirror,
                )
            }
        }
    }
}
