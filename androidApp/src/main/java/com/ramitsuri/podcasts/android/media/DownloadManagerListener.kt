package com.ramitsuri.podcasts.android.media

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@UnstableApi
class DownloadManagerListener(
    private val episodesRepository: EpisodesRepository,
    private val longLivingScope: CoroutineScope,
) : DownloadManager.Listener {
    override fun onDownloadRemoved(
        downloadManager: DownloadManager,
        download: Download,
    ) {
        longLivingScope.launch {
            val episodeId = download.request.id
            episodesRepository.updateDownloadBlocked(episodeId, true)
            episodesRepository.updateDownloadStatus(episodeId, DownloadStatus.NOT_DOWNLOADED)
            episodesRepository.updateDownloadProgress(episodeId, 0.0)
        }
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?,
    ) {
        val state = when (download.state) {
            Download.STATE_QUEUED -> DownloadStatus.QUEUED
            Download.STATE_DOWNLOADING -> DownloadStatus.DOWNLOADING
            Download.STATE_COMPLETED -> DownloadStatus.DOWNLOADED
            Download.STATE_STOPPED -> DownloadStatus.PAUSED
            Download.STATE_FAILED -> {
                // TODO log the final exception
                DownloadStatus.NOT_DOWNLOADED
            }

            else -> null
        }

        longLivingScope.launch {
            if (state != null) {
                val episodeId = download.request.id
                episodesRepository.updateDownloadStatus(episodeId, state)
                episodesRepository.updateDownloadProgress(
                    episodeId,
                    download.percentDownloaded.div(100.0).coerceIn(0.0, 1.0),
                )
            }
        }
    }
}
