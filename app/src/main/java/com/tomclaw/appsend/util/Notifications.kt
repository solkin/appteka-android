package com.tomclaw.appsend.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tomclaw.appsend.BuildConfig
import com.tomclaw.appsend.R

interface Notifications {

    fun showDownloadingNotification(
        notificationId: Int,
        title: String,
        text: String,
        progress: Int,
        indeterminate: Boolean,
        icon: Bitmap?
    )

    fun showInstallNotification(
        notificationId: Int,
        title: String,
        text: String,
        icon: Bitmap?
    )

    fun showErrorNotification(
        notificationId: Int,
        title: String,
        text: String,
        icon: Bitmap?
    )

    fun hideNotification(notificationId: Int)

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

    override fun showDownloadingNotification(
        notificationId: Int,
        title: String,
        text: String,
        progress: Int,
        indeterminate: Boolean,
        icon: Bitmap?
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(100, progress, indeterminate)
            .setSmallIcon(R.drawable.ic_pill)
            .setLargeIcon(icon)
            .setGroup(NOTIFICATION_GROUP)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    override fun showInstallNotification(
        notificationId: Int,
        title: String,
        text: String,
        icon: Bitmap?
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_pill)
            .setLargeIcon(icon)
            .setGroup(NOTIFICATION_GROUP)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    override fun showErrorNotification(
        notificationId: Int,
        title: String,
        text: String,
        icon: Bitmap?
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_INSTALL)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_pill)
            .setLargeIcon(icon)
            .setGroup(NOTIFICATION_GROUP)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    override fun hideNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
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

const val NOTIFICATION_GROUP = BuildConfig.APPLICATION_ID + ".NOTIFICATIONS"
const val CHANNEL_DOWNLOADING = "downloading_channel_id"
const val CHANNEL_INSTALL = "install_channel_id"
