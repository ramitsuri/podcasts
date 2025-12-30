package com.ramitsuri.podcasts.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.greenColor
import com.ramitsuri.podcasts.android.utils.sharePodcast
import com.ramitsuri.podcasts.android.utils.sharePodcastWithNotificationJournal
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.SharePodcastInfo
import com.ramitsuri.podcasts.navigation.sharePodcastInfo

@Composable
fun EpisodeControls(
    episode: Episode,
    playingState: PlayingState,
    allowSharingToNotificationJournal: Boolean,
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
            downloadProgress = episode.downloadProgress,
            onDownloadClicked = onDownloadClicked,
            onCancelDownloadClicked = onCancelDownloadClicked,
            onRemoveDownloadClicked = onRemoveDownloadClicked,
        )
        Spacer(modifier = Modifier.weight(1f))
        EpisodeMenu(
            showMenu = showMenu,
            allowSharingToNotificationJournal = allowSharingToNotificationJournal,
            onToggleMenu = { showMenu = !showMenu },
            episodeCompleted = episode.isCompleted,
            shareInfo = episode.sharePodcastInfo(),
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
    downloadProgress: Double,
    onDownloadClicked: () -> Unit,
    onCancelDownloadClicked: () -> Unit,
    onRemoveDownloadClicked: () -> Unit,
) {
    val view = LocalView.current
    val (toolTipLabel, onClick) =
        when (downloadStatus) {
            DownloadStatus.NOT_DOWNLOADED -> {
                stringResource(R.string.episode_controller_download) to
                    {
                        onDownloadClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                    }
            }

            DownloadStatus.PAUSED,
            DownloadStatus.QUEUED,
            -> {
                stringResource(R.string.episode_controller_queued) to
                    {
                        onCancelDownloadClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                    }
            }

            DownloadStatus.DOWNLOADING -> {
                val downloadPercent = downloadProgress.times(100).toInt().toString()
                stringResource(R.string.episode_controller_downloading, downloadPercent) to
                    {
                        onCancelDownloadClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                    }
            }

            DownloadStatus.DOWNLOADED -> {
                stringResource(R.string.episode_controller_remove_download) to
                    {
                        onRemoveDownloadClicked()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                    }
            }
        }
    ControlWithTooltip(
        toolTipLabel = toolTipLabel,
        onClicked = { onClick() },
    ) {
        DownloadingIcon(downloadStatus = downloadStatus, progress = downloadProgress)
    }
}

@Composable
private fun EpisodeMenu(
    showMenu: Boolean,
    allowSharingToNotificationJournal: Boolean,
    onToggleMenu: () -> Unit,
    episodeCompleted: Boolean,
    isFavorite: Boolean,
    shareInfo: SharePodcastInfo?,
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
                    startIcon = Icons.Filled.Favorite,
                    text = stringResource(id = R.string.episode_controller_mark_not_favorite),
                    onClick = {
                        onToggleMenu()
                        onNotFavoriteClicked()
                    },
                )
            } else {
                BottomSheetDialogMenuItem(
                    startIcon = Icons.Filled.FavoriteBorder,
                    text = stringResource(id = R.string.episode_controller_mark_favorite),
                    onClick = {
                        onToggleMenu()
                        onFavoriteClicked()
                    },
                )
            }
            if (episodeCompleted) {
                BottomSheetDialogMenuItem(
                    startIcon = Icons.Filled.Check,
                    text = stringResource(id = R.string.episode_controller_mark_not_played),
                    onClick = {
                        onToggleMenu()
                        onNotPlayedClicked()
                    },
                )
            } else {
                BottomSheetDialogMenuItem(
                    startIcon = Icons.Filled.Check,
                    text = stringResource(id = R.string.episode_controller_mark_played),
                    onClick = {
                        onToggleMenu()
                        onPlayedClicked()
                    },
                )
            }
            if (shareInfo != null) {
                BottomSheetDialogMenuItem(
                    startIcon = Icons.Filled.Share,
                    text = stringResource(id = R.string.episode_controller_share),
                    onClick = {
                        onToggleMenu()
                        context.sharePodcast(shareInfo)
                    },
                )
            }
            if (allowSharingToNotificationJournal && shareInfo != null) {
                BottomSheetDialogMenuItem(
                    startIcon = Icons.Filled.Share,
                    text = stringResource(id = R.string.episode_controller_share_notification_journal),
                    onClick = {
                        onToggleMenu()
                        context.sharePodcastWithNotificationJournal(shareInfo)
                    },
                )
            }
        }
    }
}

@Composable
private fun DownloadingIcon(
    downloadStatus: DownloadStatus,
    progress: Double,
    modifier: Modifier = Modifier,
) {
    val strokeColor =
        if (downloadStatus == DownloadStatus.DOWNLOADING || downloadStatus == DownloadStatus.DOWNLOADED) {
            greenColor
        } else {
            MaterialTheme.colorScheme.primary
        }
    val strokeWidth =
        with(LocalDensity.current) {
            2.dp.toPx()
        }
    Box(
        modifier =
            modifier
                .size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (downloadStatus == DownloadStatus.DOWNLOADING || downloadStatus == DownloadStatus.DOWNLOADED) {
            val fillColor = greenColor
            val sweepAngle = remember(Unit) { Animatable(0f) }
            LaunchedEffect(progress) {
                sweepAngle.animateTo(
                    targetValue = (360 * progress).toFloat(),
                    animationSpec =
                        tween(
                            durationMillis = 200,
                            easing = LinearEasing,
                        ),
                )
            }
            Box(
                modifier =
                    modifier
                        .size(20.dp)
                        .drawBehind {
                            drawArc(
                                color = fillColor,
                                startAngle = -90f,
                                sweepAngle = sweepAngle.value,
                                useCenter = true,
                                size = size,
                            )
                        },
            )
        }
        if (downloadStatus == DownloadStatus.NOT_DOWNLOADED ||
            downloadStatus == DownloadStatus.DOWNLOADING ||
            downloadStatus == DownloadStatus.DOWNLOADED
        ) {
            Box(
                modifier =
                    modifier
                        .size(18.dp)
                        .drawBehind {
                            drawCircle(
                                color = strokeColor,
                                style = Stroke(width = strokeWidth),
                            )
                        },
            )
        }
        if (downloadStatus == DownloadStatus.QUEUED || downloadStatus == DownloadStatus.PAUSED) {
            val gapColor = MaterialTheme.colorScheme.background

            fun DrawScope.arc(startAngle: Float) =
                drawArc(
                    color = gapColor,
                    startAngle = startAngle,
                    sweepAngle = 1f,
                    useCenter = true,
                    size = size,
                    // Adding 1f to width because otherwise there's a thin circle that appears throughout
                    style = Stroke(width = strokeWidth + 1f),
                )
            Box(
                modifier =
                    modifier
                        .size(18.dp)
                        .drawBehind {
                            drawCircle(
                                color = strokeColor,
                                style = Stroke(width = strokeWidth),
                            )
                            arc(startAngle = -90f)
                            arc(startAngle = -44.5f)
                            arc(startAngle = -1f)
                            arc(startAngle = 44.5f)
                            arc(startAngle = 89f)
                        },
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_down_inverse),
            contentDescription = null,
            tint =
                if (downloadStatus == DownloadStatus.DOWNLOADED || downloadStatus == DownloadStatus.DOWNLOADING) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.primary
                },
            modifier = Modifier.size(24.dp),
        )
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
    ControlWithTooltip(
        toolTipLabel = stringResource(toolTipLabelRes),
        onClicked = onClicked,
    ) {
        val color =
            if (useTint) {
                greenColor
            } else {
                MaterialTheme.colorScheme.primary
            }
        Icon(imageVector = icon, contentDescription = "", tint = color, modifier = Modifier.size(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlWithTooltip(
    toolTipLabel: String,
    onClicked: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val state = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(toolTipLabel)
            }
        },
        state = state,
    ) {
        IconButton(onClick = onClicked) {
            icon()
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            episode(downloadStatus = DownloadStatus.DOWNLOADED, downloadProgress = 1.0),
            playingState = PlayingState.NOT_PLAYING,
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
            allowSharingToNotificationJournal = false,
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
