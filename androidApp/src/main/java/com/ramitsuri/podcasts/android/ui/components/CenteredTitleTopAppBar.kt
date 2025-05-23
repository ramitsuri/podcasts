package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.android.ui.utils.LocalIsDarkTheme
import com.ramitsuri.podcasts.android.ui.utils.getColorFromImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredTitleTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    currentlyPlayingArtworkUrl: String? = null,
    settingsHasBadge: Boolean = false,
    onSettingsClicked: () -> Unit,
    onSearchClicked: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isDarkTheme = LocalIsDarkTheme.current
    val defaultColor = MaterialTheme.colorScheme.onBackground
    var colorFromArtwork by remember { mutableStateOf(defaultColor) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(currentlyPlayingArtworkUrl) {
        colorFromArtwork = currentlyPlayingArtworkUrl?.let { url ->
            getColorFromImage(context, url, isDarkTheme)
        } ?: defaultColor
    }
    CenterAlignedTopAppBar(
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedLogo(
                    size = 24.dp,
                    isAnimating = currentlyPlayingArtworkUrl != null,
                    color = colorFromArtwork,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(id = R.string.app_name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorFromArtwork,
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(id = R.string.menu),
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
    BottomSheetDialog(
        show = showMenu,
        onDismissRequest = { showMenu = false },
        content = {
            onSearchClicked?.let {
                BottomSheetDialogMenuItem(
                    startIcon = Icons.Filled.Search,
                    text = stringResource(id = R.string.search),
                    onClick = {
                        onSearchClicked()
                        showMenu = false
                    },
                )
            }
            BottomSheetDialogMenuItem(
                startIcon = Icons.Filled.Settings,
                hasBadge = settingsHasBadge,
                text = stringResource(id = R.string.settings),
                onClick = {
                    onSettingsClicked()
                    showMenu = false
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun Preview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CenteredTitleTopAppBar(
                settingsHasBadge = true,
                onSettingsClicked = {},
                onSearchClicked = {},
            )
        }
    }
}
