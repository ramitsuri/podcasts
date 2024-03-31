package com.ramitsuri.podcasts.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState

@Composable
fun EpisodeControls(
    episode: Episode,
    playingState: PlayingState,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onAddToQueueClicked: () -> Unit,
    onRemoveFromQueueClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    onRemoveDownloadClicked: () -> Unit,
    onCancelDownloadClicked: () -> Unit,
    onPlayedClicked: () -> Unit,
    onNotPlayedClicked: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        when (playingState) {
            PlayingState.PLAYING -> {
                ControlWithTooltip(
                    icon = Icons.Filled.PauseCircleOutline,
                    toolTipLabelRes = R.string.pause,
                    onClicked = onPauseClicked,
                )
            }

            PlayingState.NOT_PLAYING -> {
                ControlWithTooltip(
                    icon = Icons.Filled.PlayCircleOutline,
                    toolTipLabelRes = R.string.play,
                    onClicked = onPlayClicked,
                )
            }

            PlayingState.LOADING -> {
                IconButton(onClick = { }) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
        if (episode.queuePosition == Episode.NOT_IN_QUEUE) {
            ControlWithTooltip(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                toolTipLabelRes = R.string.episode_controller_add_to_queue,
                onClicked = onAddToQueueClicked,
            )
        } else {
            ControlWithTooltip(
                icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                toolTipLabelRes = R.string.episode_controller_remove_from_queue,
                onClicked = onRemoveFromQueueClicked,
            )
        }
        when (episode.downloadStatus) {
            DownloadStatus.NOT_DOWNLOADED -> {
                ControlWithTooltip(
                    icon = Icons.Outlined.ArrowCircleDown,
                    toolTipLabelRes = R.string.episode_controller_download,
                    onClicked = onDownloadClicked,
                )
            }

            DownloadStatus.PAUSED,
            DownloadStatus.DOWNLOADING,
            DownloadStatus.QUEUED,
            -> {
                ControlWithTooltip(
                    icon = Icons.Outlined.Downloading,
                    toolTipLabelRes = R.string.episode_controller_downloading,
                    onClicked = onCancelDownloadClicked,
                )
            }

            DownloadStatus.DOWNLOADED -> {
                ControlWithTooltip(
                    icon = Icons.Outlined.DownloadDone,
                    toolTipLabelRes = R.string.episode_controller_remove_download,
                    onClicked = onRemoveDownloadClicked,
                )
            }
        }
        ControlWithTooltip(
            icon = Icons.Outlined.CheckCircleOutline,
            toolTipLabelRes = if (episode.isCompleted) {
                R.string.episode_controller_mark_not_played
            } else {
                R.string.episode_controller_mark_played
            },
            onClicked = if (episode.isCompleted) {
                onNotPlayedClicked
            } else {
                onPlayedClicked
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlWithTooltip(
    icon: ImageVector,
    @StringRes toolTipLabelRes: Int,
    onClicked: () -> Unit
) {
    val state = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(stringResource(id = toolTipLabelRes))
            }
        },
        state = state,
    ) {
        IconButton(onClick = onClicked) {
            Icon(imageVector = icon, contentDescription = "")
        }
    }

}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsPlaying() {
    PreviewTheme {
        EpisodeControls(
            episode(),
            playingState = PlayingState.PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsNotPlaying() {
    PreviewTheme {
        EpisodeControls(
            episode(),
            playingState = PlayingState.NOT_PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsLoading() {
    PreviewTheme {
        EpisodeControls(
            episode(),
            playingState = PlayingState.LOADING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsInQueue() {
    PreviewTheme {
        EpisodeControls(
            episode(queuePosition = 1),
            playingState = PlayingState.NOT_PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsNotInQueue() {
    PreviewTheme {
        EpisodeControls(
            episode(queuePosition = Episode.NOT_IN_QUEUE),
            playingState = PlayingState.NOT_PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsDownloaded() {
    PreviewTheme {
        EpisodeControls(
            episode(downloadStatus = DownloadStatus.DOWNLOADED),
            playingState = PlayingState.NOT_PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsNotNotDownloaded() {
    PreviewTheme {
        EpisodeControls(
            episode(downloadStatus = DownloadStatus.NOT_DOWNLOADED),
            playingState = PlayingState.NOT_PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsDownloading() {
    PreviewTheme {
        EpisodeControls(
            episode(downloadStatus = DownloadStatus.DOWNLOADING, downloadProgress = 0.5),
            playingState = PlayingState.NOT_PLAYING,
            onPlayClicked = { },
            onPauseClicked = { },
            onAddToQueueClicked = { },
            onRemoveFromQueueClicked = { },
            onDownloadClicked = { },
            onRemoveDownloadClicked = { },
            onCancelDownloadClicked = { },
            onPlayedClicked = { },
            onNotPlayedClicked = { },
        )
    }
}
