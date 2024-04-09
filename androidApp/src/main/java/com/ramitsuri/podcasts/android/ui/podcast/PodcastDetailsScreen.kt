package com.ramitsuri.podcasts.android.ui.podcast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.PodcastWithEpisodes
import com.ramitsuri.podcasts.model.ui.PodcastDetailsViewState

@Composable
fun PodcastDetailsScreen(
    state: PodcastDetailsViewState,
    onBack: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
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
) {
    Column(modifier = modifier) {
        TopAppBar(onBack = onBack)
        val podcastWithEpisodes = state.podcastWithEpisodes
        if (podcastWithEpisodes != null) {
            PodcastDetails(
                podcastWithEpisodes = podcastWithEpisodes,
                currentlyPlayingEpisodeId = state.currentlyPlayingEpisodeId,
                currentlyPlayingEpisodeState = state.playingState,
                onSubscribeClicked = onSubscribeClicked,
                onUnsubscribeClicked = onUnsubscribeClicked,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
                onEpisodeClicked = onEpisodeClicked,
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
            )
        }
    }
}

@Composable
private fun PodcastDetails(
    podcastWithEpisodes: PodcastWithEpisodes,
    currentlyPlayingEpisodeId: String?,
    currentlyPlayingEpisodeState: PlayingState,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
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
) {
    val podcast = podcastWithEpisodes.podcast
    LazyColumn {
        item {
            PodcastHeader(
                podcast = podcast,
                onSubscribeClicked = onSubscribeClicked,
                onUnsubscribeClicked = onUnsubscribeClicked,
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
            )
        }
        items(podcastWithEpisodes.episodes) {
            ColoredHorizontalDivider()
            EpisodeItem(
                episode = it,
                playingState =
                    if (currentlyPlayingEpisodeId == it.id) {
                        currentlyPlayingEpisodeState
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

@Composable
private fun PodcastHeader(
    podcast: Podcast,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        TitleAndImage(podcast = podcast)
        Spacer(modifier = Modifier.height(16.dp))
        PodcastControls(
            subscribed = podcast.subscribed,
            autoDownloadNewEpisodes = podcast.autoDownloadEpisodes,
            autoAddToQueueNewEpisodes = podcast.autoAddToQueue,
            onSubscribeClicked = onSubscribeClicked,
            onUnsubscribeClicked = onUnsubscribeClicked,
            toggleAutoDownloadClicked = toggleAutoDownloadClicked,
            toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = remember(podcast.description) { htmlToString(podcast.description) })
        Spacer(modifier = Modifier.height(16.dp))
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
    onSubscribeClicked: () -> Unit,
    onUnsubscribeClicked: () -> Unit,
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
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
                toggleAutoDownloadClicked = toggleAutoDownloadClicked,
                toggleAutoAddToQueueClicked = toggleAutoAddToQueueClicked,
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
    toggleAutoDownloadClicked: () -> Unit,
    toggleAutoAddToQueueClicked: () -> Unit,
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
private fun PodcastDetailsPreview() {
    PreviewTheme {
        PodcastDetailsScreen(
            state =
                PodcastDetailsViewState(
                    podcastWithEpisodes =
                        PodcastWithEpisodes(
                            podcast = podcast(),
                            episodes = listOf(episode(), episode(), episode(), episode()),
                        ),
                    currentlyPlayingEpisodeId = null,
                    playingState = PlayingState.NOT_PLAYING,
                ),
            onBack = { },
            onSubscribeClicked = { },
            onUnsubscribeClicked = { },
            toggleAutoDownloadClicked = { },
            toggleAutoAddToQueueClicked = { },
            onEpisodeClicked = { },
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
        )
    }
}
