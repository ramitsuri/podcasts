package com.ramitsuri.podcasts.android.ui.episode

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.HtmlStyle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.EpisodeControls
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.components.episode
import com.ramitsuri.podcasts.android.utils.friendlyPublishDate
import com.ramitsuri.podcasts.model.Episode
import com.ramitsuri.podcasts.model.PlayingState
import com.ramitsuri.podcasts.model.ui.EpisodeDetailsViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailsScreen(
    state: EpisodeDetailsViewState,
    onBack: () -> Unit,
    onPodcastNameClicked: (podcastId: Long) -> Unit,
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
    var showErrorDialog by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(onBack = onBack, scrollBehavior = scrollBehavior)
        val episode = state.episode
        if (episode != null) {
            EpisodeDetails(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                episode = episode,
                playingState = state.playingState,
                onPodcastNameClicked = { onPodcastNameClicked(episode.podcastId) },
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
            Spacer(modifier = Modifier.height(128.dp))
        } else if (state.loading) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            LaunchedEffect(Unit) {
                showErrorDialog = true
            }
        }
    }
    if (showErrorDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                onBack()
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(R.string.generic_error))
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onBack) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun EpisodeDetails(
    modifier: Modifier = Modifier,
    episode: Episode,
    playingState: PlayingState,
    onPodcastNameClicked: () -> Unit,
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
            modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Image(
                url = episode.podcastImageUrl,
                contentDescription = episode.title,
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .size(64.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onPodcastNameClicked),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = episode.podcastName,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = episode.podcastAuthor,
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val datePublished = episode.datePublishedInstant
        if (datePublished != null) {
            Text(
                style = MaterialTheme.typography.labelSmall,
                text = friendlyPublishDate(publishedDateTime = datePublished),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(style = MaterialTheme.typography.titleLarge, text = episode.title)
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
        val htmlStyle =
            HtmlStyle.DEFAULT.copy(
                linkSpanStyle =
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    ),
            )
        val convertedText =
            remember(episode.description) {
                htmlToAnnotatedString(html = episode.description, style = htmlStyle)
            }
        val color = LocalContentColor.current
        val uriHandler = LocalUriHandler.current
        ClickableText(
            style = MaterialTheme.typography.bodyMedium.copy(color = color),
            text = convertedText,
            modifier = Modifier.fillMaxWidth(),
            onClick = { position ->
                convertedText
                    .getUrlAnnotations(position, position)
                    .firstOrNull()?.let { range ->
                        uriHandler.openUri(range.item.url)
                    }
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@ThemePreview
@Composable
private fun EpisodeDetailsPreview() {
    PreviewTheme {
        EpisodeDetailsScreen(
            state = EpisodeDetailsViewState(episode = episode()),
            onBack = { },
            onPodcastNameClicked = { },
            onEpisodePlayClicked = { },
            onEpisodePauseClicked = { },
            onEpisodeAddToQueueClicked = { },
            onEpisodeRemoveFromQueueClicked = { },
            onEpisodeDownloadClicked = { },
            onEpisodeRemoveDownloadClicked = { },
            onEpisodeCancelDownloadClicked = { },
            onEpisodePlayedClicked = { },
            onEpisodeNotPlayedClicked = { },
            onEpisodeFavoriteClicked = { },
            onEpisodeNotFavoriteClicked = { },
        )
    }
}
