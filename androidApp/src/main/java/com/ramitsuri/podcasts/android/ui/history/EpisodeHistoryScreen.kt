package com.ramitsuri.podcasts.android.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeItem
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.utils.dateFormatted
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeHistoryViewState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(
            onBack = onBack,
            label = stringResource(id = R.string.library_history),
            scrollBehavior = scrollBehavior,
        )
        LazyColumn(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
            state.episodesByDate.forEach { (date, episodes) ->
                stickyHeader {
                    HeaderItem(text = dateFormatted(date))
                }
                items(episodes) {
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
