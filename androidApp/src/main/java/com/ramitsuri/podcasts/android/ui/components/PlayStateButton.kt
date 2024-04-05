package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.utils.minutesFormatted
import com.ramitsuri.podcasts.model.PlayingState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
fun PlayStateButton(
    playingState: PlayingState,
    hasBeenPlayed: Boolean,
    remainingDuration: Duration?,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        when (playingState) {
            PlayingState.PLAYING -> {
                Playing()
            }

            PlayingState.NOT_PLAYING -> {
                Paused(hasBeenPlayed = hasBeenPlayed, remainingDuration = remainingDuration)
            }

            PlayingState.LOADING -> {
                Loading()
            }
        }
    }
}

@Composable
private fun Playing() {
    PlayingIndicator()
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = stringResource(id = R.string.play_state_button_playing),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun PlayingIndicator() {
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .padding(horizontal = 4.dp),
    ) {
        val color = MaterialTheme.colorScheme.onBackground
        val infiniteTransition = rememberInfiniteTransition(label = "playing indicator")
        val density = LocalDensity.current
        val firstLineHeight by infiniteTransition.animateFloat(
            initialValue = with(density) { 8.dp.toPx() },
            targetValue = with(density) { 2.dp.toPx() },
            animationSpec =
                infiniteRepeatable(
                    animation = tween(100),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "",
        )
        val middleLineHeight by infiniteTransition.animateFloat(
            initialValue = with(density) { 16.dp.toPx() },
            targetValue = with(density) { 6.dp.toPx() },
            animationSpec =
                infiniteRepeatable(
                    animation = tween(200),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "",
        )
        val lastLineHeight by infiniteTransition.animateFloat(
            initialValue = with(density) { 8.dp.toPx() },
            targetValue = with(density) { 3.dp.toPx() },
            animationSpec =
                infiniteRepeatable(
                    animation = tween(150),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "",
        )
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            val lineWidth = 4.dp.toPx()
            val spacingBetweenLines = 2.dp.toPx()
            val lineStartY = 20.dp.toPx() // 4dp space at the bottom in 24dp box
            drawLine(
                color = color,
                start = Offset(lineWidth, lineStartY),
                end = Offset(lineWidth, lineStartY - firstLineHeight),
                strokeWidth = lineWidth,
            )
            drawLine(
                color = color,
                start = Offset((lineWidth * 2) + spacingBetweenLines, lineStartY),
                end = Offset((lineWidth * 2) + spacingBetweenLines, lineStartY - middleLineHeight),
                strokeWidth = lineWidth,
            )
            drawLine(
                color = color,
                start = Offset((lineWidth * 3) + (spacingBetweenLines * 2), lineStartY),
                end = Offset((lineWidth * 3) + (spacingBetweenLines * 2), lineStartY - lastLineHeight),
                strokeWidth = lineWidth,
            )
        }
    }
}

@Composable
private fun Paused(
    hasBeenPlayed: Boolean,
    remainingDuration: Duration?,
) {
    val minutes by remember {
        derivedStateOf {
            remainingDuration?.inWholeMinutes
        }
    }
    Icon(imageVector = Icons.Filled.PlayCircleOutline, contentDescription = "")
    minutes?.let {
        Spacer(modifier = Modifier.width(8.dp))
        val suffix =
            if (hasBeenPlayed) {
                stringResource(id = R.string.play_state_button_left)
            } else {
                ""
            }
        Text(
            text = minutesFormatted(minutes = it, suffix = suffix),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Loading() {
    Column(
        modifier = Modifier.height(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        LinearProgressIndicator(
            modifier =
                Modifier
                    .width(16.dp)
                    .height(2.dp),
            color = MaterialTheme.colorScheme.onBackground,
            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
        )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = stringResource(id = R.string.play_state_button_loading),
        style = MaterialTheme.typography.bodySmall,
    )
}

@ThemePreview
@Composable
private fun PlayStateButtonPreview_Playing() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.PLAYING,
                hasBeenPlayed = false,
                remainingDuration = 5.minutes,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PlayStateButtonPreview_Loading() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.LOADING,
                hasBeenPlayed = false,
                remainingDuration = 5.minutes,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PausedPreview_HasBeenPlayed() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.NOT_PLAYING,
                hasBeenPlayed = true,
                remainingDuration = 65.minutes,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PausedPreview_HasNotBeenPlayed() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.NOT_PLAYING,
                hasBeenPlayed = false,
                remainingDuration = 65.minutes,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PausedPreview_HasBeenPlayed_JustMinutes() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.NOT_PLAYING,
                hasBeenPlayed = true,
                remainingDuration = 32.minutes,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PausedPreview_HasNotBeenPlayed_JustMinutes() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.NOT_PLAYING,
                hasBeenPlayed = false,
                remainingDuration = 32.minutes,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PausedPreview_DurationNull() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PlayStateButton(
                playingState = PlayingState.NOT_PLAYING,
                hasBeenPlayed = false,
                remainingDuration = null,
                onClick = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PausedPreview_DurationNull_() {
    PreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PlayStateButton(
                playingState = PlayingState.PLAYING,
                hasBeenPlayed = false,
                remainingDuration = null,
                onClick = { },
            )
        }
    }
}
