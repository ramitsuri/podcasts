package com.ramitsuri.podcasts.utils

import android.content.Context
import android.net.Uri
import coil.request.ImageRequest

fun Context.imageRequest(url: String) =
    ImageRequest.Builder(this)
        .data(url)
        .setHeader("User-Agent", "Podcasts")

fun Context.imageRequest(url: Uri) =
    ImageRequest.Builder(this)
        .data(url)
        .setHeader("User-Agent", "Podcasts")
