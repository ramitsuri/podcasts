package com.ramitsuri.podcasts.android.ui.player

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.PlayerViewState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerScreen(
    isExpanded: Boolean,
    state: PlayerViewState,
    modifier: Modifier = Modifier,
    onNotExpandedHeightKnown: (Int) -> Unit,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onSeekValueChange: (Float) -> Unit,
    onPlaybackSpeedSet: (Float) -> Unit,
    onPlaybackSpeedIncrease: () -> Unit,
    onPlaybackSpeedDecrease: () -> Unit,
    onToggleTrimSilence: () -> Unit,
    onSleepTimer: (SleepTimer) -> Unit,
    onSleepTimerIncrease: () -> Unit,
    onSleepTimerDecrease: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(if (isExpanded) 16.dp else 0.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (!isExpanded) {
            if (state.hasEverBeenPlayed) {
                PlayerScreenNotExpanded(
                    modifier =
                        Modifier.onGloballyPositioned {
                            onNotExpandedHeightKnown(it.size.height)
                        },
                    episodeTitle = state.episodeTitle,
                    episodeArtwork = state.episodeArtworkUrl,
                    playingState = state.playingState,
                    playProgress = state.progress,
                    onPlayClicked = onPlayClicked,
                    onPauseClicked = onPauseClicked,
                )
            } else {
                NeverPlayedNotExpanded(
                    modifier =
                        Modifier.onGloballyPositioned {
                            onNotExpandedHeightKnown(it.size.height)
                        },
                )
            }
        }
        if (state.hasEverBeenPlayed) {
            PlayerScreenExpanded(
                modifier = Modifier.alpha(if (isExpanded) 1f else 0f),
                episodeTitle = state.episodeTitle,
                episodeArtwork = state.episodeArtworkUrl,
                podcastName = state.podcastName,
                playingState = state.playingState,
                playedDuration = state.playedDuration,
                remainingDuration = state.remainingDuration,
                playProgress = state.progress,
                sleepTimer = state.sleepTimer,
                sleepTimerDuration = state.sleepTimerDuration,
                playbackSpeed = state.playbackSpeed,
                trimSilence = state.trimSilence,
                isCasting = state.isCasting,
                onGoToQueueClicked = onGoToQueueClicked,
                onReplayClicked = onReplayClicked,
                onPauseClicked = onPauseClicked,
                onPlayClicked = onPlayClicked,
                onSkipClicked = onSkipClicked,
                onSeekValueChange = onSeekValueChange,
                onPlaybackSpeedSet = onPlaybackSpeedSet,
                onPlaybackSpeedIncrease = onPlaybackSpeedIncrease,
                onPlaybackSpeedDecrease = onPlaybackSpeedDecrease,
                onToggleTrimSilence = onToggleTrimSilence,
                onSleepTimer = onSleepTimer,
                onSleepTimerIncrease = onSleepTimerIncrease,
                onSleepTimerDecrease = onSleepTimerDecrease,
            )
        } else {
            NeverPlayedNotExpanded()
        }
    }
}

@Composable
private fun NeverPlayedNotExpanded(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(id = R.string.player_never_played))
    }
}

@Composable
private fun PlayerScreenExpanded(
    modifier: Modifier = Modifier,
    episodeTitle: String,
    episodeArtwork: String,
    podcastName: String,
    playingState: PlayingState,
    playedDuration: Duration,
    remainingDuration: Duration?,
    playProgress: Float,
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    playbackSpeed: Float,
    trimSilence: Boolean,
    isCasting: Boolean,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onSeekValueChange: (Float) -> Unit,
    onPlaybackSpeedSet: (Float) -> Unit,
    onPlaybackSpeedIncrease: () -> Unit,
    onPlaybackSpeedDecrease: () -> Unit,
    onToggleTrimSilence: () -> Unit,
    onSleepTimer: (SleepTimer) -> Unit,
    onSleepTimerIncrease: () -> Unit,
    onSleepTimerDecrease: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(episodeArtwork)
                    .crossfade(true)
                    .build(),
            contentDescription = episodeTitle,
            contentScale = ContentScale.FillBounds,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .size(360.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = episodeTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = podcastName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Seekbar(
            playedDuration = playedDuration,
            remainingDuration = remainingDuration,
            playProgress = playProgress,
            onSeekValueChange = onSeekValueChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MainControls(
            playingState = playingState,
            onGoToQueueClicked = onGoToQueueClicked,
            onReplayClicked = onReplayClicked,
            onPauseClicked = onPauseClicked,
            onPlayClicked = onPlayClicked,
            onSkipClicked = onSkipClicked,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SecondaryControls(
            playbackSpeed = playbackSpeed,
            trimSilence = trimSilence,
            sleepTimer = sleepTimer,
            sleepTimerDuration = sleepTimerDuration,
            isCasting = isCasting,
            onPlaybackSpeedSet = onPlaybackSpeedSet,
            onPlaybackSpeedIncrease = onPlaybackSpeedIncrease,
            onPlaybackSpeedDecrease = onPlaybackSpeedDecrease,
            onToggleTrimSilence = onToggleTrimSilence,
            onSleepTimer = onSleepTimer,
            onSleepTimerIncrease = onSleepTimerIncrease,
            onSleepTimerDecrease = onSleepTimerDecrease,
        )
    }
}

@Composable
private fun MainControls(
    playingState: PlayingState,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onSkipClicked: () -> Unit,
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
        when (playingState) {
            PlayingState.PLAYING -> {
                FilledIconButton(onClick = onPauseClicked, modifier = Modifier.size(56.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        modifier = Modifier.size(32.dp),
                        contentDescription = stringResource(id = R.string.pause),
                    )
                }
            }

            PlayingState.NOT_PLAYING -> {
                FilledIconButton(onClick = onPlayClicked, modifier = Modifier.size(56.dp)) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        modifier = Modifier.size(32.dp),
                        contentDescription = stringResource(id = R.string.pause),
                    )
                }
            }

            PlayingState.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.size(56.dp))
            }
        }
        IconButton(onClick = onSkipClicked) {
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
    trimSilence: Boolean,
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    isCasting: Boolean,
    onPlaybackSpeedSet: (Float) -> Unit,
    onPlaybackSpeedIncrease: () -> Unit,
    onPlaybackSpeedDecrease: () -> Unit,
    onToggleTrimSilence: () -> Unit,
    onSleepTimer: (SleepTimer) -> Unit,
    onSleepTimerIncrease: () -> Unit,
    onSleepTimerDecrease: () -> Unit,
) {
    var showSpeedControl by remember { mutableStateOf(false) }
    var showSleepTimerControl by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        SpeedControl(
            playbackSpeed = playbackSpeed,
            trimSilence = trimSilence,
            showPlaybackControl = showSpeedControl,
            onPlaybackSpeedSetRequested = onPlaybackSpeedSet,
            onPlaybackSpeedIncrease = onPlaybackSpeedIncrease,
            onPlaybackSpeedDecrease = onPlaybackSpeedDecrease,
            onToggleTrimSilence = onToggleTrimSilence,
            onToggleMenu = { showSpeedControl = !showSpeedControl },
        )
        SleepTimerControl(
            sleepTimer = sleepTimer,
            sleepTimerDuration = sleepTimerDuration,
            showSleepTimerControl = showSleepTimerControl,
            onSleepTimer = onSleepTimer,
            onSleepTimerIncrease = onSleepTimerIncrease,
            onSleepTimerDecrease = onSleepTimerDecrease,
            onToggleMenu = { showSleepTimerControl = !showSleepTimerControl },
        )
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
fun SpeedControl(
    playbackSpeed: Float,
    trimSilence: Boolean,
    showPlaybackControl: Boolean,
    onPlaybackSpeedSetRequested: (Float) -> Unit,
    onPlaybackSpeedIncrease: () -> Unit,
    onPlaybackSpeedDecrease: () -> Unit,
    onToggleMenu: () -> Unit,
    onToggleTrimSilence: () -> Unit,
) {
    Box {
        TextButton(
            onClick = onToggleMenu,
            colors = ButtonDefaults.textButtonColors().copy(contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Text(text = "${playbackSpeed}x")
        }
        DropdownMenu(
            expanded = showPlaybackControl,
            onDismissRequest = onToggleMenu,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.player_playback_speed_1x)) },
                onClick = {
                    onToggleMenu()
                    onPlaybackSpeedSetRequested(1f)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.player_playback_speed_1_5x)) },
                onClick = {
                    onToggleMenu()
                    onPlaybackSpeedSetRequested(1.5f)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.player_playback_speed_2x)) },
                onClick = {
                    onToggleMenu()
                    onPlaybackSpeedSetRequested(2.0f)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.player_playback_speed_increase)) },
                onClick = onPlaybackSpeedIncrease,
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.player_playback_speed_decrease)) },
                onClick = onPlaybackSpeedDecrease,
            )
            if (trimSilence) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.player_trim_silence_disable)) },
                    onClick = {
                        onToggleMenu()
                        onToggleTrimSilence()
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.player_trim_silence_enable)) },
                    onClick = {
                        onToggleMenu()
                        onToggleTrimSilence()
                    },
                )
            }
        }
    }
}

@Composable
fun SleepTimerControl(
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    showSleepTimerControl: Boolean,
    onSleepTimer: (SleepTimer) -> Unit,
    onSleepTimerIncrease: () -> Unit,
    onSleepTimerDecrease: () -> Unit,
    onToggleMenu: () -> Unit,
) {
    Box {
        if (sleepTimer is SleepTimer.None) {
            IconButton(onClick = { onToggleMenu() }) {
                Icon(
                    imageVector = Icons.Outlined.Nightlight,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .rotate(-30f),
                    contentDescription = stringResource(id = R.string.player_sleep_timer),
                )
            }
        } else if (sleepTimerDuration != null) {
            TextButton(onClick = { onToggleMenu() }) {
                Text(text = sleepTimerDuration.formatted())
            }
        }
        DropdownMenu(
            expanded = showSleepTimerControl,
            onDismissRequest = onToggleMenu,
        ) {
            when (sleepTimer) {
                is SleepTimer.None -> {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.player_sleep_timer_15_min)) },
                        onClick = {
                            onToggleMenu()
                            onSleepTimer(SleepTimer.Custom(Clock.System.now().plus(15.minutes)))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.player_sleep_timer_30_min)) },
                        onClick = {
                            onToggleMenu()
                            onSleepTimer(SleepTimer.Custom(Clock.System.now().plus(30.minutes)))
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.player_sleep_timer_episode_end)) },
                        onClick = {
                            onToggleMenu()
                            onSleepTimer(SleepTimer.EndOfEpisode)
                        },
                    )
                }

                SleepTimer.EndOfEpisode -> {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.cancel)) },
                        onClick = {
                            onToggleMenu()
                            onSleepTimer(SleepTimer.None)
                        },
                    )
                }

                else -> {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.player_sleep_timer_decrease)) },
                        onClick = onSleepTimerDecrease,
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.player_sleep_timer_increase)) },
                        onClick = onSleepTimerIncrease,
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.cancel)) },
                        onClick = {
                            onToggleMenu()
                            onSleepTimer(SleepTimer.None)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Seekbar(
    playedDuration: Duration,
    remainingDuration: Duration?,
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
            Text(text = playedDuration.formatted(), style = MaterialTheme.typography.bodySmall)
            if (remainingDuration != null) {
                Text(text = remainingDuration.formatted(), style = MaterialTheme.typography.bodySmall)
            }
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
    playingState: PlayingState,
    playProgress: Float,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(episodeArtwork)
                        .crossfade(true)
                        .build(),
                contentDescription = episodeTitle,
                contentScale = ContentScale.FillBounds,
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .size(64.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = episodeTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            when (playingState) {
                PlayingState.PLAYING -> {
                    IconButton(onClick = onPauseClicked) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = stringResource(id = R.string.pause),
                        )
                    }
                }

                PlayingState.NOT_PLAYING -> {
                    IconButton(onClick = onPlayClicked) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = stringResource(id = R.string.play),
                        )
                    }
                }

                PlayingState.LOADING -> {
                    CircularProgressIndicator()
                }
            }
        }
        // TODO show play progress in divider
        HorizontalDivider()
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsPlaying_NotExpanded() {
    PreviewTheme {
        PlayerScreen(
            isExpanded = false,
            state =
                PlayerViewState(
                    playingState = PlayingState.PLAYING,
                    episodeTitle = episode().title,
                    episodeArtworkUrl = episode().podcastImageUrl,
                    podcastName = episode().podcastName,
                    sleepTimer = SleepTimer.None,
                    sleepTimerDuration = null,
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
            onSkipClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
            onPlaybackSpeedIncrease = { },
            onPlaybackSpeedDecrease = { },
            onToggleTrimSilence = { },
            onSleepTimer = { },
            onSleepTimerIncrease = { },
            onSleepTimerDecrease = { },
        )
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsNotPlaying_NotExpanded() {
    PreviewTheme {
        PlayerScreen(
            isExpanded = false,
            state =
                PlayerViewState(
                    playingState = PlayingState.NOT_PLAYING,
                    episodeTitle = episode().title,
                    episodeArtworkUrl = episode().podcastImageUrl,
                    podcastName = episode().podcastName,
                    sleepTimer = SleepTimer.None,
                    sleepTimerDuration = null,
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
            onSkipClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
            onPlaybackSpeedIncrease = { },
            onPlaybackSpeedDecrease = { },
            onToggleTrimSilence = { },
            onSleepTimer = { },
            onSleepTimerIncrease = { },
            onSleepTimerDecrease = { },
        )
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsPlaying_Expanded() {
    PreviewTheme {
        PlayerScreen(
            isExpanded = true,
            state =
                PlayerViewState(
                    hasEverBeenPlayed = true,
                    playingState = PlayingState.PLAYING,
                    episodeTitle = episode().title,
                    episodeArtworkUrl = episode().podcastImageUrl,
                    podcastName = episode().podcastName,
                    sleepTimer = SleepTimer.None,
                    sleepTimerDuration = null,
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
            onSkipClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
            onPlaybackSpeedIncrease = { },
            onPlaybackSpeedDecrease = { },
            onToggleTrimSilence = { },
            onSleepTimer = { },
            onSleepTimerIncrease = { },
            onSleepTimerDecrease = { },
        )
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsNotPlaying_Expanded() {
    PreviewTheme {
        PlayerScreen(
            isExpanded = true,
            state =
                PlayerViewState(
                    playingState = PlayingState.NOT_PLAYING,
                    episodeTitle = episode().title,
                    episodeArtworkUrl = episode().podcastImageUrl,
                    podcastName = episode().podcastName,
                    sleepTimer = SleepTimer.None,
                    sleepTimerDuration = null,
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
            onSkipClicked = { },
            onSeekValueChange = { },
            onPlaybackSpeedSet = { },
            onPlaybackSpeedIncrease = { },
            onPlaybackSpeedDecrease = { },
            onToggleTrimSilence = { },
            onSleepTimer = { },
            onSleepTimerIncrease = { },
            onSleepTimerDecrease = { },
        )
    }
}
