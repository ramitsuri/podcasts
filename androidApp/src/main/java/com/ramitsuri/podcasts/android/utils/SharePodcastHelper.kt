package com.ramitsuri.podcasts.android.utils

import android.content.Context
import android.content.Intent
import com.ramitsuri.podcasts.model.SharePodcastInfo

fun Context.sharePodcast(info: SharePodcastInfo) {
    Intent(Intent.ACTION_SEND)
        .apply {
            putExtra(Intent.EXTRA_TEXT, info.allValues)
            type = "text/plain"
        }.let { intent ->
            startActivity(Intent.createChooser(intent, null))
        }
}

fun Context.sharePodcastWithNotificationJournal(info: SharePodcastInfo) {
    Intent("com.ramitsuri.notificationjournal.intent.SHARE")
        .apply {
            putExtra("com.ramitsuri.notificationjournal.intent.TEXT", info.podcastAndEpisode)
            putExtra("com.ramitsuri.notificationjournal.intent.TAG", "Listened")
            type = "text/plain"
        }.let { intent ->
            startActivity(intent)
        }
}
