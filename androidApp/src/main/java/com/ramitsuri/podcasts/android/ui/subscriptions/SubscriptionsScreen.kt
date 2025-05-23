package com.ramitsuri.podcasts.android.ui.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Subscriptions
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.components.podcast
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.ui.SubscriptionsViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    state: SubscriptionsViewState,
    onBack: () -> Unit,
    onPodcastClicked: (podcastId: Long) -> Unit,
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
            label = stringResource(id = R.string.subscriptions),
            scrollBehavior = scrollBehavior,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 112.dp),
            modifier =
                Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 16.dp),
        ) {
            fullWidthSpacer()
            items(items = state.subscribedPodcasts, key = { it.id }) { podcast ->
                SubscribedPodcastItem(podcast = podcast, onClicked = { onPodcastClicked(it.id) })
            }
            fullWidthSpacer(height = 128.dp)
        }
        if (state.subscribedPodcasts.isEmpty()) {
            SubscriptionsEmpty()
        }
    }
}

private fun LazyGridScope.fullWidthSpacer(height: Dp = 16.dp) {
    item(
        span = {
            GridItemSpan(maxLineSpan)
        },
    ) {
        Spacer(modifier = Modifier.height(height))
    }
}

@Composable
private fun SubscribedPodcastItem(
    podcast: Podcast,
    onClicked: (Podcast) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = { onClicked(podcast) })
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            url = podcast.artwork,
            contentDescription = podcast.title,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .size(112.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = podcast.title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )
    }
}

@Composable
private fun SubscriptionsEmpty() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Subscriptions,
            contentDescription = stringResource(id = R.string.subscriptions),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.subscriptions_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = R.string.subscriptions_empty_info),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@ThemePreview
@Composable
private fun SubscriptionsScreenPreview_NotEmpty() {
    PreviewTheme {
        SubscriptionsScreen(
            state =
                SubscriptionsViewState(
                    subscribedPodcasts = (1..50).map { podcast() },
                ),
            onBack = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SubscriptionsScreenPreview_Empty() {
    PreviewTheme {
        SubscriptionsScreen(
            state = SubscriptionsViewState(),
            onBack = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SubscribedPodcastItemPreview() {
    PreviewTheme {
        SubscribedPodcastItem(
            podcast = podcast(),
            onClicked = { },
        )
    }
}
