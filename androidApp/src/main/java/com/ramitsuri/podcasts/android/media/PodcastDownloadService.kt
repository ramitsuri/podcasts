package com.ramitsuri.podcasts.android.media

import android.app.Notification
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.utils.Constants
import com.ramitsuri.podcasts.android.utils.NotificationChannel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@UnstableApi
class PodcastDownloadService :
    DownloadService(
        Constants.DOWNLOADER_FOREGROUND_NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        NotificationChannel.Download.id,
        NotificationChannel.Download.nameRes,
        NotificationChannel.Download.descriptionRes,
    ),
    KoinComponent {
    private val downloadMgr by inject<DownloadManager>()
    private val downloadManagerListener by inject<DownloadManagerListener>()

    override fun getDownloadManager(): DownloadManager {
        return downloadMgr
    }

    override fun getScheduler(): Scheduler {
        return WorkManagerScheduler(this, DOWNLOAD_WORK_NAME)
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification {
        return DownloadNotificationHelper(this, NotificationChannel.Download.id)
            .buildProgressNotification(
                this,
                R.drawable.media3_notification_small_icon,
                null,
                getString(R.string.download_notification_text),
                downloads,
                notMetRequirements,
            )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        downloadMgr.addListener(downloadManagerListener)
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        private const val DOWNLOAD_WORK_NAME = "podcast-downloader"
    }
}
