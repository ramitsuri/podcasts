package com.ramitsuri.podcasts.android.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.CenteredTitleTopAppBar
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.model.TrendingPodcast
import com.ramitsuri.podcasts.model.ui.ExploreViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreViewState,
    modifier: Modifier = Modifier,
    onSearchClicked: (removeFromBackstack: Boolean) -> Unit,
    onSettingsClicked: () -> Unit,
    onPodcastClicked: (Long) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        CenteredTitleTopAppBar(
            scrollBehavior = scrollBehavior,
            onSearchClicked = { onSearchClicked(false) },
            onSettingsClicked = onSettingsClicked,
        )
        ExploreContent(
            state = state,
            scrollBehavior = scrollBehavior,
            onPodcastClicked = onPodcastClicked,
            navToSearch = { onSearchClicked(true) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.ExploreContent(
    state: ExploreViewState,
    scrollBehavior: TopAppBarScrollBehavior,
    onPodcastClicked: (Long) -> Unit,
    navToSearch: () -> Unit,
) {
    when (state) {
        is ExploreViewState.Loading -> {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        is ExploreViewState.Success -> {
            LaunchedEffect(state) {
                if (state.podcasts.isEmpty()) {
                    navToSearch()
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 112.dp),
                modifier =
                    Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                fullWidthItem {
                    Text(
                        text = stringResource(id = R.string.trending),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
                fullWidthItem {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(items = state.podcasts, key = { it.id }) { podcast ->
                    TrendingPodcastItem(
                        podcast = podcast,
                        onClicked = { onPodcastClicked(it.id) },
                    )
                }
                fullWidthItem {
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidthItem(content: @Composable () -> Unit) {
    item(
        span = {
            GridItemSpan(maxLineSpan)
        },
    ) {
        content()
    }
}

@Composable
private fun TrendingPodcastItem(
    podcast: TrendingPodcast,
    onClicked: (TrendingPodcast) -> Unit,
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
        Text(
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            text = podcast.author,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
