package com.ramitsuri.podcasts.android.ui.importsub

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R

@Composable
fun ImportSubscriptionsScreen(
    viewState: ImportSubscriptionsViewState,
    onSubscriptionsDataFilePicked: (Uri) -> Unit,
    onSubscribeAllPodcasts: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri == null) {
                    onBack()
                    return@rememberLauncherForActivityResult
                }
                onSubscriptionsDataFilePicked(uri)
            },
        )
    LaunchedEffect(Unit) {
        filePicker.launch("text/xml")
    }
    LaunchedEffect(viewState) {
        if (viewState.subscribed) {
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

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewState.podcasts) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(style = MaterialTheme.typography.labelSmall, text = it.author)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(style = MaterialTheme.typography.bodyMedium, text = it.title)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(style = MaterialTheme.typography.bodySmall, text = it.description, maxLines = 2)
                }
            }
        }

        Button(onClick = onSubscribeAllPodcasts) {
            Text(text = stringResource(id = R.string.import_subscriptions_subscribe_all))
        }
    }
}
