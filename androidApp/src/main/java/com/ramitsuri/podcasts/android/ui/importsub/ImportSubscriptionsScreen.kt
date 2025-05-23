package com.ramitsuri.podcasts.android.ui.importsub

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.PodcastInfo
import com.ramitsuri.podcasts.android.ui.components.PodcastInfoItem
import com.ramitsuri.podcasts.android.ui.components.podcast

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImportSubscriptionsScreen(
    viewState: ImportSubscriptionsViewState,
    onSubscriptionsDataFilePicked: (Uri) -> Unit,
    onSubscribeAllPodcasts: () -> Unit,
    onSuggestionAccepted: (FailedToImportWithSuggestion) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                if (uri == null) {
                    onBack()
                    return@rememberLauncherForActivityResult
                }
                onSubscriptionsDataFilePicked(uri)
            },
        )
    LaunchedEffect(Unit) {
        filePicker.launch(arrayOf("*/*"))
    }
    LaunchedEffect(viewState) {
        if (viewState.subscribed) {
            onBack()
        }
        if (viewState.failure) {
            Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
    Column(
        modifier =
            modifier
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (viewState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            if (viewState.failedToImportWithSuggestion.isNotEmpty()) {
                stickyHeader {
                    Header(text = stringResource(id = R.string.import_subscriptions_failed_to_import_but_found))
                }
                itemsIndexed(viewState.failedToImportWithSuggestion) { index, item ->
                    ColoredHorizontalDivider()
                    FailedToImportWithSuggestionItem(failed = item, onSuggestionAccepted = onSuggestionAccepted)
                    if (index == viewState.failedToImportWithSuggestion.lastIndex) {
                        ColoredHorizontalDivider()
                    }
                }
            }
            if (viewState.failedToImport.isNotEmpty()) {
                stickyHeader {
                    Header(text = stringResource(id = R.string.import_subscriptions_failed_to_import))
                }
                itemsIndexed(viewState.failedToImport) { index, item ->
                    ColoredHorizontalDivider()
                    FailedToImportItem(text = item)
                    if (index == viewState.failedToImport.lastIndex) {
                        ColoredHorizontalDivider()
                    }
                }
            }
            if (viewState.imported.isNotEmpty()) {
                stickyHeader {
                    Header(text = stringResource(id = R.string.import_subscriptions_imported))
                }
                itemsIndexed(viewState.imported) { index, item ->
                    ColoredHorizontalDivider()
                    PodcastInfoItem(podcast = item)
                    if (index == viewState.imported.lastIndex) {
                        ColoredHorizontalDivider()
                    }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FilledTonalButton(onClick = onSubscribeAllPodcasts) {
                Text(text = stringResource(id = R.string.import_subscriptions_subscribe_all))
            }
        }
    }
}

@Composable
private fun Header(text: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FailedToImportItem(text: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text)
    }
}

@Composable
private fun FailedToImportWithSuggestionItem(
    failed: FailedToImportWithSuggestion,
    onSuggestionAccepted: (FailedToImportWithSuggestion) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PodcastInfo(podcast = failed.suggestion)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { onSuggestionAccepted(failed) }) {
            Text(text = stringResource(id = R.string.import_subscriptions_failed_import_confirm_suggestion))
        }
    }
}

@ThemePreview
@Composable
private fun FailedToImportWithSuggestionPreview() {
    PreviewTheme {
        FailedToImportWithSuggestionItem(
            failed =
                FailedToImportWithSuggestion(
                    text = "Stuff you should know",
                    suggestion = podcast(),
                ),
            onSuggestionAccepted = { },
        )
    }
}

@ThemePreview
@Composable
private fun PodcastInfoItemPreview() {
    PreviewTheme {
        PodcastInfoItem(podcast())
    }
}

@ThemePreview
@Composable
private fun FailedToImportPreview() {
    PreviewTheme {
        FailedToImportItem(
            text = "Stuff you should know",
        )
    }
}

@ThemePreview
@Composable
private fun ImportSubscriptionsScreenPreview() {
    val viewState =
        ImportSubscriptionsViewState(
            isLoading = false,
            subscribed = false,
            imported = listOf(podcast(), podcast(), podcast()),
            failedToImport = listOf("How To Money", "The Bike Shed"),
            failedToImportWithSuggestion =
                listOf(
                    FailedToImportWithSuggestion(text = "Stuff you should know", podcast()),
                ),
        )
    PreviewTheme {
        ImportSubscriptionsScreen(
            viewState = viewState,
            onSubscriptionsDataFilePicked = { },
            onSubscribeAllPodcasts = { },
            onSuggestionAccepted = { },
            onBack = { },
        )
    }
}
