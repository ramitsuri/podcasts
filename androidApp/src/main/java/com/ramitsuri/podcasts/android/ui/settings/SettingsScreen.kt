package com.ramitsuri.podcasts.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.components.TopAppBar
import com.ramitsuri.podcasts.model.ui.SettingsViewState

@Composable
fun SettingsScreen(
    state: SettingsViewState,
    onBack: () -> Unit,
    toggleAutoPlayNextInQueue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        TopAppBar(onBack = onBack, label = stringResource(id = R.string.settings))
        PlaybackSettings(
            autoPlayNextInQueue = state.autoPlayNextInQueue,
            toggleAutoPlayNextInQueue = toggleAutoPlayNextInQueue,
        )
    }
}

@Composable
private fun PlaybackSettings(
    autoPlayNextInQueue: Boolean,
    toggleAutoPlayNextInQueue: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.settings_playback),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = toggleAutoPlayNextInQueue)
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(id = R.string.settings_auto_play_next_in_queue))
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

@ThemePreview
@Composable
private fun SettingsPreview() {
    PreviewTheme {
        SettingsScreen(
            state = SettingsViewState(autoPlayNextInQueue = true),
            onBack = { },
            toggleAutoPlayNextInQueue = { },
        )
    }
}
