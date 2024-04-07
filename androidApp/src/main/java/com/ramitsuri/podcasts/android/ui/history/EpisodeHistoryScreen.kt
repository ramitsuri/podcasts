package com.ramitsuri.podcasts.android.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.utils.dateFormatted
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeHistoryViewState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeHistoryScreen(
    state: EpisodeHistoryViewState,
    onBack: () -> Unit,
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
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        TopAppBar(onBack = onBack, label = stringResource(id = R.string.library_history))
        LazyColumn {
            state.episodesByDate.forEach { (date, episodes) ->
                stickyHeader {
                    HeaderItem(text = dateFormatted(date))
                }
                items(episodes) {
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
                        onFavoriteClicked = { onEpisodeFavoriteClicked(it.id) },
                        onNotFavoriteClicked = { onEpisodeNotFavoriteClicked(it.id) },
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (state.episodesByDate.isEmpty()) {
            EpisodeHistoryEmpty()
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
    Row(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .clickable(onClick = onClicked)
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            EpisodeInfo(episode)
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

@Composable
private fun EpisodeHistoryEmpty() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(id = R.string.library_history),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.episode_history_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = R.string.episode_history_empty_info),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HeaderItem(text: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
