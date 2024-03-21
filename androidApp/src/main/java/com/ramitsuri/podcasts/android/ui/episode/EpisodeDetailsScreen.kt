package com.ramitsuri.podcasts.android.ui.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState

@Composable
fun EpisodeDetailsScreen(
    state: EpisodeDetailsViewState,
    onBack: () -> Unit,
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
    Column(modifier = modifier) {
        TopAppBar(onBack = onBack)
        val episode = state.episode
        if (episode != null) {
            EpisodeDetails(
                episode,
                playingState = state.playingState,
                onPlayClicked = { onEpisodePlayClicked(episode.id) },
                onPauseClicked = { onEpisodePauseClicked(episode.id) },
                onAddToQueueClicked = { onEpisodeAddToQueueClicked(episode.id) },
                onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(episode.id) },
                onDownloadClicked = { onEpisodeDownloadClicked(episode.id) },
                onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(episode.id) },
                onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(episode.id) },
                onPlayedClicked = { onEpisodePlayedClicked(episode.id) },
                onNotPlayedClicked = { onEpisodeNotPlayedClicked(episode.id) },
            )
        }
    }
}

@Composable
private fun EpisodeDetails(
    episode: Episode,
    playingState: PlayingState,
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
                .padding(16.dp),
    ) {
        Text(style = MaterialTheme.typography.labelSmall, text = episode.podcastName)
        Text(style = MaterialTheme.typography.labelSmall, text = episode.podcastAuthor)
        Spacer(modifier = Modifier.height(8.dp))
        Text(style = MaterialTheme.typography.labelSmall, text = episode.friendlyDatePublished)
        Spacer(modifier = Modifier.height(8.dp))
        Text(style = MaterialTheme.typography.labelSmall, text = episode.title)
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
        Text(style = MaterialTheme.typography.labelSmall, text = episode.description)
    }
}

@ThemePreview
@Composable
private fun EpisodeDetailsPreview() {
    PreviewTheme {
        EpisodeDetailsScreen(
            state = EpisodeDetailsViewState(episode()),
            onBack = { },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeAddToQueueClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
        )
    }
}
