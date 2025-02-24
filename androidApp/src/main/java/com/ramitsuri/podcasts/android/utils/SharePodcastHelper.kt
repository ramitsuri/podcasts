package com.ramitsuri.podcasts.android.utils

import android.content.Context
import android.content.Intent

fun Context.sharePodcast(shareText: String) {
    Intent(Intent.ACTION_SEND)
        .apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }.let { intent ->
            startActivity(Intent.createChooser(intent, null))
        }
}
