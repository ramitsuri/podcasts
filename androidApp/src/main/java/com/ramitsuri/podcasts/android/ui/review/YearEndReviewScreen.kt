package com.ramitsuri.podcasts.android.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.model.ui.YearEndReviewViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearEndReviewScreen(
    state: YearEndReviewViewState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showError by remember { mutableStateOf(false) }
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        TopAppBar(
            onBack = onBack,
            scrollBehavior = null,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state) {
                is YearEndReviewViewState.Data -> {
                    YearEndReviewContent(state)
                }

                is YearEndReviewViewState.Error -> {
                    LaunchedEffect(Unit) {
                        showError = true
                    }
                }

                is YearEndReviewViewState.Loading -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
    if (showError) {
        BasicAlertDialog(
            onDismissRequest = {
                showError = false
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = "Error")
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onBack) {
                    Text(text = "OK")
                }
            }
        }
    }
}

@Composable
private fun YearEndReviewContent(data: YearEndReviewViewState.Data) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Year: ${data.year}")
        Text("Listening since: ${data.listeningSince}")
        Text("Most listened to podcasts: ${data.mostListenedToPodcasts.joinToString(", ") { it.title }}")
        Text("Total duration listened: ${data.totalDurationListened}")
        Text("Total actual duration listened: ${data.totalActualDurationListened}")
        Text("Total episodes listened: ${data.totalEpisodesListened}")
        Text("Most listened on day of week: ${data.mostListenedOnDayOfWeek}")
        Text("Most listened on day: ${data.mostListenedOnDay}")
        Text("Most listened month: ${data.mostListenedMonth}")
    }
}
