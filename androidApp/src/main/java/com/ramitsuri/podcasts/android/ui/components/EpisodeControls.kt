package com.ramitsuri.podcasts.android.ui.components

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.greenColor
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
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayStateButton(
            playingState = playingState,
            hasBeenPlayed = episode.progressInSeconds > 0,
            remainingDuration = episode.remainingDuration,
            onClick = {
                when (playingState) {
                    PlayingState.PLAYING -> onPauseClicked()
                    PlayingState.NOT_PLAYING -> onPlayClicked()
                    PlayingState.LOADING -> { }
                }
            },
        )
        if (episode.queuePosition == Episode.NOT_IN_QUEUE) {
            ControlWithTooltip(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                toolTipLabelRes = R.string.episode_controller_add_to_queue,
                onClicked = onAddToQueueClicked,
            )
        } else {
            ControlWithTooltip(
                icon = Icons.Filled.CheckCircle,
                toolTipLabelRes = R.string.episode_controller_remove_from_queue,
                onClicked = onRemoveFromQueueClicked,
                useTint = true,
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
                    icon = Icons.Filled.DownloadForOffline,
                    toolTipLabelRes = R.string.episode_controller_remove_download,
                    onClicked = onRemoveDownloadClicked,
                    useTint = true
                )
            }
        }
        if (episode.isFavorite) {
            ControlWithTooltip(
                icon = Icons.Filled.Favorite,
                toolTipLabelRes = R.string.episode_controller_mark_not_favorite,
                onClicked = onNotFavoriteClicked,
            )
        } else {
            ControlWithTooltip(
                icon = Icons.Filled.FavoriteBorder,
                toolTipLabelRes = R.string.episode_controller_mark_favorite,
                onClicked = onFavoriteClicked,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        EpisodeMenu(
            showMenu = showMenu,
            onToggleMenu = { showMenu = !showMenu },
            episodeCompleted = episode.isCompleted,
            podcastTitle = episode.podcastName,
            episodeTitle = episode.title,
            onPlayedClicked = onPlayedClicked,
            onNotPlayedClicked = onNotPlayedClicked,
        )
    }
}

@Composable
private fun EpisodeMenu(
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    episodeCompleted: Boolean,
    podcastTitle: String,
    episodeTitle: String,
    onPlayedClicked: () -> Unit,
    onNotPlayedClicked: () -> Unit,
) {
    val sendIntent =
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "$podcastTitle: $episodeTitle")
            type = "text/plain"
        }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current

    Box {
        IconButton(onClick = { onToggleMenu() }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                modifier =
                Modifier
                    .size(24.dp),
                contentDescription = stringResource(id = R.string.menu),
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onToggleMenu,
        ) {
            if (episodeCompleted) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.episode_controller_mark_not_played)) },
                    onClick = {
                        onToggleMenu()
                        onNotPlayedClicked()
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.episode_controller_mark_played)) },
                    onClick = {
                        onToggleMenu()
                        onPlayedClicked()
                    },
                )
            }
            DropdownMenuItem(
                text = {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = {
                                        context.startActivity(shareIntent)
                                    },
                                ),
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.episode_controller_mark_played))
                    }
                },
                onClick = {
                    onToggleMenu()
                    onPlayedClicked()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlWithTooltip(
    icon: ImageVector,
    @StringRes toolTipLabelRes: Int,
    useTint: Boolean = false,
    onClicked: () -> Unit,
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
            if (useTint) {
                Icon(imageVector = icon, contentDescription = "", tint = greenColor)
            } else {
                Icon(imageVector = icon, contentDescription = "")
            }
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsFavorite() {
    PreviewTheme {
        EpisodeControls(
            episode(isFavorite = true),
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun EpisodeControlsPreview_IsNotFavorite() {
    PreviewTheme {
        EpisodeControls(
            episode(isFavorite = false),
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
            onFavoriteClicked = { },
            onNotFavoriteClicked = { },
        )
    }
}
