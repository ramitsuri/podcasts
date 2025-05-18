package com.ramitsuri.podcasts.android.ui.explore

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.CenteredTitleTopAppBar
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.PodcastInfoItem
import com.ramitsuri.podcasts.android.ui.components.podcast
import com.ramitsuri.podcasts.model.Podcast
import com.ramitsuri.podcasts.model.ui.ExploreViewState
import com.ramitsuri.podcasts.model.ui.SearchResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    state: ExploreViewState,
    modifier: Modifier = Modifier,
    onSettingsClicked: () -> Unit,
    onPodcastClicked: (Long) -> Unit,
    onSearchTermUpdated: (String) -> Unit,
    onSearchRequested: () -> Unit,
    onSearchCleared: () -> Unit,
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
            onSettingsClicked = onSettingsClicked,
        )
        if (state.result !is SearchResult.Searching) {
            Spacer(modifier = Modifier.height(16.dp))
            SearchInput(
                term = state.term,
                showKeyboardAutomatically = state.result == SearchResult.Default,
                onSearchTermUpdated = onSearchTermUpdated,
                onSearchRequested = onSearchRequested,
                onSearchCleared = onSearchCleared,
            )
        }
        SearchOutput(
            scrollBehavior = scrollBehavior,
            searchResult = state.result,
            onPodcastClicked = onPodcastClicked,
        )
    }
}

@Composable
private fun SearchInput(
    term: String,
    showKeyboardAutomatically: Boolean,
    onSearchTermUpdated: (String) -> Unit,
    onSearchRequested: () -> Unit,
    onSearchCleared: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var selection by remember { mutableStateOf(TextRange(term.length)) }

    LaunchedEffect(focusRequester) {
        if (showKeyboardAutomatically) {
            delay(100)
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
    ) {
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
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearchRequested()
                    },
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchOutput(
    scrollBehavior: TopAppBarScrollBehavior?,
    searchResult: SearchResult,
    onPodcastClicked: (Long) -> Unit,
) {
    when (searchResult) {
        is SearchResult.Default,
        is SearchResult.Error,
        -> {
            val context = LocalContext.current
            LaunchedEffect(searchResult) {
                if (searchResult is SearchResult.Error) {
                    Toast.makeText(context, context.getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
                }
            }
            SearchDefault()
        }

        is SearchResult.Searching -> {
            SearchLoading()
        }

        is SearchResult.Success -> {
            SearchResults(
                scrollBehavior = scrollBehavior,
                podcasts = searchResult.podcasts,
                onPodcastClicked = onPodcastClicked,
            )
        }
    }
}

@Composable
private fun SearchDefault() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(id = R.string.search_tip),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.search_tip),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SearchLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResults(
    scrollBehavior: TopAppBarScrollBehavior?,
    podcasts: List<Podcast>,
    onPodcastClicked: (Long) -> Unit,
) {
    val modifier =
        if (scrollBehavior == null) {
            Modifier
        } else {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        }
    if (podcasts.isNotEmpty()) {
        LazyColumn(
            modifier =
                modifier
                    .fillMaxWidth(),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(items = podcasts, key = { it.id }) {
                ColoredHorizontalDivider()
                PodcastInfoItem(it, onClick = onPodcastClicked)
            }
            item {
                Spacer(modifier = Modifier.height(128.dp))
            }
        }
    } else {
        SearchResultsEmpty()
    }
}

@Composable
private fun SearchResultsEmpty() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = stringResource(id = R.string.search_did_not_find_anything),
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.search_did_not_find_anything),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@ThemePreview
@Composable
private fun SearchScreenPreview_Default() {
    PreviewTheme {
        ExploreScreen(
            state = ExploreViewState(term = ""),
            onSettingsClicked = { },
            onSearchTermUpdated = { },
            onSearchRequested = { },
            onSearchCleared = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SearchScreenPreview_Default_SearchTermNotEmpty() {
    PreviewTheme {
        ExploreScreen(
            state = ExploreViewState(term = "Science Vs"),
            onSettingsClicked = { },
            onSearchTermUpdated = { },
            onSearchRequested = { },
            onSearchCleared = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SearchScreenPreview_Loading() {
    PreviewTheme {
        ExploreScreen(
            state = ExploreViewState(term = "", result = SearchResult.Searching),
            onSettingsClicked = { },
            onSearchTermUpdated = { },
            onSearchRequested = { },
            onSearchCleared = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SearchScreenPreview_Error() {
    PreviewTheme {
        ExploreScreen(
            state = ExploreViewState(term = "", result = SearchResult.Error),
            onSettingsClicked = { },
            onSearchTermUpdated = { },
            onSearchRequested = { },
            onSearchCleared = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SearchScreenPreview_Success_Empty() {
    PreviewTheme {
        ExploreScreen(
            state = ExploreViewState(term = "", result = SearchResult.Success(listOf())),
            onSettingsClicked = { },
            onSearchTermUpdated = { },
            onSearchRequested = { },
            onSearchCleared = { },
            onPodcastClicked = { },
        )
    }
}

@ThemePreview
@Composable
private fun SearchScreenPreview_Success_NotEmpty() {
    PreviewTheme {
        ExploreScreen(
            state =
                ExploreViewState(
                    term = "",
                    result =
                        SearchResult.Success(
                            listOf(
                                podcast(),
                                podcast(),
                                podcast(),
                                podcast(),
                                podcast(),
                                podcast(),
                            ),
                        ),
                ),
            onSettingsClicked = { },
            onSearchTermUpdated = { },
            onSearchRequested = { },
            onSearchCleared = { },
            onPodcastClicked = { },
        )
    }
}
