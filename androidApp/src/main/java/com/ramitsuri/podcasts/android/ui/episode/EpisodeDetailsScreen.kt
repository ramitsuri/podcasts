package com.ramitsuri.podcasts.android.ui.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState

@Composable
fun EpisodeDetailsScreen(
    state: EpisodeDetailsViewState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TopAppBar(onBack = onBack)
        val episode = state.episode
        if (episode != null) {
            EpisodeDetails(episode)
        }
    }
}

@Composable
private fun EpisodeDetails(episode: Episode) {
    Column(
        modifier =
            Modifier
                .padding(16.dp),
    ) {
        Text(style = MaterialTheme.typography.labelSmall, text = episode.podcastName)
        Text(style = MaterialTheme.typography.labelSmall, text = episode.podcastAuthor)
        Text(style = MaterialTheme.typography.labelSmall, text = episode.datePublished.toString())
        Text(style = MaterialTheme.typography.labelSmall, text = episode.title)
        Text(style = MaterialTheme.typography.labelSmall, text = episode.downloadStatus.value)
        Text(style = MaterialTheme.typography.labelSmall, text = episode.description)
    }
}
