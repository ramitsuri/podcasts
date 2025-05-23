package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.ramitsuri.podcasts.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredTitleTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    settingsHasBadge: Boolean = false,
    onSettingsClicked: () -> Unit,
    onSearchClicked: (() -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = {
            Text(
                stringResource(id = R.string.app_name),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
