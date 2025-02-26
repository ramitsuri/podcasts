package com.ramitsuri.podcasts.android.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.digitalia.compose.htmlconverter.htmlToString
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.CenteredTitleTopAppBar
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.android.ui.components.podcast
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.ui.HomeViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeViewState,
    onSettingsClicked: () -> Unit,
    onImportSubscriptionsClicked: () -> Unit,
    onPodcastClicked: (podcastId: Long) -> Unit,
    onPodcastHasNewSeen: (podcastId: Long) -> Boolean,
    onMorePodcastsClicked: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
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
    modifier: Modifier = Modifier,
    onNextPageRequested: () -> Unit,
    onYearEndReviewClicked: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val lazyListState = rememberLazyListState()
        val shouldLoadMoreItems by remember {
            derivedStateOf {
                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
                // There is 1 other item above the list of episodes + we want to load more if 10th
                // item from the end is visible, which is why subtracting 11 (1 + 10)
                lastVisibleItem != null && lastVisibleItem.index >= totalItemsCount - 11
            }
        }
        LaunchedEffect(shouldLoadMoreItems) {
            if (shouldLoadMoreItems) {
                onNextPageRequested()
            }
        }
        val hapticFeedback = LocalHapticFeedback.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        if (state.subscribedPodcasts.isNotEmpty()) {
            CenteredTitleTopAppBar(
                scrollBehavior = scrollBehavior,
                onSettingsClicked = onSettingsClicked,
            )
        }
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            LazyColumn(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), state = lazyListState) {
                if (state.subscribedPodcasts.isNotEmpty()) {
                    item {
                        Subscriptions(
                            podcasts = state.subscribedPodcasts,
                            showYearEndReview = state.showYearEndReview,
                            onPodcastClicked = { onPodcastClicked(it.id) },
                            onPodcastLongClicked = {
                                val hadNewEpisodes = onPodcastHasNewSeen(it.id)
                                if (hadNewEpisodes) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onMoreClicked = onMorePodcastsClicked,
                            onReviewItemClicked = onYearEndReviewClicked,
                        )
                    }
                }
                items(key = { it.id }, items = state.episodes) {
                    ColoredHorizontalDivider()
                    EpisodeItem(
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
                        onAddToQueueClicked = { onEpisodeAddToQueueClicked(it) },
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        if (state.episodes.isEmpty()) {
            FilledTonalButton(onClick = onImportSubscriptionsClicked) {
                Text(text = stringResource(id = R.string.home_import_subscriptions))
            }
        }
    }
}

@Composable
private fun Subscriptions(
    podcasts: List<Podcast>,
    showYearEndReview: Boolean,
    onPodcastClicked: (Podcast) -> Unit,
    onPodcastLongClicked: (Podcast) -> Unit,
    onMoreClicked: () -> Unit,
    onReviewItemClicked: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.subscriptions),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onMoreClicked) {
                Text(text = stringResource(id = R.string.more))
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (showYearEndReview) {
                item {
                    YearEndReviewItem(onClicked = onReviewItemClicked)
                }
            }
            items(items = podcasts, key = { it.id }) {
                SubscribedPodcastItem(
                    title = it.title,
                    artwork = it.artwork,
                    hasNewEpisodes = it.hasNewEpisodes,
                    onClicked = { onPodcastClicked(it) },
                    onLongClicked = { onPodcastLongClicked(it) },
                )
            }
            item {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubscribedPodcastItem(
    title: String,
    artwork: String,
    hasNewEpisodes: Boolean,
    onClicked: () -> Unit,
    onLongClicked: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .combinedClickable(
                    onClick = onClicked,
                    onLongClick = onLongClicked,
                ),
    ) {
        Image(
            url = artwork,
            contentDescription = title,
            modifier =
                Modifier
                    .size(88.dp)
                    .padding(4.dp)
                    .clip(MaterialTheme.shapes.small),
        )
        if (hasNewEpisodes) {
            Badge(
                modifier =
                    Modifier
                        .size(16.dp)
                        .border(4.dp, color = MaterialTheme.colorScheme.background, shape = CircleShape)
                        .align(Alignment.TopEnd)
                        .clip(MaterialTheme.shapes.small),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun YearEndReviewItem(onClicked: () -> Unit) {
    val brush =
        Brush.sweepGradient(
            listOf(
                Color(0xFF0000FF),
                Color(0xFFCC0000),
                Color(0xFF00AA00),
            ),
        )
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "",
    )

    Box(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .clipToBounds()
                .fillMaxWidth()
                .size(80.dp)
                .drawWithContent {
                    rotate(angle) {
                        drawCircle(
                            brush = brush,
                            radius = size.width,
                            blendMode = BlendMode.SrcIn,
                        )
                    }
                    drawContent()
                }
                .combinedClickable(
                    onClick = onClicked,
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .size(76.dp)
                    .align(Alignment.Center)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                text = stringResource(R.string.year_end_review_2024),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EpisodeItem(
    episode: Episode,
    playingState: PlayingState,
    onClicked: () -> Unit,
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
                .clickable(onClick = onClicked)
                .padding(top = 12.dp, bottom = 4.dp)
                .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    text = episode.podcastName,
                )
                val datePublished = episode.datePublishedInstant
                if (datePublished != null) {
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = friendlyPublishDate(publishedDateTime = datePublished),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = episode.title,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
            maxLines = 2,
            text = remember(episode.description) { htmlToString(episode.description) },
            modifier = Modifier.fillMaxWidth(),
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
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

@ThemePreview
@Composable
private fun EpisodeItemPreview() {
    PreviewTheme {
        EpisodeItem(
            episode = episode(),
            playingState = PlayingState.NOT_PLAYING,
            onClicked = { },
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
private fun SubscribedPodcastItemPreview_HasNewEpisodes() {
    PreviewTheme {
        val podcast = podcast(hasNewEpisodes = true)
        SubscribedPodcastItem(
            title = podcast.title,
            hasNewEpisodes = podcast.hasNewEpisodes,
            artwork = "",
            onClicked = { },
            onLongClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SubscribedPodcastItemPreview() {
    PreviewTheme {
        val podcast = podcast(hasNewEpisodes = false)
        SubscribedPodcastItem(
            title = podcast.title,
            hasNewEpisodes = podcast.hasNewEpisodes,
            artwork = "",
            onClicked = { },
            onLongClicked = { },
        )
    }
}
