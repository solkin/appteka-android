package com.tomclaw.appsend.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.tomclaw.appsend.R
import com.tomclaw.appsend.install.InstallRequest
import com.tomclaw.appsend.install.createInstallActivityIntent
import com.tomclaw.appsend.install.createInstallConfirmationIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.GROUP_NOTIFICATIONS
import com.tomclaw.appsend.util.NotificationIconHolder
import com.tomclaw.appsend.util.crc32
import com.tomclaw.appsend.util.getColor
import com.tomclaw.appsend.util.logDebug
import com.tomclaw.appsend.util.openDetailsPendingIntent
import com.tomclaw.imageloader.SimpleImageLoader.imageLoader
import com.tomclaw.imageloader.core.Handlers
import io.reactivex.rxjava3.core.Observable

interface DownloadNotifications {

    fun createInitialNotification(label: String): Notification

    fun subscribe(
        appId: String,
        label: String,
        icon: String?,
        observable: Observable<DownloadState>
    )

    fun createInstallProgress(label: String, percent: Int): Notification

    fun showInstallProgress(label: String, percent: Int)

    /** For when the install can't open its screen itself: the user is in none of ours. */
    fun showInstallReady(request: InstallRequest)

    fun showInstallConfirmation(key: String, label: String, confirmation: Intent)

    fun showInstallFailure(
        key: String,
        label: String,
        message: String,
        details: Intent,
    )

    fun cancelInstall(key: String)

}

class DownloadNotificationsImpl(
    private val context: Context,
    private val analytics: Analytics,
) : DownloadNotifications {

    private val notificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    init {
        with(context.resources) {
            createNotificationChannel(
                channelId = CHANNEL_DOWNLOADING,
                channelName = getString(R.string.downloading_channel_name),
                channelDescription = getString(R.string.downloading_channel_description)
            )
            createNotificationChannel(
                channelId = CHANNEL_INSTALL,
                channelName = getString(R.string.install_channel_name),
                channelDescription = getString(R.string.install_channel_description)
            )
        }
    }

    override fun createInitialNotification(label: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_DOWNLOADING)
            .setContentTitle(label)
            .setContentText(context.getString(R.string.waiting_for_download))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setSilent(true)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .setColor(getColor(R.color.primary_color, context))
            .setGroup(GROUP_NOTIFICATIONS)
            .build()
    }

    override fun subscribe(
        appId: String,
        label: String,
        icon: String?,
        observable: Observable<DownloadState>
    ) {
        val notificationId = appId.crc32()

        val openDetailsIntent = context.openDetailsPendingIntent(notificationId, appId, label)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(label)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setSilent(true)
            .setOngoing(true)
            .setColor(getColor(R.color.primary_color, context))
            .setContentIntent(openDetailsIntent)
            .setGroup(GROUP_NOTIFICATIONS)

        val iconHolder = NotificationIconHolder(context.resources, notificationBuilder)
        val handlers = Handlers<NotificationCompat.Builder>()
            .apply {
                successHandler { viewHolder, result ->
                    // Rendered at the holder size: some icons have no intrinsic size
                    val size = viewHolder.getSize()
                    viewHolder.get().setLargeIcon(
                        result.getDrawable().toBitmap(size.width, size.height)
                    )
                }
            }

        // The holder keeps updating the same builder, so one load is enough —
        // doing it per emission meant up to a hundred loads for one download
        icon?.let { context.imageLoader().load(iconHolder, it, handlers) }

        // takeUntil completes the stream on a terminal status, so the subscription drops
        // itself even when the relay already cached that status before we subscribed
        observable.takeUntil { state ->
            state == DownloadState.Idle || state == DownloadState.Failed || state is DownloadState.Completed
        }.subscribe({ state ->
            when (state) {
                DownloadState.Queued -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.waiting_for_download))
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setProgress(100, 0, true)
                        .setOngoing(true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }

                DownloadState.Failed -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.download_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }

                is DownloadState.Completed, DownloadState.Idle -> {
                    // Installing it is up to the delivery, which tells about it on its own
                    notificationManager.cancel(notificationId)
                }

                is DownloadState.Running -> {
                    val percent = state.percent
                    val notification = if (percent == null) {
                        // This download left the queue for the foreground notification,
                        // so its own "waiting" one has nothing left to say
                        notificationManager.cancel(notificationId)
                        notificationBuilder
                            .setContentText(context.getString(R.string.waiting_for_download))
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setProgress(100, 0, true)
                            .setOngoing(true)
                            .build()
                    } else {
                        // Progress belongs to the foreground notification alone; the
                        // cancel that used to sit here fired on every single percent
                        notificationBuilder
                            .setContentText(context.getString(R.string.downloading_progress, percent))
                            .setProgress(100, percent, false)
                            .build()
                    }
                    notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification)
                }
            }
        }, { error ->
            logDebug("[download notification] Error: $error")
            analytics.trackException(error, mapOf("reason" to "Download status subscription error"))
            notificationManager.cancel(notificationId)
        })
    }

    override fun createInstallProgress(label: String, percent: Int): Notification {
        return installNotificationBuilder(label)
            .setContentText(context.getString(R.string.installing_progress, percent))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setSilent(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setProgress(100, percent, false)
            .build()
    }

    override fun showInstallProgress(label: String, percent: Int) {
        notificationManager.notify(INSTALL_NOTIFICATION_ID, createInstallProgress(label, percent))
    }

    override fun showInstallReady(request: InstallRequest) {
        val notification = installNotificationBuilder(request.label)
            .setContentText(context.getString(R.string.tap_to_install))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(getInstallIntent(request.key.crc32(), request))
            .build()
        notificationManager.notify(request.key.crc32(), notification)
    }

    override fun showInstallConfirmation(key: String, label: String, confirmation: Intent) {
        val intent = PendingIntent.getActivity(
            context,
            key.crc32(),
            createInstallConfirmationIntent(context, key, label, confirmation),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
        val notification = installNotificationBuilder(label)
            .setContentText(context.getString(R.string.tap_to_install))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(intent)
            .build()
        notificationManager.notify(key.crc32(), notification)
    }

    override fun showInstallFailure(
        key: String,
        label: String,
        message: String,
        details: Intent,
    ) {
        val intent = PendingIntent.getActivity(
            context,
            key.crc32(),
            details,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
        val notification = installNotificationBuilder(label)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentIntent(intent)
            .build()
        notificationManager.notify(key.crc32(), notification)
    }

    override fun cancelInstall(key: String) {
        notificationManager.cancel(key.crc32())
    }

    private fun installNotificationBuilder(label: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(label)
            .setGroup(GROUP_NOTIFICATIONS)
            .setOngoing(false)
            .setAutoCancel(true)
            .setColor(getColor(R.color.primary_color, context))
    }

    private fun getInstallIntent(requestCode: Int, request: InstallRequest): PendingIntent {
        return PendingIntent.getActivity(
            context, requestCode,
            createInstallActivityIntent(context, request),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(channelId, channelName, importance)
        mChannel.description = channelDescription
        notificationManager.createNotificationChannel(mChannel)
    }

}

const val DOWNLOAD_NOTIFICATION_ID = 1
const val INSTALL_NOTIFICATION_ID = 3
const val CHANNEL_DOWNLOADING = "downloading_channel_id"
const val CHANNEL_INSTALL = "install_channel_id"
