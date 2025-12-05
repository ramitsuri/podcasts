package com.ramitsuri.podcasts.android.ui.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.BottomSheetDialog
import com.ramitsuri.podcasts.android.ui.components.BottomSheetDialogMenuItem
import com.ramitsuri.podcasts.android.ui.components.CenteredTitleTopAppBar
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.ui.components.trendingPodcast
import com.ramitsuri.podcasts.model.TrendingPodcast
import com.ramitsuri.podcasts.model.ui.ExploreViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreViewState,
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onPodcastClicked: (Long) -> Unit,
    onLanguageClicked: (String) -> Unit,
    onCategoryClicked: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        CenteredTitleTopAppBar(
            currentlyPlayingArtworkUrl = state.currentlyPlayingEpisodeArtworkUrl,
            scrollBehavior = scrollBehavior,
            onSettingsClicked = onSettingsClicked,
        )
        SearchRow(onSearchClicked = onSearchClicked)
        ExploreContent(
            state = state,
            scrollBehavior = scrollBehavior,
            onPodcastClicked = onPodcastClicked,
            onLanguageClicked = onLanguageClicked,
            onCategoryClicked = onCategoryClicked,
            onRefresh = onRefresh,
        )
    }
}

@Composable
private fun SearchRow(onSearchClicked: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min),
    ) {
        OutlinedTextField(
            value = TextFieldValue(text = ""),
            onValueChange = {},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.search),
                )
            },
            singleLine = true,
            label = { Text(stringResource(id = R.string.search)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusProperties { canFocus = false },
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(
                        onClick = onSearchClicked,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreContent(
    state: ExploreViewState,
    scrollBehavior: TopAppBarScrollBehavior,
    onPodcastClicked: (Long) -> Unit,
    onLanguageClicked: (String) -> Unit,
    onCategoryClicked: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    var showLanguageCategoriesSelector by remember { mutableStateOf(false) }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var showCategoriesSelector by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TrendingPodcastsHeader(
                onShowLanguageCategoriesSelector = { showLanguageCategoriesSelector = true },
            )
            if (state.podcastsByCategory.isEmpty()) {
                EmptyItem(
                    modifier =
                        Modifier
                            .weight(1f),
                )
            } else {
                TrendingPodcasts(
                    modifier =
                        Modifier
                            .weight(1f)
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                    podcastsByCategory = state.podcastsByCategory,
                    onPodcastClicked = onPodcastClicked,
                )
            }
        }
    }
    LanguageCategoriesSelector(
        show = showLanguageCategoriesSelector,
        selectedLanguages = state.selectedLanguages,
        selectedCategories = state.selectedCategories,
        onDismissRequest = { showLanguageCategoriesSelector = false },
        onLanguageSelectorClicked = {
            showLanguageCategoriesSelector = false
            showLanguageSelector = true
        },
        onCategorySelectorClicked = {
            showLanguageCategoriesSelector = false
            showCategoriesSelector = true
        },
    )
    LanguageSelector(
        show = showLanguageSelector,
        selectedLanguages = state.selectedLanguages,
        languages = state.languages,
        onLanguageClicked = {
            onLanguageClicked(it)
        },
        onDismissRequest = { showLanguageSelector = false },
    )
    CategoriesSelector(
        show = showCategoriesSelector,
        selectedCategories = state.selectedCategories,
        categories = state.categories,
        onCategoryClicked = {
            onCategoryClicked(it)
        },
        onDismissRequest = { showCategoriesSelector = false },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrendingPodcasts(
    modifier: Modifier = Modifier,
    podcastsByCategory: Map<String, List<TrendingPodcast>>,
    onPodcastClicked: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        podcastsByCategory
            .forEach { (category, podcasts) ->
                stickyHeader {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp),
                    )
                }
                item {
                    PodcastsRow(podcasts = podcasts, onPodcastClicked = onPodcastClicked)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ColoredHorizontalDivider()
                }
            }
        item {
            Spacer(modifier = Modifier.height(128.dp))
        }
    }
}

@Composable
private fun TrendingPodcastsHeader(onShowLanguageCategoriesSelector: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.trending),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onShowLanguageCategoriesSelector) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = stringResource(id = R.string.settings),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun EmptyItem(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = stringResource(id = R.string.trending_podcasts_unavailable),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.trending_podcasts_unavailable),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PodcastsRow(
    podcasts: List<TrendingPodcast>,
    onPodcastClicked: (Long) -> Unit,
) {
    if (podcasts.isEmpty()) {
        Text(
            text = stringResource(R.string.trending_podcasts_no_podcasts_for_category),
            modifier = Modifier.padding(24.dp),
        )
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(podcasts) { podcast ->
            TrendingPodcastItem(
                podcast = podcast,
                onClicked = { onPodcastClicked(it.id) },
            )
        }
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
                .padding(8.dp)
                .sizeIn(maxWidth = 112.dp),
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

@Composable
private fun LanguageCategoriesSelector(
    show: Boolean,
    selectedLanguages: List<String>,
    selectedCategories: List<String>,
    onDismissRequest: () -> Unit,
    onLanguageSelectorClicked: () -> Unit,
    onCategorySelectorClicked: () -> Unit,
) {
    BottomSheetDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        content = {
            BottomSheetDialogMenuItem(
                text = stringResource(id = R.string.language),
                subtitle = selectedLanguages.joinToString(", "),
                endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = onLanguageSelectorClicked,
            )
            BottomSheetDialogMenuItem(
                text = stringResource(id = R.string.categories),
                subtitle = selectedCategories.joinToString(", "),
                endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = onCategorySelectorClicked,
            )
        },
    )
}

@Composable
private fun LanguageSelector(
    show: Boolean,
    selectedLanguages: List<String>,
    languages: List<String>,
    onLanguageClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Selector(
        show = show,
        helperText = stringResource(id = R.string.trending_two_languages),
        selectedItems = selectedLanguages,
        allItems = languages,
        onItemClicked = onLanguageClicked,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun CategoriesSelector(
    show: Boolean,
    selectedCategories: List<String>,
    categories: List<String>,
    onCategoryClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Selector(
        show = show,
        helperText = stringResource(id = R.string.trending_five_categories),
        selectedItems = selectedCategories,
        allItems = categories,
        onItemClicked = onCategoryClicked,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun Selector(
    show: Boolean,
    helperText: String,
    selectedItems: List<String>,
    allItems: List<String>,
    onItemClicked: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BottomSheetDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        content = {
            var searchText by remember { mutableStateOf("") }
            val itemsToShow =
                if (searchText.isBlank() && selectedItems.isNotEmpty()) {
                    selectedItems + null + allItems // null is for divider
                } else {
                    allItems.filter { it.lowercase().startsWith(searchText.lowercase()) }
                }
            SearchTextInput(
                term = searchText,
                onSearchTermUpdated = { searchText = it },
                onSearchCleared = { searchText = "" },
            )
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn {
                items(
                    items = itemsToShow,
                ) { item ->
                    if (item == null) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 64.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            ColoredHorizontalDivider()
                        }
                    } else {
                        BottomSheetDialogMenuItem(
                            text = item,
                            endIcon =
                                if (selectedItems.contains(item)) {
                                    Icons.Default.Check
                                } else {
                                    null
                                },
                            onClick = { onItemClicked(item) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun SearchTextInput(
    term: String,
    onSearchTermUpdated: (String) -> Unit,
    onSearchCleared: () -> Unit,
) {
    var selection by remember { mutableStateOf(TextRange(term.length)) }

    OutlinedTextField(
        value = TextFieldValue(text = term, selection = selection),
        onValueChange = {
            onSearchTermUpdated(it.text)
            selection = it.selection
        },
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.search),
            )
        },
        trailingIcon =
            if (term.isNotEmpty()) {
                {
                    IconButton(onClick = onSearchCleared) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.search),
                        )
                    }
                }
            } else {
                { }
            },
        singleLine = true,
        label = { Text(stringResource(id = R.string.search)) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    )
}

@ThemePreview
@Composable
private fun PreviewExploreScreen() {
    PreviewTheme {
        ExploreScreen(
            state =
                ExploreViewState(
                    isRefreshing = false,
                    podcastsByCategory =
                        mapOf(
                            "News" to
                                listOf(
                                    trendingPodcast(id = 1),
                                    trendingPodcast(id = 2),
                                    trendingPodcast(id = 3),
                                ),
                            "Music" to
                                listOf(),
                            "Politics" to
                                listOf(
                                    trendingPodcast(id = 1),
                                    trendingPodcast(id = 2),
                                    trendingPodcast(id = 3),
                                ),
                            "Entertainment" to
                                listOf(
                                    trendingPodcast(id = 1),
                                    trendingPodcast(id = 2),
                                    trendingPodcast(id = 3),
                                ),
                            "Sports" to
                                listOf(
                                    trendingPodcast(id = 1),
                                    trendingPodcast(id = 2),
                                    trendingPodcast(id = 3),
                                ),
                        ),
                    languages = listOf(),
                    selectedLanguages = listOf("English"),
                ),
            onSearchClicked = {},
            onSettingsClicked = {},
            onPodcastClicked = {},
            onLanguageClicked = {},
            onCategoryClicked = {},
            onRefresh = {},
        )
    }
}

@ThemePreview
@Composable
private fun PreviewExploreScreenPodcastsEmpty() {
    PreviewTheme {
        ExploreScreen(
            state =
                ExploreViewState(
                    isRefreshing = false,
                    podcastsByCategory =
                        mapOf(),
                    languages = listOf(),
                    selectedLanguages = listOf("English", "Spanish"),
                ),
            onSearchClicked = {},
            onSettingsClicked = {},
            onPodcastClicked = {},
            onLanguageClicked = {},
            onCategoryClicked = {},
            onRefresh = {},
        )
    }
}
