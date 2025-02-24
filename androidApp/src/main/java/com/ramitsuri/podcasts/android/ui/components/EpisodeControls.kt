package com.ramitsuri.podcasts.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.navigation.shareText
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.greenColor
import com.ramitsuri.podcasts.android.utils.sharePodcast
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
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayStateButton(
            playingState = playingState,
            hasBeenPlayed = episode.progressInSeconds > 0,
            remainingDuration = episode.remainingDuration,
            isCompleted = episode.isCompleted,
            onClick = {
                when (playingState) {
                    PlayingState.PLAYING -> {
                        onPauseClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_OFF)
                    }

                    PlayingState.NOT_PLAYING -> {
                        onPlayClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.TOGGLE_ON)
                    }

                    PlayingState.LOADING -> {}
                }
            },
        )
        QueueButton(
            queuePosition = episode.queuePosition,
            onAddToQueueClicked = onAddToQueueClicked,
            onRemoveFromQueueClicked = onRemoveFromQueueClicked,
        )
        DownloadButton(
            downloadStatus = episode.downloadStatus,
            onDownloadClicked = onDownloadClicked,
            onCancelDownloadClicked = onCancelDownloadClicked,
            onRemoveDownloadClicked = onRemoveDownloadClicked,
        )
        Spacer(modifier = Modifier.weight(1f))
        EpisodeMenu(
            showMenu = showMenu,
            onToggleMenu = { showMenu = !showMenu },
            episodeCompleted = episode.isCompleted,
            shareText = episode.shareText(),
            isFavorite = episode.isFavorite,
            onFavoriteClicked = onFavoriteClicked,
            onNotFavoriteClicked = onNotFavoriteClicked,
            onPlayedClicked = onPlayedClicked,
            onNotPlayedClicked = onNotPlayedClicked,
        )
    }
}

@Composable
private fun QueueButton(
    queuePosition: Int,
    onAddToQueueClicked: () -> Unit,
    onRemoveFromQueueClicked: () -> Unit,
) {
    val view = LocalView.current
    if (queuePosition == Episode.NOT_IN_QUEUE) {
        ControlWithTooltip(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            toolTipLabelRes = R.string.episode_controller_add_to_queue,
            onClicked = {
                onAddToQueueClicked()
                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            },
        )
    } else {
        ControlWithTooltip(
            icon = Icons.Filled.CheckCircle,
            toolTipLabelRes = R.string.episode_controller_remove_from_queue,
            onClicked = {
                onRemoveFromQueueClicked()
                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
            },
            useTint = true,
        )
    }
}

@Composable
private fun DownloadButton(
    downloadStatus: DownloadStatus,
    onDownloadClicked: () -> Unit,
    onCancelDownloadClicked: () -> Unit,
    onRemoveDownloadClicked: () -> Unit,
) {
    val view = LocalView.current
    when (downloadStatus) {
        DownloadStatus.NOT_DOWNLOADED -> {
            ControlWithTooltip(
                icon = Icons.Outlined.ArrowCircleDown,
                toolTipLabelRes = R.string.episode_controller_download,
                onClicked = {
                    onDownloadClicked()
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                },
            )
        }

        DownloadStatus.PAUSED,
        DownloadStatus.DOWNLOADING,
        DownloadStatus.QUEUED,
        -> {
            ControlWithTooltip(
                icon = Icons.Outlined.Downloading,
                toolTipLabelRes = R.string.episode_controller_downloading,
                onClicked = {
                    onCancelDownloadClicked()
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                },
            )
        }

        DownloadStatus.DOWNLOADED -> {
            ControlWithTooltip(
                icon = ImageVector.vectorResource(R.drawable.ic_arrow_circle_down),
                toolTipLabelRes = R.string.episode_controller_remove_download,
                onClicked = {
                    onRemoveDownloadClicked()
                    view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                },
                useTint = true,
            )
        }
    }
}

@Composable
private fun EpisodeMenu(
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    episodeCompleted: Boolean,
    isFavorite: Boolean,
    shareText: String,
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
    onPlayedClicked: () -> Unit,
    onNotPlayedClicked: () -> Unit,
) {
    val context = LocalContext.current

    Box {
        IconButton(onClick = { onToggleMenu() }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                modifier =
                    Modifier
                        .size(24.dp),
                contentDescription = stringResource(id = R.string.menu),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        BottomSheetDialog(
            show = showMenu,
            onDismissRequest = onToggleMenu,
        ) {
            if (isFavorite) {
                BottomSheetDialogMenuItem(
                    icon = Icons.Filled.Favorite,
                    text = stringResource(id = R.string.episode_controller_mark_not_favorite),
                    onClick = {
                        onToggleMenu()
                        onNotFavoriteClicked()
                    },
                )
            } else {
                BottomSheetDialogMenuItem(
                    icon = Icons.Filled.FavoriteBorder,
                    text = stringResource(id = R.string.episode_controller_mark_favorite),
                    onClick = {
                        onToggleMenu()
                        onFavoriteClicked()
                    },
                )
            }
            if (episodeCompleted) {
                BottomSheetDialogMenuItem(
                    icon = Icons.Filled.Check,
                    text = stringResource(id = R.string.episode_controller_mark_not_played),
                    onClick = {
                        onToggleMenu()
                        onNotPlayedClicked()
                    },
                )
            } else {
                BottomSheetDialogMenuItem(
                    icon = Icons.Filled.Check,
                    text = stringResource(id = R.string.episode_controller_mark_played),
                    onClick = {
                        onToggleMenu()
                        onPlayedClicked()
                    },
                )
            }
            BottomSheetDialogMenuItem(
                icon = Icons.Filled.Share,
                text = stringResource(id = R.string.episode_controller_share),
                onClick = {
                    onToggleMenu()
                    context.sharePodcast(shareText)
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
            val color =
                if (useTint) {
                    greenColor
                } else {
                    MaterialTheme.colorScheme.primary
                }
            Icon(imageVector = icon, contentDescription = "", tint = color)
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
