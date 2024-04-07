package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState

@Composable
fun EpisodeItem(
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
            .padding(vertical = 8.dp, horizontal = 16.dp),
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
