package com.ramitsuri.podcasts.android.media

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.scheduler.Requirements
import com.ramitsuri.podcasts.android.BuildConfig
import com.ramitsuri.podcasts.android.utils.Constants
import com.ramitsuri.podcasts.model.DownloadStatus
import com.ramitsuri.podcasts.repositories.EpisodesRepository
import com.ramitsuri.podcasts.utils.LogHelper
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
        LogHelper.d(TAG, "onDownloadRemoved: $download")
        longLivingScope.launch {
            val episodeId = download.request.id
            episodesRepository.updateDownloadRemoved(episodeId)
        }
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?,
    ) {
        val (state, needsDownload) =
            when (download.state) {
                Download.STATE_QUEUED -> {
                    LogHelper.d(TAG, "${download.request.id} queued")
                    Pair(DownloadStatus.QUEUED, null)
                }

                Download.STATE_DOWNLOADING -> {
                    LogHelper.d(TAG, "${download.request.id} downloading")
                    Pair(DownloadStatus.DOWNLOADING, null)
                }

                Download.STATE_COMPLETED -> {
                    LogHelper.d(TAG, "${download.request.id} downloaded")
                    Pair(DownloadStatus.DOWNLOADED, false)
                }

                Download.STATE_STOPPED -> {
                    if (download.stopReason == Constants.DOWNLOAD_CANCELED_REASON) {
                        LogHelper.d(TAG, "${download.request.id} cancelled")
                        Pair(DownloadStatus.NOT_DOWNLOADED, false)
                    } else {
                        LogHelper.d(TAG, "${download.request.id} stopped")
                        Pair(DownloadStatus.PAUSED, null)
                    }
                }

                Download.STATE_FAILED -> {
                    // TODO log the final exception
                    LogHelper.d(TAG, "${download.request.id} failed")
                    Pair(DownloadStatus.NOT_DOWNLOADED, null)
                }

                else -> {
                    when (download.state) {
                        Download.STATE_REMOVING -> {
                            LogHelper.d(TAG, "${download.request.id} removing")
                        }
                        Download.STATE_RESTARTING -> {
                            LogHelper.d(TAG, "${download.request.id} restarting")
                        }
                        else -> {
                            LogHelper.d(TAG, "${download.request.id} unknown download state")
                        }
                    }
                    Pair(null, null)
                }
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

    override fun onRequirementsStateChanged(
        downloadManager: DownloadManager,
        requirements: Requirements,
        notMetRequirements: Int,
    ) {
        super.onRequirementsStateChanged(downloadManager, requirements, notMetRequirements)
        if (BuildConfig.DEBUG) {
            val requirementsString =
                buildString {
                    append("IsIdleRequired: ${requirements.isIdleRequired}")
                    appendLine()
                    append("IsNetworkRequired: ${requirements.isNetworkRequired}")
                    appendLine()
                    append("IsUnmeteredNetworkRequired: ${requirements.isUnmeteredNetworkRequired}")
                    appendLine()
                    append("IsStorageNotLowRequiredRequired: ${requirements.isStorageNotLowRequired}")
                    appendLine()
                    append("IsChargingRequired: ${requirements.isChargingRequired}")
                }
            LogHelper.d(TAG, requirementsString)
        }
    }

    companion object {
        private const val TAG = "DownloadManagerListener"
    }
}
