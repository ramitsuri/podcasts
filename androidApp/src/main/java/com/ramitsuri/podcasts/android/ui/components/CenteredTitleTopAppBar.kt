package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.ramitsuri.podcasts.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredTitleTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onSettingsClicked: () -> Unit,
) {
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
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(id = R.string.settings),
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
