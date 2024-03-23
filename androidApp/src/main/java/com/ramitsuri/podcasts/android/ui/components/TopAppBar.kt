package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview

@Composable
fun TopAppBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.back),
            )
        }
        if (label.isNotEmpty()) {
            Text(text = label)
        }
    }
}

@ThemePreview
@Composable
private fun TopAppBarPreview_WithTitle() {
    PreviewTheme {
        TopAppBar(onBack = {}, label = "Queue")
    }
}
