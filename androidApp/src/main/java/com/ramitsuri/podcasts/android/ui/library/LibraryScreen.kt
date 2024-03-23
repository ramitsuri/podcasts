package com.ramitsuri.podcasts.android.ui.library

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    onSubscriptionsClicked: () -> Unit,
    onQueueClicked: () -> Unit,
    onDownloadsClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Item(
            icon = Icons.Outlined.Subscriptions,
            labelResId = R.string.library_subscriptions,
            onClick = onSubscriptionsClicked,
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Item(
            icon = Icons.AutoMirrored.Default.PlaylistAdd,
            labelResId = R.string.library_queue,
            onClick = onQueueClicked,
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Item(
            icon = Icons.Outlined.ArrowCircleDown,
            labelResId = R.string.library_downloads,
            onClick = onDownloadsClicked,
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Item(
            icon = Icons.Default.History,
            labelResId = R.string.library_history,
            onClick = onHistoryClicked,
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Item(
    icon: ImageVector,
    @StringRes labelResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable(role = Role.Button, onClick = onClick)
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = stringResource(id = labelResId),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
        )
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
    }
}

@ThemePreview
@Composable
private fun LibraryScreenPreview() {
    PreviewTheme {
        LibraryScreen(
            onSubscriptionsClicked = { },
            onQueueClicked = { },
            onDownloadsClicked = { },
            onHistoryClicked = { },
        )
    }
}
