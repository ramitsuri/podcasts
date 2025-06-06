package com.ramitsuri.podcasts.android.ui.library.queue

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.BottomSheetDialog
import com.ramitsuri.podcasts.android.ui.components.BottomSheetDialogMenuItem
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.QueueSort
import com.ramitsuri.podcasts.model.ui.QueueViewState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    state: QueueViewState,
    onBack: () -> Unit,
    onEpisodeClicked: (episodeId: String, podcastId: Long) -> Unit,
    onEpisodePlayClicked: (episode: Episode) -> Unit,
    onEpisodePauseClicked: () -> Unit,
    onEpisodeRemoveFromQueueClicked: (episode: Episode) -> Unit,
    onEpisodeDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeRemoveDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeCancelDownloadClicked: (episode: Episode) -> Unit,
    onEpisodePlayedClicked: (episodeId: String) -> Unit,
    onEpisodeNotPlayedClicked: (episodeId: String) -> Unit,
    onEpisodeFavoriteClicked: (episodeId: String) -> Unit,
    onEpisodeNotFavoriteClicked: (episodeId: String) -> Unit,
    onEpisodesRearranged: (from: Int, to: Int) -> Unit,
    onEpisodesSortRequested: (QueueSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val view = LocalView.current
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(
            onBack = onBack,
            label = stringResource(id = R.string.library_queue),
            scrollBehavior = scrollBehavior,
            actions = {
                if (state.episodes.isNotEmpty()) {
                    IconButton(
                        onClick = { showSortMenu = true },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(id = R.string.sort),
                        )
                    }
                }
            },
        )
        val lazyListState = rememberLazyListState()
        val reorderableLazyColumnState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                onEpisodesRearranged(from.index, to.index)
                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            }

        val pullToRefreshState = rememberPullToRefreshState()
        var showQueueSize by remember { mutableStateOf(false) }
        val itemsInQueue =
            pluralStringResource(id = R.plurals.queue_items_in_queue, count = state.episodes.size, state.episodes.size)
        val context = LocalContext.current
        LaunchedEffect(showQueueSize) {
            if (showQueueSize) {
                Toast.makeText(context, itemsInQueue, Toast.LENGTH_SHORT).show()
                showQueueSize = false
            }
        }
        Box(
            modifier =
                Modifier.pullToRefresh(
                    isRefreshing = false,
                    state = pullToRefreshState,
                    threshold = 200.dp,
                    onRefresh = {
                        showQueueSize = true
                    },
                ),
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                items(state.episodes, key = { it.id }) {
                    ColoredHorizontalDivider()
                    EpisodeItem(
                        reorderableLazyColumnState = reorderableLazyColumnState,
                        episode = it,
                        playingState =
                            if (state.currentlyPlayingEpisodeId == it.id) {
                                state.currentlyPlayingEpisodeState
                            } else {
                                PlayingState.NOT_PLAYING
                            },
                        onClicked = { onEpisodeClicked(it.id, it.podcastId) },
                        onPlayClicked = { onEpisodePlayClicked(it) },
                        onPauseClicked = onEpisodePauseClicked,
                        onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(it) },
                        onDownloadClicked = { onEpisodeDownloadClicked(it) },
                        onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(it) },
                        onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(it) },
                        onPlayedClicked = { onEpisodePlayedClicked(it.id) },
                        onNotPlayedClicked = { onEpisodeNotPlayedClicked(it.id) },
                        onFavoriteClicked = { onEpisodeFavoriteClicked(it.id) },
                        onNotFavoriteClicked = { onEpisodeNotFavoriteClicked(it.id) },
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }
        }
        if (state.episodes.isEmpty()) {
            QueueEmpty()
        }
    }
    QueueSortMenu(
        showMenu = showSortMenu,
        onEpisodesSortRequested = {
            showSortMenu = false
            onEpisodesSortRequested(it)
        },
        onDismiss = { showSortMenu = false },
    )
}

@Composable
private fun QueueSortMenu(
    showMenu: Boolean,
    onEpisodesSortRequested: (QueueSort) -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetDialog(
        show = showMenu,
        onDismissRequest = onDismiss,
    ) {
        BottomSheetDialogMenuItem(
            text = stringResource(id = R.string.queue_sort_duration_shortest),
            onClick = { onEpisodesSortRequested(QueueSort.SHORTEST) },
        )
        ColoredHorizontalDivider()
        BottomSheetDialogMenuItem(
            text = stringResource(id = R.string.queue_sort_duration_longest),
            onClick = { onEpisodesSortRequested(QueueSort.LONGEST) },
        )
        ColoredHorizontalDivider()
        BottomSheetDialogMenuItem(
            text = stringResource(id = R.string.queue_sort_oldest),
            onClick = { onEpisodesSortRequested(QueueSort.OLDEST) },
        )
        ColoredHorizontalDivider()
        BottomSheetDialogMenuItem(
            text = stringResource(id = R.string.queue_sort_newest),
            onClick = { onEpisodesSortRequested(QueueSort.NEWEST) },
        )
        ColoredHorizontalDivider()
        BottomSheetDialogMenuItem(
            text = stringResource(id = R.string.queue_sort_podcast),
            onClick = { onEpisodesSortRequested(QueueSort.PODCAST) },
        )
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
    onFavoriteClicked: () -> Unit,
    onNotFavoriteClicked: () -> Unit,
) {
    ReorderableItem(
        state = reorderableLazyColumnState,
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
                Spacer(modifier = Modifier.height(8.dp))
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
                    onFavoriteClicked = onFavoriteClicked,
                    onNotFavoriteClicked = onNotFavoriteClicked,
                )
            }
        }
    }
}

@Composable
private fun EpisodeInfo(episode: Episode) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            url = episode.podcastImageUrl,
            contentDescription = episode.title,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.extraSmall)
                    .size(40.dp),
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
            onEpisodeClicked = { _, _ -> },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
            onEpisodeFavoriteClicked = { },
            onEpisodeNotFavoriteClicked = { },
            onEpisodesSortRequested = { },
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
            onEpisodeClicked = { _, _ -> },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
            onEpisodeFavoriteClicked = { },
            onEpisodeNotFavoriteClicked = { },
            onEpisodesSortRequested = { },
        )
    }
}
