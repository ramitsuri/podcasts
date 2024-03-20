package com.ramitsuri.podcasts.model

import com.ramitsuri.podcasts.GetEpisode
import com.ramitsuri.podcasts.GetEpisodesForPodcast
import com.ramitsuri.podcasts.GetEpisodesForPodcasts
import com.ramitsuri.podcasts.GetEpisodesInQueue
import com.ramitsuri.podcasts.network.model.EpisodeDto
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

data class Episode(
    val id: String,
    val podcastId: Long,
    val podcastName: String,
    val podcastAuthor: String,
    val podcastImageUrl: String,
    val title: String,
    val description: String,
    val link: String,
    val enclosureUrl: String,
    val datePublished: Long,
    val duration: Int?,
    val explicit: Boolean,
    val episode: Int?,
    val season: Int?,
    val progressInSeconds: Int,
    val downloadStatus: DownloadStatus,
    val downloadProgress: Double,
    val downloadBlocked: Boolean,
    val queuePosition: Int,
    val completedAt: Instant?,
) {

    val isCompleted = completedAt != null

    val friendlyDatePublished: String
        get() {
            val format =
                LocalDateTime.Format {
                    year()
                    char('-')
                    monthNumber()
                    char('-')
                    dayOfMonth()
                }
            return Instant
                .fromEpochSeconds(datePublished)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .format(format)
        }

    internal constructor(dto: EpisodeDto) : this(
        id = dto.id,
        podcastId = dto.podcastId,
        podcastName = "",
        podcastAuthor = "",
        podcastImageUrl = "",
        title = dto.title,
        description = dto.description,
        link = dto.link,
        enclosureUrl = dto.enclosureUrl,
        datePublished = dto.datePublished,
        duration = dto.duration,
        explicit = dto.explicit == 1,
        episode = dto.episode,
        season = dto.season,
        progressInSeconds = PLAY_PROGRESS_MIN,
        downloadStatus = DownloadStatus.NOT_DOWNLOADED,
        downloadProgress = 0.0,
        downloadBlocked = false,
        queuePosition = NOT_IN_QUEUE,
        completedAt = null,
    )

    internal constructor(getEpisode: GetEpisode) : this(
        id = getEpisode.id,
        podcastId = getEpisode.podcastId,
        podcastName = getEpisode.podcastTitle,
        podcastAuthor = getEpisode.podcastAuthor,
        podcastImageUrl = getEpisode.podcastImageUrl,
        title = getEpisode.title,
        description = getEpisode.description,
        link = getEpisode.link,
        enclosureUrl = getEpisode.enclosureUrl,
        datePublished = getEpisode.datePublished,
        duration = getEpisode.duration,
        explicit = getEpisode.explicit,
        episode = getEpisode.episode,
        season = getEpisode.season,
        progressInSeconds = getEpisode.playProgress,
        downloadStatus = getEpisode.downloadStatus,
        downloadProgress = getEpisode.downloadProgress,
        downloadBlocked = getEpisode.downloadBlocked,
        queuePosition = getEpisode.queuePosition,
        completedAt = getEpisode.completedAt,
    )

    internal constructor(getEpisodesForPodcast: GetEpisodesForPodcast) : this(
        id = getEpisodesForPodcast.id,
        podcastId = getEpisodesForPodcast.podcastId,
        podcastName = getEpisodesForPodcast.podcastTitle,
        podcastAuthor = getEpisodesForPodcast.podcastAuthor,
        podcastImageUrl = getEpisodesForPodcast.podcastImageUrl,
        title = getEpisodesForPodcast.title,
        description = getEpisodesForPodcast.description,
        link = getEpisodesForPodcast.link,
        enclosureUrl = getEpisodesForPodcast.enclosureUrl,
        datePublished = getEpisodesForPodcast.datePublished,
        duration = getEpisodesForPodcast.duration,
        explicit = getEpisodesForPodcast.explicit,
        episode = getEpisodesForPodcast.episode,
        season = getEpisodesForPodcast.season,
        progressInSeconds = getEpisodesForPodcast.playProgress,
        downloadStatus = getEpisodesForPodcast.downloadStatus,
        downloadProgress = getEpisodesForPodcast.downloadProgress,
        downloadBlocked = getEpisodesForPodcast.downloadBlocked,
        queuePosition = getEpisodesForPodcast.queuePosition,
        completedAt = getEpisodesForPodcast.completedAt,
    )

    internal constructor(getEpisodesForPodcasts: GetEpisodesForPodcasts) : this(
        id = getEpisodesForPodcasts.id,
        podcastId = getEpisodesForPodcasts.podcastId,
        podcastName = getEpisodesForPodcasts.podcastTitle,
        podcastAuthor = getEpisodesForPodcasts.podcastAuthor,
        podcastImageUrl = getEpisodesForPodcasts.podcastImageUrl,
        title = getEpisodesForPodcasts.title,
        description = getEpisodesForPodcasts.description,
        link = getEpisodesForPodcasts.link,
        enclosureUrl = getEpisodesForPodcasts.enclosureUrl,
        datePublished = getEpisodesForPodcasts.datePublished,
        duration = getEpisodesForPodcasts.duration,
        explicit = getEpisodesForPodcasts.explicit,
        episode = getEpisodesForPodcasts.episode,
        season = getEpisodesForPodcasts.season,
        progressInSeconds = getEpisodesForPodcasts.playProgress,
        downloadStatus = getEpisodesForPodcasts.downloadStatus,
        downloadProgress = getEpisodesForPodcasts.downloadProgress,
        downloadBlocked = getEpisodesForPodcasts.downloadBlocked,
        queuePosition = getEpisodesForPodcasts.queuePosition,
        completedAt = getEpisodesForPodcasts.completedAt,
    )

    internal constructor(getEpisodesInQueue: GetEpisodesInQueue) : this(
        id = getEpisodesInQueue.id,
        podcastId = getEpisodesInQueue.podcastId,
        podcastName = getEpisodesInQueue.podcastTitle,
        podcastAuthor = getEpisodesInQueue.podcastAuthor,
        podcastImageUrl = getEpisodesInQueue.podcastImageUrl,
        title = getEpisodesInQueue.title,
        description = getEpisodesInQueue.description,
        link = getEpisodesInQueue.link,
        enclosureUrl = getEpisodesInQueue.enclosureUrl,
        datePublished = getEpisodesInQueue.datePublished,
        duration = getEpisodesInQueue.duration,
        explicit = getEpisodesInQueue.explicit,
        episode = getEpisodesInQueue.episode,
        season = getEpisodesInQueue.season,
        progressInSeconds = getEpisodesInQueue.playProgress,
        downloadStatus = getEpisodesInQueue.downloadStatus,
        downloadProgress = getEpisodesInQueue.downloadProgress,
        downloadBlocked = getEpisodesInQueue.downloadBlocked,
        queuePosition = getEpisodesInQueue.queuePosition,
        completedAt = getEpisodesInQueue.completedAt,
    )

    companion object {
        const val NOT_IN_QUEUE = -1
        const val PLAY_PROGRESS_MIN = 0
        const val PLAY_PROGRESS_MAX = Int.MAX_VALUE
    }
}
