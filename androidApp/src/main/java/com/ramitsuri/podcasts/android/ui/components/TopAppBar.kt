package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    onBack: (() -> Unit)?,
    label: String = "",
    menuItems: List<AppBarMenuItem> = listOf(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
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
        actions = {
            if (menuItems.isNotEmpty()) {
                Menu(
                    showMenu = showMenu,
                    onToggleMenu = { showMenu = !showMenu },
                    menuItems = menuItems,
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun Menu(
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    menuItems: List<AppBarMenuItem>,
) {
    Box {
        IconButton(onClick = { onToggleMenu() }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                modifier =
                Modifier
                    .size(24.dp),
                contentDescription = stringResource(id = R.string.menu),
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onToggleMenu,
        ) {
            menuItems.forEach { appBarMenuItem ->
                MenuItem(
                    icon = appBarMenuItem.icon,
                    text = appBarMenuItem.title,
                    onClick = {
                        onToggleMenu()
                        appBarMenuItem.onClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
        },
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithTitle() {
    PreviewTheme {
        TopAppBar(onBack = {}, label = "Queue")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithTitle_WithMenu() {
    PreviewTheme {
        TopAppBar(
            onBack = {},
            label = "Queue",
            menuItems =
            listOf(
                AppBarMenuItem(
                    title = "Settings",
                    icon = Icons.Filled.Settings,
                    onClick = { },
                ),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithoutTitle_WithMenu() {
    PreviewTheme {
        TopAppBar(
            onBack = {},
            menuItems =
            listOf(
                AppBarMenuItem(
                    title = "Settings",
                    icon = Icons.Filled.Settings,
                    onClick = { },
                ),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreview
@Composable
private fun TopAppBarPreview_WithoutBack_WithoutTitle_WithMenu() {
    PreviewTheme {
        TopAppBar(
            onBack = null,
            menuItems =
            listOf(
                AppBarMenuItem(
                    title = "Settings",
                    icon = Icons.Filled.Settings,
                    onClick = { },
                ),
            ),
        )
    }
}
