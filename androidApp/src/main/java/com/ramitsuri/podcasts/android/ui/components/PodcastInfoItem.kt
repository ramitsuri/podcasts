package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.model.Podcast

@Composable
fun PodcastInfoItem(
    podcast: Podcast,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    onClick: ((Long) -> Unit)? = null,
) {
    val modifier =
        if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable(onClick = { onClick(podcast.id) })
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PodcastInfo(podcast = podcast)
    }
}

@Composable
fun PodcastInfo(podcast: Podcast) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            url = podcast.artwork,
            contentDescription = podcast.title,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .size(96.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = podcast.title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )

            Text(
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                text = podcast.author,
                modifier = Modifier.fillMaxWidth(),
            )
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
