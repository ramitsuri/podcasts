package com.ramitsuri.podcasts.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.model.ui.PlayerViewState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerScreen(
    state: PlayerViewState,
    modifier: Modifier = Modifier,
    onNotExpandedHeightKnown: (Int) -> Unit,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onForwardClicked: () -> Unit,
    onSeekValueChange: (Float) -> Unit,
    onPlaybackSpeedSet: (Float) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(if (state.isExpanded) 16.dp else 0.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (!state.isExpanded) {
            PlayerScreenNotExpanded(
                modifier = Modifier.onGloballyPositioned {
                    onNotExpandedHeightKnown(it.size.height)
                },
                episodeTitle = state.episodeTitle,
                episodeArtwork = state.episodeArtworkUrl,
                isPlaying = state.isPlaying,
                playProgress = state.progress,
                onPlayClicked = onPlayClicked,
                onPauseClicked = onPauseClicked,
            )
        }
        PlayerScreenExpanded(
            modifier = Modifier.alpha(if (state.isExpanded) 1f else 0f),
            episodeTitle = state.episodeTitle,
            episodeArtwork = state.episodeArtworkUrl,
            podcastName = state.podcastName,
            isPlaying = state.isPlaying,
            playedDuration = state.playedDuration,
            remainingDuration = state.remainingDuration,
            playProgress = state.progress,
            sleepTimer = state.sleepTimer,
            playbackSpeed = state.playbackSpeed,
            isCasting = state.isCasting,
            onGoToQueueClicked = onGoToQueueClicked,
            onReplayClicked = onReplayClicked,
            onPauseClicked = onPauseClicked,
            onPlayClicked = onPlayClicked,
            onForwardClicked = onForwardClicked,
            onSeekValueChange = onSeekValueChange,
            onPlaybackSpeedSet = onPlaybackSpeedSet,
        )
    }
}

@Composable
private fun PlayerScreenExpanded(
    modifier: Modifier = Modifier,
    episodeTitle: String,
    episodeArtwork: String,
    podcastName: String,
    isPlaying: Boolean,
    playedDuration: Duration,
    remainingDuration: Duration,
    playProgress: Float,
    sleepTimer: SleepTimer,
    playbackSpeed: Float,
    isCasting: Boolean,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onForwardClicked: () -> Unit,
    onSeekValueChange: (Float) -> Unit,
    onPlaybackSpeedSet: (Float) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(episodeArtwork)
                .crossfade(true)
                .build(),
            contentDescription = episodeTitle,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .size(360.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = episodeTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = podcastName, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))
        Seekbar(
            playedDuration = playedDuration,
            remainingDuration = remainingDuration,
            playProgress = playProgress,
            onSeekValueChange = onSeekValueChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MainControls(
            isPlaying = isPlaying,
            onGoToQueueClicked = onGoToQueueClicked,
            onReplayClicked = onReplayClicked,
            onPauseClicked = onPauseClicked,
            onPlayClicked = onPlayClicked,
            onForwardClicked = onForwardClicked,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SecondaryControls(
            playbackSpeed = playbackSpeed,
            sleepTimer = sleepTimer,
            isCasting = isCasting,
            onPlaybackSpeedSet = onPlaybackSpeedSet,
        )
    }
}

@Composable
private fun MainControls(
    isPlaying: Boolean,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onForwardClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        IconButton(onClick = onGoToQueueClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(id = R.string.pause),
            )
        }
        IconButton(onClick = onReplayClicked) {
            Icon(
                imageVector = Icons.Filled.Replay10,
                modifier = Modifier.size(32.dp),
                contentDescription = stringResource(id = R.string.pause),
            )
        }
        if (isPlaying) {
            FilledIconButton(onClick = onPauseClicked, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    modifier = Modifier.size(32.dp),
                    contentDescription = stringResource(id = R.string.pause),
                )
            }
        } else {
            FilledIconButton(onClick = onPlayClicked, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    modifier = Modifier.size(32.dp),
                    contentDescription = stringResource(id = R.string.pause),
                )
            }
        }
        IconButton(onClick = onForwardClicked) {
            Icon(
                imageVector = Icons.Filled.Forward30,
                modifier = Modifier.size(32.dp),
                contentDescription = stringResource(id = R.string.pause),
            )
        }
        Spacer(modifier = Modifier.width(40.dp))
    }
}

@Composable
private fun SecondaryControls(
    playbackSpeed: Float,
    sleepTimer: SleepTimer,
    isCasting: Boolean,
    onPlaybackSpeedSet: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        TextButton(onClick = { /*TODO*/ }) {
            Text(text = "${playbackSpeed}x")
        }
        if (sleepTimer is SleepTimer.None) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Outlined.Nightlight,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-30f),
                    contentDescription = stringResource(id = R.string.pause),
                )
            }
        } else {
            TextButton(onClick = { /*TODO*/ }) {
                Text(text = sleepTimer.timerDuration.formatted())
            }
        }
        if (isCasting) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.CastConnected,
                    modifier = Modifier.size(24.dp),
                    contentDescription = stringResource(id = R.string.pause),
                )
            }
        } else {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.Cast,
                    modifier = Modifier.size(24.dp),
                    contentDescription = stringResource(id = R.string.pause),
                )
            }
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(id = R.string.pause),
            )
        }
    }
}

@Composable
private fun Seekbar(
    playedDuration: Duration,
    remainingDuration: Duration,
    playProgress: Float,
    onSeekValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = playProgress,
            onValueChange = onSeekValueChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = playedDuration.formatted())
            Text(text = remainingDuration.formatted())
        }
    }
}

private fun Duration.formatted(): String {
    val hours = inWholeHours % 24
    return if (hours == 0L) {
        String.format(
            "%02d:%02d",
            inWholeMinutes % 60,
            inWholeSeconds % 60,
        )
    } else {
        String.format(
            "%02d:%02d:%02d",
            hours,
            inWholeMinutes % 60,
            inWholeSeconds % 60,
        )
    }
}

@Composable
private fun PlayerScreenNotExpanded(
    modifier: Modifier = Modifier,
    episodeTitle: String,
    episodeArtwork: String,
    isPlaying: Boolean,
    playProgress: Float,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(episodeArtwork)
                    .crossfade(true)
                    .build(),
                contentDescription = episodeTitle,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .size(64.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = episodeTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            if (isPlaying) {
                IconButton(onClick = onPauseClicked) {
                    Icon(imageVector = Icons.Filled.Pause, contentDescription = stringResource(id = R.string.pause))
                }
            } else {
                IconButton(onClick = onPlayClicked) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = stringResource(id = R.string.play))
                }
            }
        }
        // TODO show play progress in divider
        HorizontalDivider()
    }
}

@Preview
@Composable
private fun PlayerScreenPreview_IsPlaying_NotExpanded() {
    AppTheme {
        PlayerScreen(
            state = PlayerViewState(
                isExpanded = false,
                isPlaying = true,
                episodeTitle = episode().title,
                episodeArtworkUrl = episode().link,
                podcastName = episode().podcastName,
                sleepTimer = SleepTimer.None,
                playbackSpeed = 1f,
                isCasting = false,
                progress = 0.4f,
                playedDuration = 5.seconds,
                remainingDuration = 55.minutes + 32.seconds,
            ),
            onNotExpandedHeightKnown = { },
            onGoToQueueClicked = { },
            onReplayClicked = { },
            onPauseClicked = { },
            onPlayClicked = { },
            onForwardClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
        )
    }
}

@Preview
@Composable
private fun PlayerScreenPreview_IsNotPlaying_NotExpanded() {
    AppTheme {
        PlayerScreen(
            state = PlayerViewState(
                isExpanded = false,
                isPlaying = false,
                episodeTitle = episode().title,
                episodeArtworkUrl = episode().link,
                podcastName = episode().podcastName,
                sleepTimer = SleepTimer.None,
                playbackSpeed = 1f,
                isCasting = false,
                progress = 0.4f,
                playedDuration = 5.seconds,
                remainingDuration = 55.minutes + 32.seconds,
            ),
            onNotExpandedHeightKnown = { },
            onGoToQueueClicked = { },
            onReplayClicked = { },
            onPauseClicked = { },
            onPlayClicked = { },
            onForwardClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
        )
    }
}


@Preview
@Composable
private fun PlayerScreenPreview_IsPlaying_Expanded() {
    AppTheme {
        PlayerScreen(
            state = PlayerViewState(
                isExpanded = true,
                isPlaying = true,
                episodeTitle = episode().title,
                episodeArtworkUrl = episode().link,
                podcastName = episode().podcastName,
                sleepTimer = SleepTimer.None,
                playbackSpeed = 1f,
                isCasting = false,
                progress = 0.4f,
                playedDuration = 5.seconds,
                remainingDuration = 55.minutes + 32.seconds,
            ),
            onNotExpandedHeightKnown = { },
            onGoToQueueClicked = { },
            onReplayClicked = { },
            onPauseClicked = { },
            onPlayClicked = { },
            onForwardClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
        )
    }
}

@Preview
@Composable
private fun PlayerScreenPreview_IsNotPlaying_Expanded() {
    AppTheme {
        PlayerScreen(
            state = PlayerViewState(
                isExpanded = true,
                isPlaying = false,
                episodeTitle = episode().title,
                episodeArtworkUrl = episode().link,
                podcastName = episode().podcastName,
                sleepTimer = SleepTimer.None,
                playbackSpeed = 1f,
                isCasting = false,
                progress = 0.4f,
                playedDuration = 5.seconds,
                remainingDuration = 55.minutes + 32.seconds,
            ),
            onNotExpandedHeightKnown = { },
            onGoToQueueClicked = { },
            onReplayClicked = { },
            onPauseClicked = { },
            onPlayClicked = { },
            onForwardClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
        )
    }
}
