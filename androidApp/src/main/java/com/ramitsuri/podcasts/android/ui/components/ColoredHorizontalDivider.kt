package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ColoredHorizontalDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
}
