package com.ramitsuri.podcasts.android.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.AppTheme
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.ui.HomeViewState

@Composable
fun HomeScreen(
    state: HomeViewState,
    onImportSubscriptionsClicked: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
    onEpisodePlayClicked: (episodeId: String) -> Unit,
    onEpisodePauseClicked: (episodeId: String) -> Unit,
    onEpisodeAddToQueueClicked: (episodeId: String) -> Unit,
    onEpisodeRemoveFromQueueClicked: (episodeId: String) -> Unit,
    onEpisodeDownloadClicked: (episodeId: String) -> Unit,
    onEpisodeRemoveDownloadClicked: (episodeId: String) -> Unit,
    onEpisodeCancelDownloadClicked: (episodeId: String) -> Unit,
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
            items(state.episodes) {
                EpisodeItem(
                    episode = it,
                    isPlaying = state.currentlyPlayingEpisodeId == it.id,
                    onClicked = { onEpisodeClicked(it.id) },
                    onPlayClicked = { onEpisodePlayClicked(it.id) },
                    onPauseClicked = { onEpisodePauseClicked(it.id) },
                    onAddToQueueClicked = { onEpisodeAddToQueueClicked(it.id) },
                    onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(it.id) },
                    onDownloadClicked = { onEpisodeDownloadClicked(it.id) },
                    onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(it.id) },
                    onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(it.id) },
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
private fun EpisodeItem(
    episode: Episode,
    isPlaying: Boolean,
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
            isPlaying = isPlaying,
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

@Preview
@Composable
private fun EpisodeItemPreview() {
    AppTheme {
        EpisodeItem(
            episode = episode(),
            isPlaying = false,
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
