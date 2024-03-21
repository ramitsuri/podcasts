package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                IconButton(onClick = onPauseClicked) {
                    Icon(imageVector = Icons.Filled.PauseCircleOutline, contentDescription = "")
                }
            }

            PlayingState.NOT_PLAYING -> {
                IconButton(onClick = onPlayClicked) {
                    Icon(imageVector = Icons.Filled.PlayCircleOutline, contentDescription = "")
                }
            }

            PlayingState.LOADING -> {
                IconButton(onClick = { }) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
        if (episode.queuePosition == Episode.NOT_IN_QUEUE) {
            IconButton(onClick = onAddToQueueClicked) {
                Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "")
            }
        } else {
            IconButton(onClick = onRemoveFromQueueClicked) {
                Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = "")
            }
        }
        when (episode.downloadStatus) {
            DownloadStatus.NOT_DOWNLOADED -> {
                IconButton(onClick = onDownloadClicked) {
                    Icon(imageVector = Icons.Outlined.DownloadForOffline, contentDescription = "")
                }
            }

            DownloadStatus.PAUSED,
            DownloadStatus.DOWNLOADING,
            DownloadStatus.QUEUED,
            -> {
                IconButton(onClick = onCancelDownloadClicked) {
                    Icon(imageVector = Icons.Outlined.Downloading, contentDescription = "")
                }
            }

            DownloadStatus.DOWNLOADED -> {
                IconButton(onClick = onRemoveDownloadClicked) {
                    Icon(imageVector = Icons.Outlined.DownloadDone, contentDescription = "")
                }
            }
        }
        IconButton(
            onClick =
                if (episode.completedAt == null) {
                    onPlayedClicked
                } else {
                    onNotPlayedClicked
                },
        ) {
            Icon(imageVector = Icons.Outlined.CheckCircleOutline, contentDescription = "")
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
