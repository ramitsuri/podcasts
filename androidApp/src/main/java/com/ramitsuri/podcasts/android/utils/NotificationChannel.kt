package com.ramitsuri.podcasts.android.utils

import androidx.annotation.StringRes
import com.ramitsuri.podcasts.android.R

enum class NotificationChannel(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
) {
    Download(
        id = "download-service",
        nameRes = R.string.download_notification_channel_name,
        descriptionRes = R.string.download_notification_channel_description,
    ),
}
