package com.ramitsuri.podcasts.model

enum class DownloadStatus(val value: String) {
    NOT_DOWNLOADED("not_downloaded"),
    QUEUED("queued"),
    DOWNLOADING("downloading"),
    PAUSED("paused"),
    DOWNLOADED("downloaded"),
    ;

    companion object {
        fun fromValue(value: String): DownloadStatus {
            return DownloadStatus.entries.firstOrNull { it.value == value } ?: NOT_DOWNLOADED
        }
    }
}
