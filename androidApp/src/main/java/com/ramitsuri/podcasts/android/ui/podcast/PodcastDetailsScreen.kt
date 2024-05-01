package com.ramitsuri.podcasts.android.ui.podcast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import be.digitalia.compose.htmlconverter.htmlToString
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.android.ui.components.podcast
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.EpisodeSortOrder
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.ui.PodcastDetailsViewState
import com.ramitsuri.podcasts.model.ui.PodcastWithSelectableEpisodes
import com.ramitsuri.podcasts.model.ui.SelectableEpisode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailsScreen(
    state: PodcastDetailsViewState,
    onBack: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    toggleShowCompletedEpisodesClicked: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
    onEpisodeSelectionChanged: (episodeId: String) -> Unit,
    onEpisodePlayClicked: (episode: Episode) -> Unit,
    onEpisodePauseClicked: () -> Unit,
    onEpisodeAddToQueueClicked: (episode: Episode) -> Unit,
    onEpisodeRemoveFromQueueClicked: (episode: Episode) -> Unit,
    onEpisodeDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeRemoveDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeCancelDownloadClicked: (episode: Episode) -> Unit,
    onEpisodePlayedClicked: (episodeId: String) -> Unit,
    onEpisodeNotPlayedClicked: (episodeId: String) -> Unit,
    onEpisodeFavoriteClicked: (episodeId: String) -> Unit,
    onEpisodeNotFavoriteClicked: (episodeId: String) -> Unit,
    onEpisodeSortOrderClicked: () -> Unit,
    onSelectAllEpisodesClicked: () -> Unit,
    onUnselectAllEpisodesClicked: () -> Unit,
    onMarkSelectedEpisodesAsPlayed: () -> Unit,
    onMarkSelectedEpisodesAsNotPlayed: () -> Unit,
    onNextPageRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(onBack = onBack, scrollBehavior = scrollBehavior)
        val podcastWithEpisodes = state.podcastWithEpisodes
        if (podcastWithEpisodes != null) {
            PodcastDetails(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                podcastWithEpisodes = podcastWithEpisodes,
                currentlyPlayingEpisodeId = state.currentlyPlayingEpisodeId,
                currentlyPlayingEpisodeState = state.playingState,
                episodeSortOrder = state.episodeSortOrder,
                onSubscribeClicked = onSubscribeClicked,
                onUnsubscribeClicked = onUnsubscribeClicked,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
                toggleShowCompletedEpisodesClicked = toggleShowCompletedEpisodesClicked,
                onEpisodeClicked = onEpisodeClicked,
                onEpisodeSelectionChanged = onEpisodeSelectionChanged,
                onEpisodePlayClicked = onEpisodePlayClicked,
                onEpisodePauseClicked = onEpisodePauseClicked,
                onEpisodeAddToQueueClicked = onEpisodeAddToQueueClicked,
                onEpisodeRemoveFromQueueClicked = onEpisodeRemoveFromQueueClicked,
                onEpisodeDownloadClicked = onEpisodeDownloadClicked,
                onEpisodeRemoveDownloadClicked = onEpisodeRemoveDownloadClicked,
                onEpisodeCancelDownloadClicked = onEpisodeCancelDownloadClicked,
                onEpisodePlayedClicked = onEpisodePlayedClicked,
                onEpisodeNotPlayedClicked = onEpisodeNotPlayedClicked,
                onEpisodeFavoriteClicked = onEpisodeFavoriteClicked,
                onEpisodeNotFavoriteClicked = onEpisodeNotFavoriteClicked,
                onEpisodeSortOrderClicked = onEpisodeSortOrderClicked,
                onSelectAllEpisodesClicked = onSelectAllEpisodesClicked,
                onUnselectAllEpisodesClicked = onUnselectAllEpisodesClicked,
                onMarkSelectedEpisodesAsPlayed = onMarkSelectedEpisodesAsPlayed,
                onMarkSelectedEpisodesAsNotPlayed = onMarkSelectedEpisodesAsNotPlayed,
                onNextPageRequested = onNextPageRequested,
            )
        }
    }
}

@Composable
private fun PodcastDetails(
    modifier: Modifier = Modifier,
    podcastWithEpisodes: PodcastWithSelectableEpisodes,
    currentlyPlayingEpisodeId: String?,
    currentlyPlayingEpisodeState: PlayingState,
    episodeSortOrder: EpisodeSortOrder,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    toggleShowCompletedEpisodesClicked: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
    onEpisodeSelectionChanged: (episodeId: String) -> Unit,
    onEpisodePlayClicked: (episode: Episode) -> Unit,
    onEpisodePauseClicked: () -> Unit,
    onEpisodeAddToQueueClicked: (episode: Episode) -> Unit,
    onEpisodeRemoveFromQueueClicked: (episode: Episode) -> Unit,
    onEpisodeDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeRemoveDownloadClicked: (episode: Episode) -> Unit,
    onEpisodeCancelDownloadClicked: (episode: Episode) -> Unit,
    onEpisodePlayedClicked: (episodeId: String) -> Unit,
    onEpisodeNotPlayedClicked: (episodeId: String) -> Unit,
    onEpisodeFavoriteClicked: (episodeId: String) -> Unit,
    onEpisodeNotFavoriteClicked: (episodeId: String) -> Unit,
    onEpisodeSortOrderClicked: () -> Unit,
    onSelectAllEpisodesClicked: () -> Unit,
    onUnselectAllEpisodesClicked: () -> Unit,
    onMarkSelectedEpisodesAsPlayed: () -> Unit,
    onMarkSelectedEpisodesAsNotPlayed: () -> Unit,
    onNextPageRequested: () -> Unit,
) {
    val podcast = podcastWithEpisodes.podcast
    val lazyListState = rememberLazyListState()
    val shouldLoadMoreItems by remember {
        derivedStateOf {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
            // There are 3 other items in the list in addition to the episodes + we want to load more if second last
            // item from the end is visible, which is why subtracting 5 (3 + 2)
            lastVisibleItem != null && lastVisibleItem.index >= totalItemsCount - 5
        }
    }
    LaunchedEffect(shouldLoadMoreItems) {
        if (shouldLoadMoreItems) {
            onNextPageRequested()
        }
    }
    LazyColumn(modifier = modifier, state = lazyListState) {
        item {
            PodcastHeader(
                podcast = podcast,
                onSubscribeClicked = onSubscribeClicked,
                onUnsubscribeClicked = onUnsubscribeClicked,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
                toggleShowCompletedEpisodesClicked = toggleShowCompletedEpisodesClicked,
            )
        }
        if (podcastWithEpisodes.episodes.isNotEmpty()) {
            item {
                EpisodeCountAndMenu(
                    count = podcastWithEpisodes.episodes.size,
                    selectedCount = podcastWithEpisodes.selectedCount,
                    sortOrder = episodeSortOrder,
                    onSortOrderClicked = onEpisodeSortOrderClicked,
                    onSelectAllClicked = onSelectAllEpisodesClicked,
                    onUnselectAllClicked = onUnselectAllEpisodesClicked,
                    onMarkSelectedAsPlayed = onMarkSelectedEpisodesAsPlayed,
                    onMarkSelectedAsNotPlayed = onMarkSelectedEpisodesAsNotPlayed,
                )
            }
        }
        items(podcastWithEpisodes.episodes) {
            val episode = it.episode
            ColoredHorizontalDivider()
            EpisodeItem(
                episode = episode,
                playingState =
                if (currentlyPlayingEpisodeId == episode.id) {
                    currentlyPlayingEpisodeState
                } else {
                    PlayingState.NOT_PLAYING
                },
                inSelectionState = podcastWithEpisodes.inSelectionState,
                selected = it.selected,
                onClicked = { onEpisodeClicked(episode.id) },
                onSelectionChanged = { onEpisodeSelectionChanged(episode.id) },
                onPlayClicked = { onEpisodePlayClicked(episode) },
                onPauseClicked = onEpisodePauseClicked,
                onAddToQueueClicked = { onEpisodeAddToQueueClicked(episode) },
                onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(episode) },
                onDownloadClicked = { onEpisodeDownloadClicked(episode) },
                onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(episode) },
                onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(episode) },
                onPlayedClicked = { onEpisodePlayedClicked(episode.id) },
                onNotPlayedClicked = { onEpisodeNotPlayedClicked(episode.id) },
                onFavoriteClicked = { onEpisodeFavoriteClicked(episode.id) },
                onNotFavoriteClicked = { onEpisodeNotFavoriteClicked(episode.id) },
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EpisodeCountAndMenu(
    count: Int,
    selectedCount: Int,
    sortOrder: EpisodeSortOrder,
    onSortOrderClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
    onUnselectAllClicked: () -> Unit,
    onMarkSelectedAsPlayed: () -> Unit,
    onMarkSelectedAsNotPlayed: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val countText =
        pluralStringResource(
            id = R.plurals.podcast_details_episode_count,
            count = count,
            count,
        )
    val selectedCountText =
        if (selectedCount == 0) {
            ""
        } else {
            stringResource(
                id = R.string.podcast_details_selected_episode_count,
                selectedCount,
            )
        }
    val text =
        buildAnnotatedString {
            append(countText)
            withStyle(SpanStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)) {
                append(selectedCountText)
            }
        }
    Row(
        modifier =
        Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.weight(1f))
        EpisodesMenu(
            showMenu = showMenu,
            onToggleMenu = { showMenu = !showMenu },
            showSortOrder = selectedCount == 0,
            sortOrder = sortOrder,
            showSelectAll = selectedCount != count,
            showUnselectAll = selectedCount != 0,
            showMarkAsPlayed = selectedCount != 0,
            showMarkAsNotPlayed = selectedCount != 0,
            onSortOrderClicked = onSortOrderClicked,
            onSelectAllClicked = onSelectAllClicked,
            onUnselectAllClicked = onUnselectAllClicked,
            onMarkSelectedAsPlayed = onMarkSelectedAsPlayed,
            onMarkSelectedAsNotPlayed = onMarkSelectedAsNotPlayed,
        )
    }
}

@Composable
private fun EpisodesMenu(
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    showSortOrder: Boolean,
    sortOrder: EpisodeSortOrder,
    showSelectAll: Boolean,
    showUnselectAll: Boolean,
    showMarkAsPlayed: Boolean,
    showMarkAsNotPlayed: Boolean,
    onSortOrderClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
    onUnselectAllClicked: () -> Unit,
    onMarkSelectedAsPlayed: () -> Unit,
    onMarkSelectedAsNotPlayed: () -> Unit,
) {
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
            // Sort order
            if (showSortOrder) {
                val sortTextResId =
                    when (sortOrder) {
                        EpisodeSortOrder.DATE_PUBLISHED_DESC -> R.string.podcast_details_sort_by_publish_asc
                        EpisodeSortOrder.DATE_PUBLISHED_ASC -> R.string.podcast_details_sort_by_publish_desc
                    }
                DropdownMenuItem(
                    text = { Text(stringResource(id = sortTextResId)) },
                    onClick = {
                        onSortOrderClicked()
                        onToggleMenu()
                    },
                )
            }

            // Mark Played
            if (showMarkAsPlayed) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_mark_selected_as_played)) },
                    onClick = {
                        onMarkSelectedAsPlayed()
                        onToggleMenu()
                    },
                )
            }

            // Mark Not Played
            if (showMarkAsNotPlayed) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_mark_selected_as_not_played)) },
                    onClick = {
                        onMarkSelectedAsNotPlayed()
                        onToggleMenu()
                    },
                )
            }

            // Select All
            if (showSelectAll) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_select_all_episodes)) },
                    onClick = {
                        onSelectAllClicked()
                        onToggleMenu()
                    },
                )
            }

            // Unselect All
            if (showUnselectAll) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_unselect_all_episodes)) },
                    onClick = {
                        onUnselectAllClicked()
                        onToggleMenu()
                    },
                )
            }
        }
    }
}

@Composable
private fun PodcastHeader(
    podcast: Podcast,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    toggleShowCompletedEpisodesClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
) {
    val html = remember(podcast.description) { htmlToAnnotatedString(podcast.description) }
    val collapsedMaxLine = 3
    var isExpanded by remember { mutableStateOf(false) }
    var clickable by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        TitleAndImage(podcast = podcast)
        Spacer(modifier = Modifier.height(16.dp))
        PodcastControls(
            subscribed = podcast.subscribed,
            autoDownloadNewEpisodes = podcast.autoDownloadEpisodes,
            autoAddToQueueNewEpisodes = podcast.autoAddToQueue,
            showCompletedEpisodes = podcast.showCompletedEpisodes,
            onSubscribeClicked = onSubscribeClicked,
            onUnsubscribeClicked = onUnsubscribeClicked,
            toggleAutoDownloadClicked = toggleAutoDownloadClicked,
            toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
            toggleShowCompletedEpisodesClicked = toggleShowCompletedEpisodesClicked,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier =
            Modifier
                .clickable(clickable) {
                    isExpanded = !isExpanded
                },
        ) {
            Text(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                text = html,
                overflow = TextOverflow.Visible,
                maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
                onTextLayout = { textLayoutResult ->
                    if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                        clickable = true
                    }
                },
            )
        }
    }
}

@Composable
private fun TitleAndImage(podcast: Podcast) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model =
            ImageRequest.Builder(LocalContext.current)
                .data(podcast.artwork)
                .crossfade(true)
                .build(),
            contentDescription = podcast.title,
            contentScale = ContentScale.FillBounds,
            modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .size(64.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                style = MaterialTheme.typography.bodyLarge,
                text = podcast.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = podcast.author,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PodcastControls(
    subscribed: Boolean,
    autoDownloadNewEpisodes: Boolean,
    autoAddToQueueNewEpisodes: Boolean,
    showCompletedEpisodes: Boolean,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    toggleShowCompletedEpisodesClicked: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(onClick = if (subscribed) onUnsubscribeClicked else onSubscribeClicked) {
            Text(text = stringResource(id = if (subscribed) R.string.unsubscribe else R.string.subscribe))
        }
        AnimatedVisibility(visible = subscribed) {
            PodcastMenu(
                showMenu = showMenu,
                onToggleMenu = { showMenu = !showMenu },
                autoDownloadNewEpisodes = autoDownloadNewEpisodes,
                autoAddToQueueNewEpisodes = autoAddToQueueNewEpisodes,
                showCompletedEpisodes = showCompletedEpisodes,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
                toggleShowCompletedEpisodesClicked = toggleShowCompletedEpisodesClicked,
            )
        }
    }
}

@Composable
private fun PodcastMenu(
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    autoDownloadNewEpisodes: Boolean,
    autoAddToQueueNewEpisodes: Boolean,
    showCompletedEpisodes: Boolean,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    toggleShowCompletedEpisodesClicked: () -> Unit,
) {
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
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = autoDownloadNewEpisodes, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.auto_download_new_episodes))
                    }
                },
                onClick = {
                    toggleAutoDownloadClicked()
                },
            )
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = autoAddToQueueNewEpisodes, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.auto_add_to_queue_new_episodes))
                    }
                },
                onClick = {
                    toggleAutoAddToQueueClicked()
                },
            )
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = showCompletedEpisodes, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.podcast_details_show_completed_episodes))
                    }
                },
                onClick = {
                    toggleShowCompletedEpisodesClicked()
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EpisodeItem(
    episode: Episode,
    playingState: PlayingState,
    inSelectionState: Boolean,
    selected: Boolean,
    onClicked: () -> Unit,
    onSelectionChanged: () -> Unit,
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
    Column(
        modifier =
        Modifier
            .combinedClickable(
                onClick =
                if (inSelectionState) {
                    onSelectionChanged
                } else {
                    onClicked
                },
                onLongClick = onSelectionChanged,
            )
            .background(
                color =
                if (inSelectionState && selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.background
                },
            )
            .padding(top = 12.dp, bottom = 4.dp)
            .padding(horizontal = 16.dp),
    ) {
        val datePublished = episode.datePublishedInstant
        if (datePublished != null) {
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = friendlyPublishDate(publishedDateTime = datePublished),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = episode.title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            text = remember(episode.description) { htmlToString(episode.description) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Crossfade(
            targetState = inSelectionState,
            label = "EpisodeControlSelectionStateTransition",
        ) { isSelectionState ->
            if (isSelectionState) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    val icon =
                        if (selected) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.Circle
                        }
                    IconButton(onClick = onSelectionChanged) {
                        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                EpisodeControls(
                    episode = episode,
                    playingState = playingState,
                    onPlayClicked = onPlayClicked,
                    onPauseClicked = onPauseClicked,
                    onAddToQueueClicked = onAddToQueueClicked,
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

@ThemePreview
@Composable
private fun PodcastDetailsPreview() {
    PreviewTheme {
        PodcastDetailsScreen(
            state =
            PodcastDetailsViewState(
                podcastWithEpisodes =
                PodcastWithSelectableEpisodes(
                    podcast = podcast(),
                    episodes =
                    listOf(
                        SelectableEpisode(false, episode()),
                        SelectableEpisode(false, episode()),
                        SelectableEpisode(false, episode()),
                        SelectableEpisode(false, episode()),
                    ),
                ),
                currentlyPlayingEpisodeId = null,
                playingState = PlayingState.NOT_PLAYING,
            ),
            onBack = { },
            onSubscribeClicked = { },
            onUnsubscribeClicked = { },
            toggleAutoDownloadClicked = { },
            toggleAutoAddToQueueClicked = { },
            toggleShowCompletedEpisodesClicked = { },
            onEpisodeClicked = { },
            onEpisodeSelectionChanged = { },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeAddToQueueClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
            onEpisodeFavoriteClicked = { },
            onEpisodeNotFavoriteClicked = { },
            onEpisodeSortOrderClicked = { },
            onSelectAllEpisodesClicked = { },
            onUnselectAllEpisodesClicked = { },
            onMarkSelectedEpisodesAsPlayed = { },
            onMarkSelectedEpisodesAsNotPlayed = { },
            onNextPageRequested = { },
        )
    }
}

@ThemePreview
@Composable
private fun PodcastDetails_WithSelectionPreview() {
    PreviewTheme {
        PodcastDetailsScreen(
            state =
            PodcastDetailsViewState(
                podcastWithEpisodes =
                PodcastWithSelectableEpisodes(
                    podcast = podcast(),
                    episodes =
                    listOf(
                        SelectableEpisode(true, episode()),
                        SelectableEpisode(false, episode()),
                        SelectableEpisode(true, episode()),
                        SelectableEpisode(false, episode()),
                    ),
                ),
                currentlyPlayingEpisodeId = null,
                playingState = PlayingState.NOT_PLAYING,
            ),
            onBack = { },
            onSubscribeClicked = { },
            onUnsubscribeClicked = { },
            toggleAutoDownloadClicked = { },
            toggleAutoAddToQueueClicked = { },
            toggleShowCompletedEpisodesClicked = { },
            onEpisodeClicked = { },
            onEpisodeSelectionChanged = { },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeAddToQueueClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
            onEpisodeFavoriteClicked = { },
            onEpisodeNotFavoriteClicked = { },
            onEpisodeSortOrderClicked = { },
            onSelectAllEpisodesClicked = { },
            onUnselectAllEpisodesClicked = { },
            onMarkSelectedEpisodesAsPlayed = { },
            onMarkSelectedEpisodesAsNotPlayed = { },
            onNextPageRequested = { },
        )
    }
}
