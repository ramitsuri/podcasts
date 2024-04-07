package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

data class AppBarMenuItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)
