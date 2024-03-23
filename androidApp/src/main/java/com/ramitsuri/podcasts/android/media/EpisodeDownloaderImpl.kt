package com.ramitsuri.podcasts.android.media

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import coil.imageLoader
import coil.request.ImageRequest
import com.ramitsuri.podcasts.android.utils.Constants
import com.ramitsuri.podcasts.download.EpisodeDownloader
import com.ramitsuri.podcasts.model.Episode

@OptIn(UnstableApi::class)
class EpisodeDownloaderImpl(private val appContext: Context) : EpisodeDownloader {
    override fun add(episode: Episode) {
        // Episode download
        val episodeRequest =
            DownloadRequest.Builder(
                episode.id,
                Uri.Builder()
                    .encodedPath(episode.enclosureUrl)
                    .build(),
            ).build()
        DownloadService.sendAddDownload(
            appContext,
            PodcastDownloadService::class.java,
            episodeRequest,
            false,
        )

        // Image download
        val imageRequest = ImageRequest.Builder(appContext)
            .data(episode.podcastImageUrl)
            .build()
        appContext.imageLoader.enqueue(imageRequest)
    }

    override fun remove(episode: Episode) {
        DownloadService.sendRemoveDownload(
            appContext,
            PodcastDownloadService::class.java,
            episode.id,
            false,
        )
    }

    override fun cancel(episode: Episode) {
        DownloadService.sendSetStopReason(
            appContext,
            PodcastDownloadService::class.java,
            episode.id,
            Constants.DOWNLOAD_CANCELED_REASON,
            false,
        )
    }
}
