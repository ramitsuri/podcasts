package com.ramitsuri.podcasts.android.ui.podcast

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import androidx.core.view.HapticFeedbackConstantsCompat
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import be.digitalia.compose.htmlconverter.htmlToString
import com.ramitsuri.podcasts.android.BuildConfig
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.Image
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
import com.ramitsuri.podcasts.utils.LogHelper

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
    onLoadOlderEpisodesRequested: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (BuildConfig.DEBUG) {
        LaunchedEffect(key1 = state) {
            LogHelper.d(
                "PodcastDetails",
                "Total: ${state.podcastWithEpisodes?.episodes?.size}, " +
                    "selected: ${state.podcastWithEpisodes?.episodes?.count { it.selected }}",
            )
        }
    }
    Column(modifier = modifier) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(onBack = onBack, scrollBehavior = scrollBehavior)
        val podcastWithEpisodes = state.podcastWithEpisodes
        if (podcastWithEpisodes != null) {
            PodcastDetails(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                hasMorePages = state.hasMorePages,
                alreadyLoadedEpisodeCount = state.availableEpisodeCount,
                loadingOlderEpisodes = state.loadingOlderEpisodes,
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
                onLoadOlderEpisodesRequested = onLoadOlderEpisodesRequested,
            )
        }
    }
}

@Composable
private fun PodcastDetails(
    modifier: Modifier = Modifier,
    hasMorePages: Boolean,
    loadingOlderEpisodes: Boolean,
    alreadyLoadedEpisodeCount: Long,
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
    onLoadOlderEpisodesRequested: (Long) -> Unit,
) {
    val podcast = podcastWithEpisodes.podcast
    val lazyListState = rememberLazyListState()
    val shouldLoadMoreItems by remember {
        derivedStateOf {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
            // There is 1 other items in the list above episodes (podcast header) we want to load more if 10th
            // item from the end is visible, which is why subtracting 11 (1 + 10)
            lastVisibleItem != null && lastVisibleItem.index >= totalItemsCount - 11
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
                count = podcastWithEpisodes.episodes.size,
                selectedCount = podcastWithEpisodes.selectedCount,
                sortOrder = episodeSortOrder,
                showCompletedEpisodes = podcast.showCompletedEpisodes,
                onSortOrderClicked = onEpisodeSortOrderClicked,
                onSelectAllClicked = onSelectAllEpisodesClicked,
                onUnselectAllClicked = onUnselectAllEpisodesClicked,
                onMarkSelectedAsPlayed = onMarkSelectedEpisodesAsPlayed,
                onMarkSelectedAsNotPlayed = onMarkSelectedEpisodesAsNotPlayed,
                toggleShowCompletedEpisodesClicked = toggleShowCompletedEpisodesClicked,
                onSubscribeClicked = onSubscribeClicked,
                onUnsubscribeClicked = onUnsubscribeClicked,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
            )
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
        if (!hasMorePages) {
            item {
                LoadEvenOlderEpisodesButton(
                    loadingOlderEpisodes = loadingOlderEpisodes,
                    alreadyLoadedCount = alreadyLoadedEpisodeCount,
                    onLoadOlderEpisodesRequested = onLoadOlderEpisodesRequested,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LoadEvenOlderEpisodesButton(
    loadingOlderEpisodes: Boolean,
    alreadyLoadedCount: Long,
    onLoadOlderEpisodesRequested: (Long) -> Unit,
) {
    var showLoadEvenOlderEpisodesDialog by remember { mutableStateOf(false) }
    var enteredLoadMoreCount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { showLoadEvenOlderEpisodesDialog = true }) {
            if (loadingOlderEpisodes) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(text = stringResource(id = R.string.podcast_details_load_even_older_episodes))
            }
        }
    }
    if (showLoadEvenOlderEpisodesDialog) {
        Dialog(
            onDismissRequest = { showLoadEvenOlderEpisodesDialog = false },
        ) {
            Card {
                Column(
                    modifier =
                        Modifier
                            .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.podcast_details_load_even_older_episodes_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text =
                            stringResource(
                                id = R.string.podcast_details_load_even_older_episodes_message,
                            ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text =
                            stringResource(
                                id = R.string.podcast_details_load_even_older_episodes_message_already_loaded_count,
                                alreadyLoadedCount,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = enteredLoadMoreCount,
                        onValueChange = {
                            if (it.isDigitsOnly()) {
                                enteredLoadMoreCount = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = {
                            Text(stringResource(id = R.string.podcast_details_load_even_older_episodes_text_field_hint))
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = {
                                showLoadEvenOlderEpisodesDialog = false
                                enteredLoadMoreCount = ""
                            },
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(
                            onClick = {
                                showLoadEvenOlderEpisodesDialog = false
                                onLoadOlderEpisodesRequested(enteredLoadMoreCount.toLongOrNull() ?: 0)
                                enteredLoadMoreCount = ""
                            },
                        ) {
                            Text(text = stringResource(id = R.string.podcast_details_load_even_older_episodes_load))
                        }
                    }
                }
            }
        }
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
    showCompletedEpisodes: Boolean,
    showCompletedEpisodesMenuItem: Boolean,
    onSortOrderClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
    onUnselectAllClicked: () -> Unit,
    onMarkSelectedAsPlayed: () -> Unit,
    onMarkSelectedAsNotPlayed: () -> Unit,
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
                tint = MaterialTheme.colorScheme.primary,
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
                        onToggleMenu()
                        onSortOrderClicked()
                    },
                )
            }

            // Show Completed Episodes
            if (showCompletedEpisodesMenuItem) {
                val showCompletedTextResId =
                    if (showCompletedEpisodes) {
                        R.string.podcast_details_hide_completed_episodes
                    } else {
                        R.string.podcast_details_show_completed_episodes
                    }
                DropdownMenuItem(
                    text = { Text(stringResource(id = showCompletedTextResId)) },
                    onClick = {
                        onToggleMenu()
                        toggleShowCompletedEpisodesClicked()
                    },
                )
            }

            // Mark Played
            if (showMarkAsPlayed) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_mark_selected_as_played)) },
                    onClick = {
                        onToggleMenu()
                        onMarkSelectedAsPlayed()
                    },
                )
            }

            // Mark Not Played
            if (showMarkAsNotPlayed) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_mark_selected_as_not_played)) },
                    onClick = {
                        onToggleMenu()
                        onMarkSelectedAsNotPlayed()
                    },
                )
            }

            // Select All
            if (showSelectAll) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_select_all_episodes)) },
                    onClick = {
                        onToggleMenu()
                        onSelectAllClicked()
                    },
                )
            }

            // Unselect All
            if (showUnselectAll) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.podcast_details_unselect_all_episodes)) },
                    onClick = {
                        onToggleMenu()
                        onUnselectAllClicked()
                    },
                )
            }
        }
    }
}

@Composable
private fun PodcastHeader(
    podcast: Podcast,
    count: Int,
    selectedCount: Int,
    sortOrder: EpisodeSortOrder,
    showCompletedEpisodes: Boolean,
    onSortOrderClicked: () -> Unit,
    onSelectAllClicked: () -> Unit,
    onUnselectAllClicked: () -> Unit,
    onMarkSelectedAsPlayed: () -> Unit,
    onMarkSelectedAsNotPlayed: () -> Unit,
    toggleShowCompletedEpisodesClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
) {
    val html = remember(podcast.description) { htmlToAnnotatedString(podcast.description) }
    val collapsedMaxLine = 3
    var isExpanded by remember { mutableStateOf(false) }
    var clickable by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        TitleAndImage(podcast = podcast)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SubscribeButton(
                subscribed = podcast.subscribed,
                autoDownloadNewEpisodes = podcast.autoDownloadEpisodes,
                autoAddToQueueNewEpisodes = podcast.autoAddToQueue,
                onSubscribeClicked = onSubscribeClicked,
                onUnsubscribeClicked = onUnsubscribeClicked,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
            )
            if (count > 0) {
                EpisodesMenu(
                    showMenu = showMenu,
                    onToggleMenu = { showMenu = !showMenu },
                    showSortOrder = selectedCount == 0,
                    sortOrder = sortOrder,
                    showSelectAll = selectedCount > 0 && selectedCount != count,
                    showUnselectAll = selectedCount != 0,
                    showMarkAsPlayed = selectedCount != 0,
                    showMarkAsNotPlayed = selectedCount != 0,
                    showCompletedEpisodes = showCompletedEpisodes,
                    showCompletedEpisodesMenuItem = selectedCount == 0,
                    onSortOrderClicked = onSortOrderClicked,
                    onSelectAllClicked = onSelectAllClicked,
                    onUnselectAllClicked = onUnselectAllClicked,
                    onMarkSelectedAsPlayed = onMarkSelectedAsPlayed,
                    onMarkSelectedAsNotPlayed = onMarkSelectedAsNotPlayed,
                    toggleShowCompletedEpisodesClicked = toggleShowCompletedEpisodesClicked,
                )
            }
        }
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
                overflow = TextOverflow.Ellipsis,
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
        Image(
            url =podcast.artwork,
            contentDescription = podcast.title,
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
    val view = LocalView.current
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
                    onLongClick = {
                        onSelectionChanged()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                    },
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

@Composable
private fun SubscribeButton(
    subscribed: Boolean,
    autoDownloadNewEpisodes: Boolean,
    autoAddToQueueNewEpisodes: Boolean,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable(
                        onClick = {
                            if (subscribed) {
                                showMenu = true
                            } else {
                                onSubscribeClicked()
                                showMenu = true
                            }
                        },
                    )
                    .background(
                        color =
                            if (subscribed) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.background
                            },
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (subscribed) Icons.Filled.CheckCircle else Icons.Filled.Add,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = if (subscribed) R.string.subscribed else R.string.subscribe),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = !showMenu },
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
                onClick = toggleAutoDownloadClicked,
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
                onClick = toggleAutoAddToQueueClicked,
            )
            if (subscribed) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(stringResource(id = R.string.unsubscribe))
                    },
                    onClick = {
                        showMenu = false
                        onUnsubscribeClicked()
                    },
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
            onLoadOlderEpisodesRequested = { },
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
            onLoadOlderEpisodesRequested = { },
        )
    }
}

@ThemePreview
@Composable
private fun SubscribeButtonPreview_WhenSubscribed() {
    PreviewTheme {
        SubscribeButton(
            subscribed = true,
            autoDownloadNewEpisodes = false,
            autoAddToQueueNewEpisodes = false,
            onSubscribeClicked = { },
            onUnsubscribeClicked = { },
            toggleAutoDownloadClicked = { },
            toggleAutoAddToQueueClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SubscribeButtonPreview_WhenNotSubscribed() {
    PreviewTheme {
        SubscribeButton(
            subscribed = false,
            autoDownloadNewEpisodes = false,
            autoAddToQueueNewEpisodes = false,
            onSubscribeClicked = { },
            onUnsubscribeClicked = { },
            toggleAutoDownloadClicked = { },
            toggleAutoAddToQueueClicked = { },
        )
    }
}
