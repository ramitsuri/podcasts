package com.ramitsuri.podcasts.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri

fun Context.getEpisodeDeepLinkIntent(navDeepLink: String): PendingIntent? {
    return TaskStackBuilder.create(this).run {
        val mainIntent = Intent(Intent.ACTION_VIEW, navDeepLink.toUri())
        addNextIntentWithParentStack(mainIntent)
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        getPendingIntent(0, flags)
    }
}
