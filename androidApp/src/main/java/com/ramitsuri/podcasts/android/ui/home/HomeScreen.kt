package com.ramitsuri.podcasts.android.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.ui.EpisodeListViewState

@Composable
fun HomeScreen(
    state: EpisodeListViewState,
    onImportSubscriptionsClicked: () -> Unit,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn {
            if (state.subscribedPodcasts.isNotEmpty()) {
                item {
                    Subscriptions(podcasts = state.subscribedPodcasts)
                }
            }
            items(state.episodes) {
                HorizontalDivider()
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
                )
            }
        }
        if (state.episodes.isEmpty()) {
            Button(onClick = onImportSubscriptionsClicked) {
                Text(text = stringResource(id = R.string.home_import_subscriptions))
            }
        }
    }
}

@Composable
private fun Subscriptions(podcasts: List<Podcast>) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.subscriptions),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(podcasts) {
                SubscribedPodcastItem(it)
            }
        }
    }
}

@Composable
private fun SubscribedPodcastItem(podcast: Podcast) {
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
                .size(96.dp),
    )
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
) {
    Column(
        modifier =
            Modifier
                .clickable(onClick = onClicked)
                .padding(top = 12.dp, bottom = 4.dp)
                .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(style = MaterialTheme.typography.bodySmall, text = episode.podcastName)
                Text(style = MaterialTheme.typography.bodySmall, text = episode.friendlyDatePublished)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = episode.title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )
        Text(style = MaterialTheme.typography.bodySmall, text = episode.description, maxLines = 2)
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
        )
    }
}
