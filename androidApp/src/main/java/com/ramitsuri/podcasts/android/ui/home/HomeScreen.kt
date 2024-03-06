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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.ui.HomeViewState

@Composable
fun HomeScreen(
    state: HomeViewState,
    onImportSubscriptionsClicked: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
    onEpisodePlayClicked: (episodeId: String) -> Unit,
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
                    onClicked = { onEpisodeClicked(it.id) },
                    onPlayClicked = { onEpisodePlayClicked(it.id) },
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
    onClicked: () -> Unit,
    onPlayClicked: () -> Unit,
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
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onPlayClicked) {
                Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "")
            }
            IconButton(onClick = onAddToQueueClicked) {
                Icon(imageVector = Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "")
            }
            IconButton(onClick = onDownloadClicked) {
                Icon(imageVector = Icons.Outlined.DownloadForOffline, contentDescription = "")
            }
            IconButton(onClick = onPlayedClicked) {
                Icon(imageVector = Icons.Outlined.CheckCircleOutline, contentDescription = "")
            }
        }
    }
}
