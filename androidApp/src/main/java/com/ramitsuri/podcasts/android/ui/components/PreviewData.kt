package com.ramitsuri.podcasts.android.ui.components

import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode

fun episode(
    downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    downloadProgress: Double = 0.0,
    queuePosition: Int = Episode.NOT_IN_QUEUE,
) =
    Episode(
        id = "",
        podcastId = 0,
        podcastName = "Stuff You Should Know",
        podcastAuthor = "iHeartPodcasts",
        title = "Selects: The Case of Sacco and Vanzetti",
        description = "The trial of Sacco and Vanzetti, two anarchists accused of murder, was one of the " +
            "first \"crimes of the century.\" But did they do it? To this day there is speculation that " +
            "they did not. Learn all about this famous case in this classic episode. \n\n" +
            "See omnystudio.com/listener for privacy information.",
        link = "",
        enclosureUrl = "",
        datePublished = 1710609953,
        duration = 1 * 60 * 60,
        explicit = false,
        episode = null,
        season = null,
        progressInSeconds = 0,
        downloadStatus = downloadStatus,
        downloadProgress = downloadProgress,
        downloadBlocked = false,
        queuePosition = queuePosition,
        completedAt = null,
    )
