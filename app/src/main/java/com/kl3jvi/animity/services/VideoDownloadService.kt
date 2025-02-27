package com.kl3jvi.animity.services

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.navigation.NavDeepLinkBuilder
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.kl3jvi.animity.R
import com.kl3jvi.animity.application.AnimityApplication
import com.kl3jvi.animity.application.AppContainer
import com.kl3jvi.animity.utils.Constants.Companion.DOWNLOAD_CHANNEL_DESCRIPT
import com.kl3jvi.animity.utils.Constants.Companion.DOWNLOAD_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class VideoDownloadService :
    DownloadService(
        1,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        DOWNLOAD_CHANNEL_ID,
        R.string.app_name,
        R.string.title_home
    ) {
    private lateinit var notificationHelper: DownloadNotificationHelper
    private lateinit var context: Context

    private lateinit var manager: DownloadManager

    @Inject
    lateinit var exoDatabaseProvider: ExoDatabaseProvider

    override fun onCreate() {
        super.onCreate()
        context = this
        notificationHelper = DownloadNotificationHelper(this, DOWNLOAD_CHANNEL_ID)
    }


    override fun getDownloadManager(): DownloadManager {
        exoDatabaseProvider = ExoDatabaseProvider(applicationContext)
        val appContainer = (applicationContext as AnimityApplication).appContainer
        manager = appContainer.downloadManager
        manager.maxParallelDownloads = 3
        manager.addListener(object : DownloadManager.Listener {
            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }

            override fun onDownloadsPausedChanged(
                downloadManager: DownloadManager,
                downloadsPaused: Boolean
            ) {
                if (downloadsPaused) {
                    Toast.makeText(this@VideoDownloadService, "paused", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@VideoDownloadService, "resumed", Toast.LENGTH_SHORT).show()
                }

            }
        })
        return manager
    }

    override fun getScheduler(): Scheduler? {
        return null
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {

        val contentIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_downloads)
            .setArguments(null)
            .createPendingIntent()

        return notificationHelper.buildProgressNotification(
            this@VideoDownloadService,
            R.drawable.ic_downloading,
            contentIntent,
            DOWNLOAD_CHANNEL_DESCRIPT,
            downloads,
        )
    }
}