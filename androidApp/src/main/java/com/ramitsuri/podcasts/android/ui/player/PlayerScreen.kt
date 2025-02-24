package com.ramitsuri.podcasts.android.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.navigation.shareText
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.SleepTimerControl
import com.ramitsuri.podcasts.android.ui.components.SpeedControl
import com.ramitsuri.podcasts.android.ui.components.SquigglySlider
import com.ramitsuri.podcasts.android.utils.sharePodcast
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.PlayerViewState
import com.ramitsuri.podcasts.model.ui.SleepTimer
import java.util.Locale
import kotlin.time.Duration

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
    onPlaybackSpeedSet: (Int) -> Unit,
    onToggleTrimSilence: () -> Unit,
    onEndOfEpisodeTimerSet: () -> Unit,
    onCustomTimerSet: (Int) -> Unit,
    onTimerCanceled: () -> Unit,
    onTimerIncrement: () -> Unit,
    onTimerDecrement: () -> Unit,
    onNotExpandedPlayerClicked: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
) {
    val alphaExpandedPlayer: Float by animateFloatAsState(if (isExpanded) 1f else 0f, label = "player visibility")
    val alphaNotExpandedPlayer: Float by animateFloatAsState(if (isExpanded) 0f else 1f, label = "player visibility")
    Box(
        modifier =
            modifier
                .fillMaxWidth(),
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
                modifier =
                    Modifier
                        .padding(16.dp)
                        .alpha(alphaExpandedPlayer),
                isExpanded = isExpanded,
                shareText = state.episode.shareText(),
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
                onToggleTrimSilence = onToggleTrimSilence,
                onEndOfEpisodeTimerSet = onEndOfEpisodeTimerSet,
                onCustomTimerSet = onCustomTimerSet,
                onTimerCanceled = onTimerCanceled,
                onTimerIncrement = onTimerIncrement,
                onTimerDecrement = onTimerDecrement,
                onFavoriteClicked = onFavoriteClicked,
                onNotFavoriteClicked = onNotFavoriteClicked,
            )
        }
    }
}

@Composable
private fun PlayerScreenExpanded(
    modifier: Modifier = Modifier,
    shareText: String,
    isExpanded: Boolean,
    episodeTitle: String,
    episodeArtwork: String,
    podcastName: String,
    playingState: PlayingState,
    playedDuration: Duration,
    remainingDuration: Duration?,
    playProgress: Float,
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    playbackSpeed: Int,
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
    onPlaybackSpeedSet: (Int) -> Unit,
    onToggleTrimSilence: () -> Unit,
    onEndOfEpisodeTimerSet: () -> Unit,
    onCustomTimerSet: (Int) -> Unit,
    onTimerCanceled: () -> Unit,
    onTimerIncrement: () -> Unit,
    onTimerDecrement: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
) {
    var showSleepTimerControl by remember { mutableStateOf(false) }
    var sleepTimerControlHeight by remember { mutableIntStateOf(0) }

    var showSpeedControl by remember { mutableStateOf(false) }
    var speedControlHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(isExpanded) {
        if (!isExpanded) {
            showSleepTimerControl = false
            showSpeedControl = false
        }
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Player(
            disableUI = showSleepTimerControl || showSpeedControl,
            yOffset =
                if (showSleepTimerControl) {
                    sleepTimerControlHeight
                } else if (showSpeedControl) {
                    speedControlHeight
                } else {
                    0
                },
            episodeTitle = episodeTitle,
            episodeArtwork = episodeArtwork,
            podcastName = podcastName,
            playingState = playingState,
            playedDuration = playedDuration,
            remainingDuration = remainingDuration,
            playProgress = playProgress,
            sleepTimer = sleepTimer,
            sleepTimerDuration = sleepTimerDuration,
            playbackSpeed = playbackSpeed,
            isCasting = isCasting,
            isFavorite = isFavorite,
            shareText = shareText,
            onEpisodeTitleClicked = onEpisodeTitleClicked,
            onPodcastNameClicked = onPodcastNameClicked,
            onGoToQueueClicked = onGoToQueueClicked,
            onReplayClicked = onReplayClicked,
            onPauseClicked = onPauseClicked,
            onPlayClicked = onPlayClicked,
            onSkipClicked = onSkipClicked,
            onSeekValueChange = onSeekValueChange,
            onFavoriteClicked = onFavoriteClicked,
            onNotFavoriteClicked = onNotFavoriteClicked,
            onShowSleepControl = { showSleepTimerControl = it },
            onShowSpeedControl = { showSpeedControl = it },
        )
        if (showSleepTimerControl) {
            SleepTimer(
                sleepTimer = sleepTimer,
                sleepTimerDuration = sleepTimerDuration,
                onTimerDecrement = onTimerDecrement,
                onTimerIncrement = onTimerIncrement,
                onTimerCanceled = onTimerCanceled,
                onEndOfEpisodeTimerSet = onEndOfEpisodeTimerSet,
                onCustomTimerSet = onCustomTimerSet,
                onHideSleepTimerControl = { showSleepTimerControl = false },
                onSleepTimerControlHeightKnown = { sleepTimerControlHeight = it },
            )
        }
        if (showSpeedControl) {
            Speed(
                speed = playbackSpeed,
                trimSilence = trimSilence,
                onSpeedSet = onPlaybackSpeedSet,
                onTrimSilence = onToggleTrimSilence,
                onHideSpeedControl = { showSpeedControl = false },
                onSpeedControlHeightKnown = { speedControlHeight = it },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Speed(
    speed: Int,
    trimSilence: Boolean,
    onSpeedSet: (Int) -> Unit,
    onTrimSilence: () -> Unit,
    onHideSpeedControl: () -> Unit,
    onSpeedControlHeightKnown: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .onGloballyPositioned {
                    onSpeedControlHeightKnown(it.size.height)
                }
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpeedControl(
            selectedValue = speed,
            trimSilence = trimSilence,
            onSpeedSet = onSpeedSet,
            onTrimSilence = onTrimSilence,
        )
        Spacer(modifier = Modifier.height(16.dp))
        CloseButton(onClick = { onHideSpeedControl() })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SleepTimer(
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    onTimerDecrement: () -> Unit,
    onTimerIncrement: () -> Unit,
    onTimerCanceled: () -> Unit,
    onEndOfEpisodeTimerSet: () -> Unit,
    onCustomTimerSet: (Int) -> Unit,
    onHideSleepTimerControl: () -> Unit,
    onSleepTimerControlHeightKnown: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .onGloballyPositioned {
                    onSleepTimerControlHeightKnown(it.size.height)
                }
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SleepTimerControl(
            sleepTimer = sleepTimer,
            sleepTimerDuration = sleepTimerDuration,
            onEndOfEpisodeTimerSet = {
                onEndOfEpisodeTimerSet()
                onHideSleepTimerControl()
            },
            onTimerCanceled = {
                onTimerCanceled()
                onHideSleepTimerControl()
            },
            onTimerDecrement = {
                onTimerDecrement()
            },
            onTimerIncrement = {
                onTimerIncrement()
            },
            onCustomTimerSet = {
                onCustomTimerSet(it)
                onHideSleepTimerControl()
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        CloseButton(onClick = { onHideSleepTimerControl() })
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Player(
    disableUI: Boolean,
    yOffset: Int,
    episodeTitle: String,
    episodeArtwork: String,
    podcastName: String,
    playingState: PlayingState,
    playedDuration: Duration,
    remainingDuration: Duration?,
    playProgress: Float,
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    playbackSpeed: Int,
    isCasting: Boolean,
    isFavorite: Boolean,
    shareText: String,
    onEpisodeTitleClicked: () -> Unit,
    onPodcastNameClicked: () -> Unit,
    onGoToQueueClicked: () -> Unit,
    onReplayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onSeekValueChange: (Float) -> Unit,
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
    onShowSleepControl: (Boolean) -> Unit,
    onShowSpeedControl: (Boolean) -> Unit,
) {
    Box {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .alpha(if (disableUI) 0.3f else 1f)
                    .offset(
                        y =
                            with(LocalDensity.current) {
                                (-yOffset).toDp()
                            },
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                url = episodeArtwork,
                contentDescription = episodeTitle,
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
                playing = playingState == PlayingState.PLAYING,
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
                sleepTimer = sleepTimer,
                sleepTimerDuration = sleepTimerDuration,
                isCasting = isCasting,
                shareText = shareText,
                onShowSleepControl = onShowSleepControl,
                onShowSpeedControl = onShowSpeedControl,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (disableUI) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier =
                    Modifier
                        .offset(
                            y =
                                with(LocalDensity.current) {
                                    (-yOffset).toDp()
                                },
                        )
                        .matchParentSize()
                        .clickable(interactionSource = interactionSource, indication = null) { },
            )
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.close))
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
    val view = LocalView.current
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
                FilledIconButton(
                    onClick = {
                        onPauseClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_OFF)
                    },
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        modifier = Modifier.size(32.dp),
                        contentDescription = stringResource(id = R.string.pause),
                    )
                }
            }

            PlayingState.NOT_PLAYING -> {
                FilledIconButton(
                    onClick = {
                        onPlayClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_ON)
                    },
                    modifier = Modifier.size(56.dp),
                ) {
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
    modifier: Modifier = Modifier,
    playbackSpeed: Int,
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    isCasting: Boolean,
    shareText: String,
    onShowSpeedControl: (Boolean) -> Unit,
    onShowSleepControl: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        SpeedControlButton(
            playbackSpeed = playbackSpeed,
            onToggleMenu = {
                onShowSpeedControl(true)
            },
        )
        SleepTimerControlButton(
            sleepTimer = sleepTimer,
            sleepTimerDuration = sleepTimerDuration,
            onToggleMenu = {
                onShowSleepControl(true)
            },
        )
        ShareButton(shareText)

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
private fun ShareButton(shareText: String) {
    val context = LocalContext.current
    IconButton(onClick = { context.sharePodcast(shareText) }) {
        Icon(
            imageVector = Icons.Filled.Share,
            modifier =
                Modifier
                    .size(24.dp),
            contentDescription = stringResource(id = R.string.episode_controller_share),
        )
    }
}

@Composable
private fun SpeedControlButton(
    playbackSpeed: Int,
    onToggleMenu: () -> Unit,
) {
    Box {
        TextButton(
            onClick = onToggleMenu,
            colors = ButtonDefaults.textButtonColors().copy(contentColor = MaterialTheme.colorScheme.onBackground),
        ) {
            Text(text = "${playbackSpeed.div(10)}.${playbackSpeed.mod(10)}x")
        }
    }
}

@Composable
private fun SleepTimerControlButton(
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Seekbar(
    playing: Boolean,
    playedDuration: Duration,
    remainingDuration: Duration?,
    playProgress: Float,
    onSeekValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SquigglySlider(
            value = playProgress,
            onValueChange = onSeekValueChange,
            textStart = playedDuration.formatted(),
            textEnd = if (remainingDuration != null) "-${remainingDuration.formatted()}" else "",
            squigglesSpec =
                if (playing) {
                    SquigglySlider.SquigglesSpec()
                } else {
                    SquigglySlider.SquigglesSpec(amplitude = 0.dp)
                },
        )
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
    val view = LocalView.current
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
            Image(
                url = episodeArtwork,
                contentDescription = episodeTitle,
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
                    IconButton(
                        onClick = {
                            onPauseClicked()
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_OFF)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = stringResource(id = R.string.pause),
                        )
                    }
                }

                PlayingState.NOT_PLAYING -> {
                    IconButton(
                        onClick = {
                            onPlayClicked()
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_ON)
                        },
                    ) {
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
                        sleepTimer = SleepTimer.None,
                        sleepTimerDuration = null,
                        playbackSpeed = 10,
                        isCasting = false,
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
                onToggleTrimSilence = { },
                onTimerDecrement = { },
                onTimerIncrement = { },
                onTimerCanceled = { },
                onEndOfEpisodeTimerSet = { },
                onCustomTimerSet = { },
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
                        sleepTimer = SleepTimer.None,
                        sleepTimerDuration = null,
                        playbackSpeed = 10,
                        isCasting = false,
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
                onToggleTrimSilence = { },
                onTimerDecrement = { },
                onTimerIncrement = { },
                onTimerCanceled = { },
                onEndOfEpisodeTimerSet = { },
                onCustomTimerSet = { },
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
                        playingState = PlayingState.PLAYING,
                        sleepTimer = SleepTimer.None,
                        sleepTimerDuration = null,
                        playbackSpeed = 10,
                        isCasting = false,
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
                onToggleTrimSilence = { },
                onTimerDecrement = { },
                onTimerIncrement = { },
                onTimerCanceled = { },
                onEndOfEpisodeTimerSet = { },
                onCustomTimerSet = { },
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
                    sleepTimer = SleepTimer.None,
                    sleepTimerDuration = null,
                    playbackSpeed = 10,
                    isCasting = false,
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
            onToggleTrimSilence = { },
            onTimerDecrement = { },
            onTimerIncrement = { },
            onTimerCanceled = { },
            onEndOfEpisodeTimerSet = { },
            onCustomTimerSet = { },
            onNotExpandedPlayerClicked = { },
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
        )
    }
}
