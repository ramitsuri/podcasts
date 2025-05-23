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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.utils.dateFormatted
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeHistoryViewState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EpisodeHistoryScreen(
    state: EpisodeHistoryViewState,
    onBack: () -> Unit,
    onEpisodeClicked: (episodeId: String, podcastId: Long) -> Unit,
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
                items(items = episodes, key = { date.toString() + it.episode.id + it.sessionId }) {
                    val episode = it.episode
                    ColoredHorizontalDivider()
                    EpisodeItem(
                        episode = episode,
                        playingState =
                            if (state.currentlyPlayingEpisodeId == episode.id) {
                                state.currentlyPlayingEpisodeState
                            } else {
                                PlayingState.NOT_PLAYING
                            },
                        onClicked = { onEpisodeClicked(episode.id, episode.podcastId) },
                        onPlayClicked = { onEpisodePlayClicked(episode) },
                        onPauseClicked = onEpisodePauseClicked,
                        onAddToQueueClicked = { onEpisodeAddToQueueClicked(episode) },
                        onRemoveFromQueueClicked = { onEpisodeRemoveFromQueueClicked(episode) },
                        onDownloadClicked = { onEpisodeDownloadClicked(episode) },
                        onRemoveDownloadClicked = { onEpisodeRemoveDownloadClicked(episode) },
                        onCancelDownloadClicked = { onEpisodeCancelDownloadClicked(episode) },
                        onPlayedClicked = { onEpisodePlayedClicked(episode.id) },
                        onNotPlayedClicked = { onEpisodeNotPlayedClicked(episode.id) },
                        onFavoriteClicked = { onEpisodeFavoriteClicked(episode.id) },
                        onNotFavoriteClicked = { onEpisodeNotFavoriteClicked(episode.id) },
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(128.dp))
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
    Column(
        modifier =
            Modifier
                .clickable(onClick = onClicked)
                .padding(top = 12.dp, bottom = 4.dp)
                .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                url = episode.podcastImageUrl,
                contentDescription = episode.title,
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .size(40.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    text = episode.podcastName,
                )
                val datePublished = episode.datePublishedInstant
                if (datePublished != null) {
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = friendlyPublishDate(publishedDateTime = datePublished),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = MaterialTheme.typography.bodyLarge,
            text = episode.title,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
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
