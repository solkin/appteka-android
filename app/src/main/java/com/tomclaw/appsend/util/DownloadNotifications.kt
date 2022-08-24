package com.tomclaw.appsend.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.tomclaw.appsend.BuildConfig
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.imageloader.SimpleImageLoader.imageLoader
import com.tomclaw.imageloader.core.Handlers
import com.tomclaw.imageloader.core.ViewHolder
import com.tomclaw.imageloader.core.ViewSize
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File

interface DownloadNotifications {

    fun subscribe(
        appId: String,
        label: String,
        icon: String?,
        file: File,
        observable: Observable<Int>
    )

}

class DownloadNotificationsImpl(
    private val context: Context
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

    override fun subscribe(
        appId: String,
        label: String,
        icon: String?,
        file: File,
        observable: Observable<Int>
    ) {
        val notificationId = appId.hashCode() // TODO: replace with stable ID

        val openDetailsIntent = getOpenDetailsIntent(appId, label)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(label)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setLargeIcon(null)
            .setSilent(true)
            .setOngoing(true)
            .setColor(context.resources.getColor(R.color.primary_color))
            .setContentIntent(openDetailsIntent)
            .setGroup(GROUP_NOTIFICATIONS)

        val iconHolder = NotificationIconHolder(context.resources, notificationBuilder)
        val handlers = Handlers<NotificationCompat.Builder>()
            .apply {
                successHandler { viewHolder, result ->
                    viewHolder.get().setLargeIcon(result.getDrawable().toBitmap())
                }
            }

        var disposable: Disposable? = null
        disposable = observable.subscribe { status ->
            icon?.run { context.imageLoader().load(iconHolder, icon, handlers) }
            when (status) {
                AWAIT -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.waiting_for_download))
                        .setProgress(100, 0, true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }
                ERROR -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.download_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }
                COMPLETED -> {
                    notificationManager.cancel(notificationId)
                    val installIntent = getInstallIntent(file)
                    val installNotificationBuilder =
                        NotificationCompat.Builder(context, CHANNEL_INSTALL)
                            .setContentTitle(label)
                            .setContentText(context.getString(R.string.tap_to_install))
                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setGroup(GROUP_NOTIFICATIONS)
                            .setOngoing(false)
                            .setColor(context.resources.getColor(R.color.primary_color))
                            .setContentIntent(installIntent)
                    val installIconHolder = NotificationIconHolder(
                        resources = context.resources,
                        notificationBuilder = installNotificationBuilder
                    )
                    icon?.run { context.imageLoader().load(installIconHolder, icon, handlers) }
                    val notification = installNotificationBuilder.build()
                    notificationManager.notify(notificationId, notification)
                }
                IDLE -> {
                    notificationManager.cancel(notificationId)
                    disposable?.dispose()
                }
                else -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.downloading_progress, status))
                        .setProgress(100, status, false)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getOpenDetailsIntent(appId: String, label: String): PendingIntent {
        return PendingIntent.getActivity(
            context, 0,
            createDetailsActivityIntent(
                context = context,
                appId = appId,
                packageName = null,
                label = label,
                moderation = false,
                finishOnly = false
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            FLAG_CANCEL_CURRENT
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getInstallIntent(file: File): PendingIntent {
        return PendingIntent.getActivity(
            context, 0,
            IntentHelper.openFileIntent(
                context,
                file.absolutePath,
                "application/vnd.android.package-archive"
            ),
            FLAG_CANCEL_CURRENT
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

    class NotificationIconHolder(
        resources: Resources,
        private val notificationBuilder: NotificationCompat.Builder
    ) : ViewHolder<NotificationCompat.Builder> {

        private val size = ViewSize(dpToPx(64, resources), dpToPx(64, resources))

        override var tag: Any?
            get() = notificationBuilder.extras.getString("tag")
            set(value) {
                notificationBuilder.extras.putString("tag", value.toString())
            }

        override fun get(): NotificationCompat.Builder {
            return notificationBuilder
        }

        override fun getSize(): ViewSize {
            return size
        }

        override fun optSize(): ViewSize {
            return size
        }
    }

}

const val GROUP_NOTIFICATIONS = BuildConfig.APPLICATION_ID + ".NOTIFICATIONS"
const val CHANNEL_DOWNLOADING = "downloading_channel_id"
const val CHANNEL_INSTALL = "install_channel_id"
