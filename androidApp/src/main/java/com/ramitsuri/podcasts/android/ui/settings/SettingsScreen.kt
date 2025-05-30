package com.ramitsuri.podcasts.android.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.android.ui.utils.isVisible
import com.ramitsuri.podcasts.android.utils.friendlyFetchDateTime
import com.ramitsuri.podcasts.model.RemoveDownloadsAfter
import com.ramitsuri.podcasts.model.ui.SettingsViewState
import com.ramitsuri.podcasts.utils.Constants
import kotlinx.coroutines.delay
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
    toggleShouldDownloadOnWifiOnly: () -> Unit,
    onFetchRequested: () -> Unit,
    onRemoveCompletedAfterSelected: (RemoveDownloadsAfter) -> Unit,
    onRemoveUnfinishedAfterSelected: (RemoveDownloadsAfter) -> Unit,
    onVersionClicked: () -> Unit,
    onBackupRestoreClicked: () -> Unit,
    onAddWidgetClicked: () -> Unit,
    onWidgetItemSeen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var markWidgetItemSeen by remember { mutableStateOf(false) }
    LaunchedEffect(markWidgetItemSeen) {
        if (markWidgetItemSeen) {
            delay(2000)
            onWidgetItemSeen()
        }
    }
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        TopAppBar(onBack = onBack, label = stringResource(id = R.string.settings), scrollBehavior = scrollBehavior)
        Column(
            modifier =
                Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState()),
        ) {
            PlaybackSettings(
                autoPlayNextInQueue = state.autoPlayNextInQueue,
                toggleAutoPlayNextInQueue = toggleAutoPlayNextInQueue,
            )
            ColoredHorizontalDivider()
            FetchSettings(
                fetching = state.fetching,
                shouldDownloadOnWifiOnly = state.shouldDownloadOnWifiOnly,
                lastFetchTime = state.lastFetchTime,
                removeCompletedAfter = state.removeCompletedAfter,
                removeUnfinishedAfter = state.removeUnfinishedAfter,
                onFetchRequested = onFetchRequested,
                toggleShouldDownloadOnWifiOnly = toggleShouldDownloadOnWifiOnly,
                onRemoveCompletedAfterSelected = onRemoveCompletedAfterSelected,
                onRemoveUnfinishedAfterSelected = onRemoveUnfinishedAfterSelected,
            )
            ColoredHorizontalDivider()
            General(
                showWidgetNewBadge = state.showWidgetNewBadge,
                onBackupRestoreClicked = onBackupRestoreClicked,
                onVersionClicked = onVersionClicked,
                onAddWidgetClicked = onAddWidgetClicked,
            )
            Spacer(
                modifier =
                    Modifier
                        .height(128.dp)
                        .isVisible {
                            // Marking it seen here because even items behind player and bottom nav bar are considered
                            // visible. So if spacer (which is at the bottom of screen) is seen, widget item definitely was
                            // seen.
                            markWidgetItemSeen = it
                        },
            )
        }
    }
}

@Composable
private fun PlaybackSettings(
    autoPlayNextInQueue: Boolean,
    toggleAutoPlayNextInQueue: () -> Unit,
) {
    Section(title = stringResource(id = R.string.settings_playback)) {
        TitleSwitchRow(
            title = stringResource(id = R.string.settings_auto_play_next_in_queue),
            switchChecked = autoPlayNextInQueue,
            onClick = toggleAutoPlayNextInQueue,
        )
    }
}

@Composable
private fun FetchSettings(
    fetching: Boolean,
    shouldDownloadOnWifiOnly: Boolean,
    lastFetchTime: Instant,
    removeCompletedAfter: RemoveDownloadsAfter,
    removeUnfinishedAfter: RemoveDownloadsAfter,
    toggleShouldDownloadOnWifiOnly: () -> Unit,
    onFetchRequested: () -> Unit,
    onRemoveCompletedAfterSelected: (RemoveDownloadsAfter) -> Unit,
    onRemoveUnfinishedAfterSelected: (RemoveDownloadsAfter) -> Unit,
) {
    var showRemoveCompletedDialog by remember { mutableStateOf(false) }
    var showRemoveUnfinishedDialog by remember { mutableStateOf(false) }

    Section(title = stringResource(id = R.string.settings_fetch)) {
        // Download now
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
        TitleProgressBarRow(
            title = stringResource(id = R.string.settings_fetch_now),
            showProgress = fetching,
            subtitle = text,
            onClick = onFetchRequested,
        )

        // Remove completed episodes
        TitleSubtitleRow(
            title = stringResource(id = R.string.settings_remove_completed),
            subtitle = removeCompletedAfter.text(),
            onClick = { showRemoveCompletedDialog = true },
        )

        // Remove unfinished episodes
        TitleSubtitleRow(
            title = stringResource(id = R.string.settings_remove_unfinished),
            subtitle = removeUnfinishedAfter.text(),
            onClick = { showRemoveUnfinishedDialog = true },
        )

        // Download on Wifi only
        TitleSwitchRow(
            title = stringResource(id = R.string.settings_should_download_on_wifi_only),
            switchChecked = shouldDownloadOnWifiOnly,
            onClick = toggleShouldDownloadOnWifiOnly,
        )
    }

    if (showRemoveCompletedDialog) {
        RemoveDownloadsAfterDialog(
            title = stringResource(id = R.string.settings_remove_completed),
            selectedOption = removeCompletedAfter,
            selectableOptions =
                listOf(
                    RemoveDownloadsAfter.TWENTY_FOUR_HOURS,
                    RemoveDownloadsAfter.SEVEN_DAYS,
                    RemoveDownloadsAfter.THIRTY_DAYS,
                    RemoveDownloadsAfter.NINETY_DAYS,
                ),
            onOptionSelected = onRemoveCompletedAfterSelected,
            onDismiss = { showRemoveCompletedDialog = false },
        )
    }

    if (showRemoveUnfinishedDialog) {
        RemoveDownloadsAfterDialog(
            title = stringResource(id = R.string.settings_remove_unfinished),
            selectedOption = removeUnfinishedAfter,
            selectableOptions =
                listOf(
                    RemoveDownloadsAfter.THIRTY_DAYS,
                    RemoveDownloadsAfter.NINETY_DAYS,
                ),
            onOptionSelected = onRemoveUnfinishedAfterSelected,
            onDismiss = { showRemoveUnfinishedDialog = false },
        )
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
private fun General(
    showWidgetNewBadge: Boolean,
    onBackupRestoreClicked: () -> Unit,
    onVersionClicked: () -> Unit,
    onAddWidgetClicked: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val appVersion = context.packageManager.getPackageInfo(context.packageName, 0)?.versionName ?: ""
    Section(title = stringResource(id = R.string.settings_general)) {
        // Widget
        TitleSubtitleRow(
            title = stringResource(id = R.string.settings_widget_title),
            subtitle = stringResource(id = R.string.settings_widget_subtitle),
            hasNewBadge = showWidgetNewBadge,
            onClick = onAddWidgetClicked,
        )
        // Backup and restore
        TitleSubtitleRow(
            title = stringResource(id = R.string.settings_backup_and_restore),
            onClick = onBackupRestoreClicked,
        )
        // Version
        TitleSubtitleRow(
            title = stringResource(id = R.string.settings_version),
            subtitle = appVersion,
            onClick = onVersionClicked,
        )

        // Report bug
        TitleIconRow(
            title = stringResource(id = R.string.settings_report_bug),
            icon = Icons.AutoMirrored.Filled.OpenInNew,
            onClick = { uriHandler.openUri(AppConstants.GITHUB_LINK) },
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        CategoryTitle(text = title)
        content()
    }
}

@Composable
private fun TitleSubtitleRow(
    title: String,
    subtitle: String = "",
    hasNewBadge: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Title(text = title)
            AnimatedVisibility(hasNewBadge) {
                NewBadge()
            }
        }
        if (subtitle.isNotEmpty()) {
            Subtitle(text = subtitle)
        }
    }
}

@Composable
private fun TitleIconRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Title(text = title)
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}

@Composable
private fun TitleSwitchRow(
    title: String,
    switchChecked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Title(text = title)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = switchChecked,
            onCheckedChange = null,
            thumbContent =
                if (switchChecked) {
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

@Composable
private fun TitleProgressBarRow(
    title: String,
    showProgress: Boolean,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !showProgress,
                    onClick = onClick,
                )
                .padding(16.dp),
    ) {
        Title(text = title)
        if (showProgress) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Subtitle(text = subtitle)
        }
    }
}

@Composable
private fun RemoveDownloadsAfterDialog(
    title: String,
    selectedOption: RemoveDownloadsAfter,
    selectableOptions: List<RemoveDownloadsAfter>,
    onOptionSelected: (RemoveDownloadsAfter) -> Unit,
    onDismiss: () -> Unit,
) {
    var selection by remember(selectedOption) { mutableStateOf(selectedOption) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.selectableGroup()) {
                    selectableOptions.forEach {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (selection == it),
                                    onClick = { selection = it },
                                    role = Role.RadioButton,
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (selection == it),
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = it.text(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            onOptionSelected(selection)
                            onDismiss()
                        },
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewBadge() {
    Row {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.new_badge),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(vertical = 2.dp, horizontal = 8.dp),
        )
    }
}

@Composable
private fun RemoveDownloadsAfter.text(): String {
    return when (this) {
        RemoveDownloadsAfter.TWENTY_FOUR_HOURS -> stringResource(id = R.string.settings_remove_after_24_hours)
        RemoveDownloadsAfter.SEVEN_DAYS -> stringResource(id = R.string.settings_remove_after_7_days)
        RemoveDownloadsAfter.THIRTY_DAYS -> stringResource(id = R.string.settings_remove_after_30_days)
        RemoveDownloadsAfter.NINETY_DAYS -> stringResource(id = R.string.settings_remove_after_90_days)
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
            onRemoveCompletedAfterSelected = { },
            onRemoveUnfinishedAfterSelected = { },
            onVersionClicked = { },
            onBackupRestoreClicked = { },
            toggleShouldDownloadOnWifiOnly = { },
            onAddWidgetClicked = { },
            onWidgetItemSeen = { },
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
            onRemoveCompletedAfterSelected = { },
            onRemoveUnfinishedAfterSelected = { },
            onVersionClicked = { },
            onBackupRestoreClicked = { },
            toggleShouldDownloadOnWifiOnly = { },
            onAddWidgetClicked = { },
            onWidgetItemSeen = { },
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
            onRemoveCompletedAfterSelected = { },
            onRemoveUnfinishedAfterSelected = { },
            onVersionClicked = { },
            onBackupRestoreClicked = { },
            toggleShouldDownloadOnWifiOnly = { },
            onAddWidgetClicked = { },
            onWidgetItemSeen = { },
        )
    }
}

@ThemePreview
@Composable
private fun RemoveCompletedAfterDialogPreview() {
    PreviewTheme {
        RemoveDownloadsAfterDialog(
            title = "Remove completed episodes",
            selectedOption = RemoveDownloadsAfter.THIRTY_DAYS,
            selectableOptions =
                listOf(
                    RemoveDownloadsAfter.TWENTY_FOUR_HOURS,
                    RemoveDownloadsAfter.SEVEN_DAYS,
                    RemoveDownloadsAfter.THIRTY_DAYS,
                    RemoveDownloadsAfter.NINETY_DAYS,
                ),
            onOptionSelected = { },
            onDismiss = { },
        )
    }
}
