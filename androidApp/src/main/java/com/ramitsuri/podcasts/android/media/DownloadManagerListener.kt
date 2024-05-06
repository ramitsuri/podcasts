package com.ramitsuri.podcasts.android.media

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.ramitsuri.podcasts.android.utils.Constants
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
            episodesRepository.updateDownloadedAt(episodeId, null)
            episodesRepository.updateNeedsDownload(episodeId, false)
        }
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?,
    ) {
        val (state, needsDownload) =
            when (download.state) {
                Download.STATE_QUEUED -> Pair(DownloadStatus.QUEUED, null)
                Download.STATE_DOWNLOADING -> Pair(DownloadStatus.DOWNLOADING, null)
                Download.STATE_COMPLETED -> Pair(DownloadStatus.DOWNLOADED, false)
                Download.STATE_STOPPED -> {
                    if (download.stopReason == Constants.DOWNLOAD_CANCELED_REASON) {
                        Pair(DownloadStatus.NOT_DOWNLOADED, false)
                    } else {
                        Pair(DownloadStatus.PAUSED, null)
                    }
                }

                Download.STATE_FAILED -> {
                    // TODO log the final exception
                    Pair(DownloadStatus.NOT_DOWNLOADED, null)
                }

                else -> Pair(null, null)
            }

        longLivingScope.launch {
            val episodeId = download.request.id
            if (state != null) {
                episodesRepository.updateDownloadStatus(episodeId, state)
                if (state == DownloadStatus.DOWNLOADED) {
                    episodesRepository.updateDownloadedAt(episodeId)
                    episodesRepository.updateDownloadProgress(episodeId, 1.0)
                } else {
                    episodesRepository.updateDownloadProgress(
                        episodeId,
                        download.percentDownloaded.div(100.0).coerceIn(0.0, 1.0),
                    )
                }
            }
            if (needsDownload != null) {
                episodesRepository.updateNeedsDownload(episodeId, needsDownload)
            }
        }
    }
}
