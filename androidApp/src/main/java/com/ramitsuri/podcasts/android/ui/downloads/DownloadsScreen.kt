package com.ramitsuri.podcasts.android.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeItem
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.DownloadsViewState

@Composable
fun DownloadsScreen(
    state: DownloadsViewState,
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
        TopAppBar(onBack = onBack, label = stringResource(id = R.string.library_downloads))
        LazyColumn {
            items(state.episodes, key = { it.id }) {
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
        if (state.episodes.isEmpty()) {
            DownloadsEmpty()
        }
    }
}

@Composable
private fun DownloadsEmpty() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ArrowCircleDown,
            contentDescription = stringResource(id = R.string.library_downloads),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.downloads_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = R.string.downloads_empty_info),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
