package com.ramitsuri.podcasts.android.ui.components

import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.model.Episode

fun episode(
    downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    downloadProgress: Double = 0.0,
    queuePosition: Int = Episode.NOT_IN_QUEUE,
) = Episode(
    id = "",
    podcastId = 0,
    podcastName = "Stuff You Should Know",
    podcastAuthor = "iHeartPodcasts",
    title = "Selects: The Case of Sacco and Vanzetti",
    description =
        "The trial of Sacco and Vanzetti, two anarchists accused of murder, was one of the " +
            "first \"crimes of the century.\" But did they do it? To this day there is speculation that " +
            "they did not. Learn all about this famous case in this classic episode. \n\n" +
            "See omnystudio.com/listener for privacy information.",
    link =
        "https://www.omnycontent.com/d/programs/e73c998e-6e60-432f-8610-ae210140c5b1/a91018a4-ea4f-4130-bf55-" +
            "ae270180c327/image.jpg?t=1684846432&size=Large",
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
