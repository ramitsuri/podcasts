package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
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
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.model.Podcast

@Composable
fun PodcastInfoItem(
    podcast: Podcast,
    onClick: ((Long) -> Unit)? = null,
) {
    val modifier =
        if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable(onClick = { onClick(podcast.id) })
        }
    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PodcastInfo(podcast = podcast)
        }
    }
}

@Composable
fun PodcastInfo(podcast: Podcast) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(podcast.artwork)
                    .crossfade(true)
                    .build(),
            contentDescription = podcast.title,
            contentScale = ContentScale.FillBounds,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .size(96.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(style = MaterialTheme.typography.labelSmall, text = podcast.title)
            Text(style = MaterialTheme.typography.bodySmall, text = podcast.author, maxLines = 3)
        }
    }
}

@ThemePreview
@Composable
private fun PodcastInfoItemPreview() {
    PreviewTheme {
        PodcastInfoItem(podcast())
    }
}
