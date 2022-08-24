package com.tomclaw.appsend.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tomclaw.appsend.BuildConfig
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

interface Notifications {

    fun startObservation(appId: String, label: String, observable: Observable<Int>)

}

class NotificationsImpl(
    private val context: Context
) : Notifications {

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

    override fun startObservation(appId: String, label: String, observable: Observable<Int>) {
        val notificationId = appId.hashCode() // TODO: replace with stable ID

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(label)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setLargeIcon(null)
            .setSilent(true)
            .setOngoing(true)
            .setGroup(GROUP_NOTIFICATIONS)

        var disposable: Disposable? = null
        disposable = observable.subscribe { status ->
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
                    val notification = NotificationCompat.Builder(context, CHANNEL_INSTALL)
                        .setContentTitle(label)
                        .setContentText(context.getString(R.string.tap_to_install))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setLargeIcon(null)
                        .setGroup(GROUP_NOTIFICATIONS)
                        .setOngoing(false)
                        .build()
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

const val GROUP_NOTIFICATIONS = BuildConfig.APPLICATION_ID + ".NOTIFICATIONS"
const val CHANNEL_DOWNLOADING = "downloading_channel_id"
const val CHANNEL_INSTALL = "install_channel_id"
