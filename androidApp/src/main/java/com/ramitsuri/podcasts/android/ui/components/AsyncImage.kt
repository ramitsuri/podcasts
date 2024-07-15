package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Image(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds,
) {
    AsyncImage(
        model =
            ImageRequest.Builder(LocalContext.current)
                .setHeader("User-Agent", "Podcasts")
                .data(url)
                .crossfade(true)
                .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}
