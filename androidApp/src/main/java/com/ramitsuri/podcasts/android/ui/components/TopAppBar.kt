package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    onBack: (() -> Unit)?,
    label: String = "",
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        colors =
            TopAppBarDefaults
                .topAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = {
            if (label.isNotEmpty()) {
                Text(
                    label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithTitle() {
    PreviewTheme {
        TopAppBar(
            onBack = {},
            label = "Queue",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithoutTitle() {
    PreviewTheme {
        TopAppBar(
            onBack = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithoutBack_WithoutTitle() {
    PreviewTheme {
        TopAppBar(
            onBack = null,
        )
    }
}
