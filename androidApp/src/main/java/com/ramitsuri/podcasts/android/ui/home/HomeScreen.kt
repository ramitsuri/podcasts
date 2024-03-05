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
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.ui.HomeViewState

@Composable
fun HomeScreen(
    state: HomeViewState,
    onImportSubscriptionsClicked: () -> Unit,
    onEpisodeClicked: (episodeId: String) -> Unit,
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
                EpisodeItem(episode = it, onEpisodeClicked = onEpisodeClicked)
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
private fun EpisodeItem(episode: Episode, onEpisodeClicked: (episodeId: String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                onEpisodeClicked(episode.id)
            },
    ) {
        Text(style = MaterialTheme.typography.labelSmall, text = episode.podcastName)
        Spacer(modifier = Modifier.height(8.dp))
        Text(style = MaterialTheme.typography.bodyMedium, text = episode.title)
        Spacer(modifier = Modifier.height(8.dp))
        Text(style = MaterialTheme.typography.bodySmall, text = episode.description, maxLines = 2)
    }
}
