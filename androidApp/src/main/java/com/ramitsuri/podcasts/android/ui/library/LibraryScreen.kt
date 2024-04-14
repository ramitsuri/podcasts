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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ramitsuri.podcasts.android.ui.components.CenteredTitleTopAppBar
import com.ramitsuri.podcasts.android.ui.components.ColoredHorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    onSettingsClicked: () -> Unit,
    onSubscriptionsClicked: () -> Unit,
    onQueueClicked: () -> Unit,
    onDownloadsClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onFavoritesClicked: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        CenteredTitleTopAppBar(
            onSettingsClicked = onSettingsClicked,
        )
        ColoredHorizontalDivider()
        Item(
            icon = Icons.Outlined.Subscriptions,
            labelResId = R.string.subscriptions,
            onClick = onSubscriptionsClicked,
        )
        ColoredHorizontalDivider()
        Item(
            icon = Icons.AutoMirrored.Default.PlaylistAdd,
            labelResId = R.string.library_queue,
            onClick = onQueueClicked,
        )
        ColoredHorizontalDivider()
        Item(
            icon = Icons.Outlined.ArrowCircleDown,
            labelResId = R.string.library_downloads,
            onClick = onDownloadsClicked,
        )
        ColoredHorizontalDivider()
        Item(
            icon = Icons.Filled.FavoriteBorder,
            labelResId = R.string.library_favorites,
            onClick = onFavoritesClicked,
        )
        ColoredHorizontalDivider()
        Item(
            icon = Icons.Default.History,
            labelResId = R.string.library_history,
            onClick = onHistoryClicked,
        )
        ColoredHorizontalDivider()
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
            onSettingsClicked = { },
            onSubscriptionsClicked = { },
            onQueueClicked = { },
            onDownloadsClicked = { },
            onHistoryClicked = { },
            onFavoritesClicked = { },
        )
    }
}
