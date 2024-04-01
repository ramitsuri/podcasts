package com.ramitsuri.podcasts.android.ui.library.queue

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.QueueViewState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@Composable
fun QueueScreen(
    state: QueueViewState,
    onBack: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
    onEpisodePlayClicked: (episode: Episode) -> Unit,
    onEpisodePauseClicked: () -> Unit,
    onEpisodeRemoveFromQueueClicked: (episode: Episode) -> Unit,
    onEpisodeDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeRemoveDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeCancelDownloadClicked: (episode: Episode) -> Unit,
    onEpisodePlayedClicked: (episodeId: String) -> Unit,
    onEpisodeNotPlayedClicked: (episodeId: String) -> Unit,
    onEpisodesRearranged: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        TopAppBar(onBack = onBack, label = stringResource(id = R.string.library_queue))
        val lazyListState = rememberLazyListState()
        val reorderableLazyColumnState =
            rememberReorderableLazyColumnState(lazyListState) { from, to ->
                onEpisodesRearranged(from.index, to.index)
            }
        LazyColumn(state = lazyListState) {
            items(state.episodes, key = { it.id }) {
                HorizontalDivider()
                EpisodeItem(
                    reorderableLazyColumnState = reorderableLazyColumnState,
                    episode = it,
                    playingState =
                        if (state.currentlyPlayingEpisodeId == it.id) {
                            state.currentlyPlayingEpisodeState
                        } else {
                            PlayingState.NOT_PLAYING
                        },
                    onClicked = { onEpisodeClicked(it.id) },
                    onPlayClicked = { onEpisodePlayClicked(it) },
                    onPauseClicked = onEpisodePauseClicked,
                    onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(it) },
                    onDownloadClicked = { onEpisodeDownloadClicked(it) },
                    onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(it) },
                    onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(it) },
                    onPlayedClicked = { onEpisodePlayedClicked(it.id) },
                    onNotPlayedClicked = { onEpisodeNotPlayedClicked(it.id) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (state.episodes.isEmpty()) {
            QueueEmpty()
        }
    }
}

@Composable
private fun QueueEmpty() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.PlaylistAdd,
            contentDescription = stringResource(id = R.string.library_queue),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.queue_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = R.string.queue_empty_info),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.EpisodeItem(
    reorderableLazyColumnState: ReorderableLazyListState,
    episode: Episode,
    playingState: PlayingState,
    onClicked: () -> Unit,
    onPlayClicked: () -> Unit,
    onPauseClicked: () -> Unit,
    onRemoveFromQueueClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    onRemoveDownloadClicked: () -> Unit,
    onCancelDownloadClicked: () -> Unit,
    onPlayedClicked: () -> Unit,
    onNotPlayedClicked: () -> Unit,
) {
    ReorderableItem(
        reorderableLazyListState = reorderableLazyColumnState,
        key = episode.id,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .clickable(onClick = onClicked)
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                modifier = Modifier.draggableHandle(interactionSource = interactionSource),
                onClick = {},
            ) {
                Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
            }
            Column {
                EpisodeInfo(episode)
                EpisodeControls(
                    episode = episode,
                    playingState = playingState,
                    onPlayClicked = onPlayClicked,
                    onPauseClicked = onPauseClicked,
                    onAddToQueueClicked = { },
                    onRemoveFromQueueClicked = onRemoveFromQueueClicked,
                    onDownloadClicked = onDownloadClicked,
                    onRemoveDownloadClicked = onRemoveDownloadClicked,
                    onCancelDownloadClicked = onCancelDownloadClicked,
                    onPlayedClicked = onPlayedClicked,
                    onNotPlayedClicked = onNotPlayedClicked,
                )
            }
        }
    }
}

@Composable
private fun EpisodeInfo(episode: Episode) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(episode.podcastImageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = episode.title,
            contentScale = ContentScale.FillBounds,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .size(56.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            val datePublished = episode.datePublishedInstant
            if (datePublished != null) {
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = friendlyPublishDate(publishedDateTime = datePublished),
                )
            }
            Text(style = MaterialTheme.typography.bodySmall, text = episode.title, maxLines = 1)
        }
    }
}

@ThemePreview
@Composable
private fun QueuePreview_Empty() {
    PreviewTheme {
        QueueScreen(
            state = QueueViewState(),
            onBack = { },
            onEpisodesRearranged = { _, _ -> },
            onEpisodeClicked = { },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun QueuePreview_NotEmpty() {
    PreviewTheme {
        QueueScreen(
            state =
                QueueViewState(
                    episodes = listOf(episode(), episode()),
                ),
            onBack = { },
            onEpisodesRearranged = { _, _ -> },
            onEpisodeClicked = { },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
        )
    }
}
