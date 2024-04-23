package com.ramitsuri.podcasts.android.ui.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.utils.friendlyFetchDateTime
import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.utils.Constants
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes
import com.ramitsuri.podcasts.android.utils.Constants as AppConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsViewState,
    onBack: () -> Unit,
    toggleAutoPlayNextInQueue: () -> Unit,
    onFetchRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(onBack = onBack, label = stringResource(id = R.string.settings), scrollBehavior = scrollBehavior)
        PlaybackSettings(
            autoPlayNextInQueue = state.autoPlayNextInQueue,
            toggleAutoPlayNextInQueue = toggleAutoPlayNextInQueue,
        )
        ColoredHorizontalDivider()
        FetchSettings(
            fetching = state.fetching,
            lastFetchTime = state.lastFetchTime,
            onFetchRequested = onFetchRequested,
        )
        ColoredHorizontalDivider()
        AboutApp()
    }
}

@Composable
private fun PlaybackSettings(
    autoPlayNextInQueue: Boolean,
    toggleAutoPlayNextInQueue: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        CategoryTitle(text = stringResource(id = R.string.settings_playback))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = toggleAutoPlayNextInQueue)
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Title(text = stringResource(id = R.string.settings_auto_play_next_in_queue))
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = autoPlayNextInQueue,
                onCheckedChange = null,
                thumbContent =
                    if (autoPlayNextInQueue) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
            )
        }
    }
}

@Composable
private fun FetchSettings(
    fetching: Boolean,
    lastFetchTime: Instant,
    onFetchRequested: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        CategoryTitle(text = stringResource(id = R.string.settings_fetch))
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = !fetching,
                        onClick = onFetchRequested,
                    )
                    .padding(16.dp),
        ) {
            Title(text = stringResource(id = R.string.settings_fetch_now))
            if (fetching) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                val text =
                    if (lastFetchTime == Constants.NEVER_FETCHED_TIME) {
                        stringResource(id = R.string.settings_last_fetched_never)
                    } else {
                        stringResource(
                            id = R.string.settings_last_fetched_at_time_format,
                            friendlyFetchDateTime(
                                fetchDateTime = lastFetchTime,
                            ),
                        )
                    }
                Subtitle(text = text)
            }
        }
    }
}

@Composable
private fun CategoryTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun Subtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun AboutApp() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val appVersion = context.packageManager.getPackageInfo(context.packageName, 0)?.versionName ?: ""
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        CategoryTitle(text = stringResource(id = R.string.settings_about))

        // Version
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Title(text = stringResource(id = R.string.settings_version))
            Subtitle(text = appVersion)
        }

        // View source
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clickable { uriHandler.openUri(AppConstants.GITHUB_LINK) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Title(text = stringResource(id = R.string.settings_view_source))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
            )
        }
    }
}

@ThemePreview
@Composable
private fun SettingsPreview_LastFetchTimeNever() {
    PreviewTheme {
        SettingsScreen(
            state = SettingsViewState(autoPlayNextInQueue = true),
            onBack = { },
            toggleAutoPlayNextInQueue = { },
            onFetchRequested = { },
        )
    }
}

@ThemePreview
@Composable
private fun SettingsPreview_LastFetchTimeMinutesAgo() {
    PreviewTheme {
        SettingsScreen(
            state = SettingsViewState(autoPlayNextInQueue = true, lastFetchTime = Clock.System.now().minus(1.minutes)),
            onBack = { },
            toggleAutoPlayNextInQueue = { },
            onFetchRequested = { },
        )
    }
}

@ThemePreview
@Composable
private fun SettingsPreview_Fetching() {
    PreviewTheme {
        SettingsScreen(
            state = SettingsViewState(autoPlayNextInQueue = true, fetching = true),
            onBack = { },
            toggleAutoPlayNextInQueue = { },
            onFetchRequested = { },
        )
    }
}
