package com.ramitsuri.podcasts.utils

import android.content.Context
import androidx.core.net.toUri
import coil.request.ImageRequest

fun Context.imageRequest(url: String): ImageRequest.Builder {
    return url
        .replace("http://", "https://")
        .toUri()
        .let { uri ->
            ImageRequest.Builder(this)
                .data(uri)
                .setHeader("User-Agent", "Podcasts")
        }
}
