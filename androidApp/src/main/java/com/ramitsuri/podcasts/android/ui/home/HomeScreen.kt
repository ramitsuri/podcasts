package com.ramitsuri.podcasts.android.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeListViewState

@Composable
fun HomeScreen(
    episodes: LazyPagingItems<Episode>,
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
            when (val pagingState = episodes.loadState.refresh) {
                is LoadState.Error -> println("Refresh - error: ${pagingState.error}")
                is LoadState.Loading -> println("Refresh - loading")
                is LoadState.NotLoading -> println("Refresh - not loading end reached: ${pagingState.endOfPaginationReached}")
            }
            items(count = episodes.itemCount, key = episodes.itemKey { it.id }) { index ->
                val episode = episodes[index]
                if (episode != null) {
                    EpisodeItem(
                        episode = episode,
                        playingState =
                        if (state.currentlyPlayingEpisodeId == episode.id) {
                            state.currentlyPlayingEpisodeState
                        } else {
                            PlayingState.NOT_PLAYING
                        },
                        onClicked = { onEpisodeClicked(episode.id) },
                        onPlayClicked = { onEpisodePlayClicked(episode) },
                        onPauseClicked = onEpisodePauseClicked,
                        onAddToQueueClicked = { onEpisodeAddToQueueClicked(episode)
                                              episodes.refresh()},
                        onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(episode)
                            episodes.refresh()},
                        onDownloadClicked = { onEpisodeDownloadClicked(episode) },
                        onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(episode) },
                        onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(episode) },
                        onPlayedClicked = { onEpisodePlayedClicked(episode.id) },
                        onNotPlayedClicked = { onEpisodeNotPlayedClicked(episode.id) },
                    )
                }
            }
            when (val pagingState = episodes.loadState.append) {
                is LoadState.Error -> println("Append - error: ${pagingState.error}")
                is LoadState.Loading -> {
                    println("Append - loading")
                    item {
                        Text(text = "Loading more")
                    }
                }
                is LoadState.NotLoading -> println("Append - not loading end reached: ${pagingState.endOfPaginationReached}")
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
            .padding(16.dp)
            .clickable(onClick = onClicked),
    ) {
        Text(style = MaterialTheme.typography.labelSmall, text = episode.podcastName)
        Spacer(modifier = Modifier.height(8.dp))
        Text(style = MaterialTheme.typography.bodyMedium, text = episode.title)
        Spacer(modifier = Modifier.height(8.dp))
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
