package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.podcasts.android.ui.AppTheme
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode

@Composable
fun EpisodeControls(
    episode: Episode,
    isPlaying: Boolean,
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
        if (isPlaying) {
            IconButton(onClick = onPlayClicked) {
                Icon(imageVector = Icons.Filled.PlayCircleOutline, contentDescription = "")
            }
        } else {
            IconButton(onClick = onPauseClicked) {
                Icon(imageVector = Icons.Filled.PauseCircleOutline, contentDescription = "")
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
            DownloadStatus.QUEUED -> {
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
            onClick = if (episode.completedAt == null) {
                onPlayedClicked
            } else {
                onNotPlayedClicked
            },
        ) {
            Icon(imageVector = Icons.Outlined.CheckCircleOutline, contentDescription = "")
        }
    }
}

@Preview
@Composable
private fun EpisodeControlsPreview_IsPlaying() {
    AppTheme {
        EpisodeControls(
            episode(),
            isPlaying = true,
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

@Preview
@Composable
private fun EpisodeControlsPreview_IsNotPlaying() {
    AppTheme {
        EpisodeControls(
            episode(),
            isPlaying = false,
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

@Preview
@Composable
private fun EpisodeControlsPreview_IsInQueue() {
    AppTheme {
        EpisodeControls(
            episode(queuePosition = 1),
            isPlaying = false,
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

@Preview
@Composable
private fun EpisodeControlsPreview_IsNotInQueue() {
    AppTheme {
        EpisodeControls(
            episode(queuePosition = Episode.NOT_IN_QUEUE),
            isPlaying = false,
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

@Preview
@Composable
private fun EpisodeControlsPreview_IsDownloaded() {
    AppTheme {
        EpisodeControls(
            episode(downloadStatus = DownloadStatus.DOWNLOADED),
            isPlaying = false,
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

@Preview
@Composable
private fun EpisodeControlsPreview_IsNotNotDownloaded() {
    AppTheme {
        EpisodeControls(
            episode(downloadStatus = DownloadStatus.NOT_DOWNLOADED),
            isPlaying = false,
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

@Preview
@Composable
private fun EpisodeControlsPreview_IsDownloading() {
    AppTheme {
        EpisodeControls(
            episode(downloadStatus = DownloadStatus.DOWNLOADING, downloadProgress = 0.5),
            isPlaying = false,
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
