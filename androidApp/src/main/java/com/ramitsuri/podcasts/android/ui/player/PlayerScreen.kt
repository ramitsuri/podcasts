package com.ramitsuri.podcasts.android.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.geometry.Offset
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
import java.util.Locale
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
    onEpisodeTitleClicked: () -> Unit,
    onPodcastNameClicked: () -> Unit,
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
    onNotExpandedPlayerClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
) {
    val alphaExpandedPlayer: Float by animateFloatAsState(if (isExpanded) 1f else 0f, label = "player visibility")
    val alphaNotExpandedPlayer: Float by animateFloatAsState(if (isExpanded) 0f else 1f, label = "player visibility")
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
                        Modifier
                            .onGloballyPositioned {
                                onNotExpandedHeightKnown(it.size.height)
                            }
                            .alpha(alphaNotExpandedPlayer),
                    episodeTitle = state.episodeTitle,
                    episodeArtwork = state.episodeArtworkUrl,
                    playingState = state.playingState,
                    playProgress = state.progress,
                    onPlayClicked = onPlayClicked,
                    onPauseClicked = onPauseClicked,
                    onClicked = onNotExpandedPlayerClicked,
                )
            }
        }
        if (state.hasEverBeenPlayed) {
            PlayerScreenExpanded(
                modifier = Modifier.alpha(alphaExpandedPlayer),
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
                isFavorite = state.isFavorite,
                onEpisodeTitleClicked = onEpisodeTitleClicked,
                onPodcastNameClicked = onPodcastNameClicked,
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
                onFavoriteClicked = onFavoriteClicked,
                onNotFavoriteClicked = onNotFavoriteClicked,
            )
        }
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
    isFavorite: Boolean,
    onEpisodeTitleClicked: () -> Unit,
    onPodcastNameClicked: () -> Unit,
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
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
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
                    .clip(MaterialTheme.shapes.medium)
                    .size(328.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEpisodeTitleClicked),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = episodeTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPodcastNameClicked),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = podcastName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
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
            isFavorite = isFavorite,
            onGoToQueueClicked = onGoToQueueClicked,
            onReplayClicked = onReplayClicked,
            onPauseClicked = onPauseClicked,
            onPlayClicked = onPlayClicked,
            onSkipClicked = onSkipClicked,
            onFavoriteClicked = onFavoriteClicked,
            onNotFavoriteClicked = onNotFavoriteClicked,
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
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MainControls(
    playingState: PlayingState,
    isFavorite: Boolean,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
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
                contentDescription = stringResource(id = R.string.player_go_to_queue),
            )
        }
        IconButton(onClick = onReplayClicked) {
            Icon(
                imageVector = Icons.Filled.Replay10,
                modifier = Modifier.size(32.dp),
                contentDescription = stringResource(id = R.string.replay_10),
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
                        contentDescription = stringResource(id = R.string.play),
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
                contentDescription = stringResource(id = R.string.skip_30),
            )
        }
        if (isFavorite) {
            IconButton(onClick = onNotFavoriteClicked) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    modifier = Modifier.size(24.dp),
                    contentDescription = stringResource(id = R.string.episode_controller_mark_not_favorite),
                )
            }
        } else {
            IconButton(onClick = onFavoriteClicked) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    modifier = Modifier.size(24.dp),
                    contentDescription = stringResource(id = R.string.episode_controller_mark_favorite),
                )
            }
        }
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

        // TODO Hidden until implemented
        if (false) {
            if (isCasting) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.CastConnected,
                        modifier = Modifier.size(24.dp),
                        contentDescription = stringResource(id = R.string.pause),
                    )
                }
            } else {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Cast,
                        modifier = Modifier.size(24.dp),
                        contentDescription = stringResource(id = R.string.pause),
                    )
                }
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    modifier = Modifier.size(24.dp),
                    contentDescription = stringResource(id = R.string.pause),
                )
            }
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
                Text(text = "-${remainingDuration.formatted()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun Duration.formatted(): String {
    val hours = inWholeHours % 24
    return if (hours == 0L) {
        String.format(
            Locale.getDefault(),
            "%02d:%02d",
            inWholeMinutes % 60,
            inWholeSeconds % 60,
        )
    } else {
        String.format(
            Locale.getDefault(),
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
    onClicked: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClicked),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
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
                        .clip(MaterialTheme.shapes.extraSmall)
                        .size(40.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = episodeTitle,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
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
        PlayProgressNotExpanded(playProgress = playProgress)
    }
}

@Composable
private fun PlayProgressNotExpanded(playProgress: Float) {
    val playedTrackColor = MaterialTheme.colorScheme.primary
    val notPlayedTrackColor = playedTrackColor.copy(alpha = 0.2f)
    val thickness = 1.dp
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(thickness),
    ) {
        drawLine(
            color = playedTrackColor,
            strokeWidth = thickness.toPx(),
            start = Offset(0f, thickness.toPx() / 2),
            end = Offset(size.width * playProgress, thickness.toPx() / 2),
        )
        drawLine(
            color = notPlayedTrackColor,
            strokeWidth = thickness.toPx(),
            start = Offset(size.width * playProgress, thickness.toPx() / 2),
            end = Offset(size.width, thickness.toPx() / 2),
        )
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsPlaying_NotExpanded() {
    PreviewTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                onEpisodeTitleClicked = { },
                onPodcastNameClicked = { },
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
                onNotExpandedPlayerClicked = { },
                onFavoriteClicked = { },
                onNotFavoriteClicked = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsNotPlaying_NotExpanded() {
    PreviewTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                onEpisodeTitleClicked = { },
                onPodcastNameClicked = { },
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
                onNotExpandedPlayerClicked = { },
                onFavoriteClicked = { },
                onNotFavoriteClicked = { },
            )
        }
    }
}

@ThemePreview
@Composable
private fun PlayerScreenPreview_IsPlaying_Expanded() {
    PreviewTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            PlayerScreen(
                isExpanded = true,
                state =
                    PlayerViewState(
                        episodeId = "",
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
                onEpisodeTitleClicked = { },
                onPodcastNameClicked = { },
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
                onNotExpandedPlayerClicked = { },
                onFavoriteClicked = { },
                onNotFavoriteClicked = { },
            )
        }
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
            onEpisodeTitleClicked = { },
            onPodcastNameClicked = { },
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
            onNotExpandedPlayerClicked = { },
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
        )
    }
}
