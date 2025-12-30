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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@UnstableApi
class DownloadManagerListener(
    private val episodesRepository: EpisodesRepository,
    private val longLivingScope: CoroutineScope,
) : DownloadManager.Listener {
    private val downloadStatusJobs = DownloadStatusJobs()

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
        longLivingScope.launch {
            val episodeId = download.request.id
            val (status, needsDownload) = download.statusAndNeedsDownload
            if (status != null) {
                episodesRepository.updateDownloadStatus(episodeId, status)
            }
            if (needsDownload != null) {
                episodesRepository.updateNeedsDownload(episodeId, needsDownload)
            }
            if (status == DownloadStatus.DOWNLOADING) {
                val job =
                    launch {
                        while (true) {
                            downloadManager
                                .currentDownloads
                                .find { it.request.id == download.request.id }
                                ?.percentDownloaded
                                ?.toDouble()
                                ?.div(100)
                                ?.coerceIn(0.0, 1.0)
                                ?.let { progress ->
                                    episodesRepository.updateDownloadProgress(download.request.id, progress)
                                }
                            delay(1000)
                        }
                    }
                downloadStatusJobs.cancelAndAdd(download, job)
            } else if (status == DownloadStatus.DOWNLOADED) {
                episodesRepository.updateDownloadedAt(episodeId)
                episodesRepository.updateDownloadProgress(episodeId, 1.0)
                downloadStatusJobs.cancelAndRemove(download)
            } else {
                downloadStatusJobs.cancelAndRemove(download)
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

    private val Download.statusAndNeedsDownload: Pair<DownloadStatus?, Boolean?>
        get() =
            when (state) {
                Download.STATE_QUEUED -> {
                    LogHelper.d(TAG, "${request.id} queued")
                    Pair(DownloadStatus.QUEUED, null)
                }

                Download.STATE_DOWNLOADING -> {
                    LogHelper.d(TAG, "${request.id} downloading")
                    Pair(DownloadStatus.DOWNLOADING, null)
                }

                Download.STATE_COMPLETED -> {
                    LogHelper.d(TAG, "${request.id} downloaded")
                    Pair(DownloadStatus.DOWNLOADED, false)
                }

                Download.STATE_STOPPED -> {
                    if (stopReason == Constants.DOWNLOAD_CANCELED_REASON) {
                        LogHelper.d(TAG, "${request.id} cancelled")
                        Pair(DownloadStatus.NOT_DOWNLOADED, false)
                    } else {
                        LogHelper.d(TAG, "${request.id} stopped")
                        Pair(DownloadStatus.PAUSED, null)
                    }
                }

                Download.STATE_FAILED -> {
                    // TODO log the final exception
                    LogHelper.d(TAG, "${request.id} failed")
                    Pair(DownloadStatus.NOT_DOWNLOADED, null)
                }

                else -> {
                    when (state) {
                        Download.STATE_REMOVING -> {
                            LogHelper.d(TAG, "${request.id} removing")
                        }

                        Download.STATE_RESTARTING -> {
                            LogHelper.d(TAG, "${request.id} restarting")
                        }

                        else -> {
                            LogHelper.d(TAG, "${request.id} unknown download state")
                        }
                    }
                    Pair(null, null)
                }
            }

    class DownloadStatusJobs {
        private val downloadIdJobsMap = mutableMapOf<String, Job>()
        private val mutex = Mutex()

        suspend fun cancelAndAdd(
            download: Download,
            job: Job,
        ) {
            mutex.withLock {
                downloadIdJobsMap[download.request.id]?.cancel()
                downloadIdJobsMap[download.request.id] = job
            }
        }

        suspend fun cancelAndRemove(download: Download) {
            mutex.withLock {
                downloadIdJobsMap.remove(download.request.id)?.cancel()
            }
        }
    }

    companion object {
        private const val TAG = "DownloadManagerListener"
    }
}
