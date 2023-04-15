package com.tomclaw.appsend.upload

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.tomclaw.appsend.BuildConfig
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.item.CommonItem
import com.tomclaw.appsend.util.NotificationIconHolder
import com.tomclaw.appsend.util.PackageIconLoader
import com.tomclaw.appsend.util.getColor
import com.tomclaw.imageloader.SimpleImageLoader.imageLoader
import com.tomclaw.imageloader.core.Handlers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

interface UploadNotifications {

    fun subscribe(
        id: String,
        item: CommonItem,
        start: (Int, Notification) -> Unit,
        stop: () -> Unit,
        observable: Observable<Int>
    )

}

class UploadNotificationsImpl(private val context: Context) : UploadNotifications {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        with(context.resources) {
            createNotificationChannel(
                channelId = CHANNEL_UPLOADING,
                channelName = getString(R.string.uploading_channel_name),
                channelDescription = getString(R.string.uploading_channel_description)
            )
            createNotificationChannel(
                channelId = CHANNEL_UPLOADED,
                channelName = getString(R.string.uploaded_channel_name),
                channelDescription = getString(R.string.uploaded_channel_description)
            )
        }
    }

    override fun subscribe(
        id: String,
        item: CommonItem,
        start: (Int, Notification) -> Unit,
        stop: () -> Unit,
        observable: Observable<Int>
    ) {
        val notificationId = id.hashCode() // TODO: replace with stable ID

//        val uploadingIntent = getOpenDetailsIntent(appId, label)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_UPLOADED)
            .setContentTitle(item.label)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setLargeIcon(null)
            .setSilent(true)
            .setOngoing(true)
            .setColor(getColor(R.color.primary_color, context))
//            .setContentIntent(uploadingIntent)
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
            val uri = PackageIconLoader.getUri(item.packageInfo)
            uri?.run { context.imageLoader().load(iconHolder, uri, handlers) }
            when (status) {
                AWAIT -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.waiting_for_upload))
                        .setSmallIcon(android.R.drawable.stat_sys_upload)
                        .setProgress(100, 0, true)
                        .setOngoing(true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }
                ERROR -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.upload_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                    stop()
                    disposable?.dispose()
                }
                COMPLETED -> {
                    notificationManager.cancel(notificationId)
//                    val uploadedIntent = getInstallIntent(file)
                    val uploadedNotificationBuilder =
                        NotificationCompat.Builder(context, CHANNEL_UPLOADED)
                            .setContentTitle(item.label)
                            .setContentText(context.getString(R.string.upload_done))
                            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                            .setGroup(GROUP_NOTIFICATIONS)
                            .setOngoing(false)
                            .setAutoCancel(true)
                            .setColor(getColor(R.color.primary_color, context))
//                            .setContentIntent(uploadedIntent)
                    val uploadedIconHolder = NotificationIconHolder(
                        resources = context.resources,
                        notificationBuilder = uploadedNotificationBuilder
                    )
                    uri?.run { context.imageLoader().load(uploadedIconHolder, uri, handlers) }
                    val notification = uploadedNotificationBuilder.build()
                    notificationManager.notify(notificationId, notification)
                    stop()
                    disposable?.dispose()
                }
                IDLE -> {
                    notificationManager.cancel(notificationId)
                    stop()
                    disposable?.dispose()
                }
                STARTED -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.waiting_for_upload))
                        .setSmallIcon(android.R.drawable.stat_sys_upload)
                        .setProgress(100, 0, true)
                        .setOngoing(true)
                        .build()
                    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
                    start(
                        UPLOAD_NOTIFICATION_ID,
                        notification
                    )
                }
                else -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.uploading_progress, status))
                        .setProgress(100, status, false)
                        .build()
                    notificationManager.cancel(notificationId)
                    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
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

const val UPLOAD_NOTIFICATION_ID = 2
const val GROUP_NOTIFICATIONS = BuildConfig.APPLICATION_ID + ".NOTIFICATIONS"
const val CHANNEL_UPLOADING = "uploading_channel_id"
const val CHANNEL_UPLOADED = "uploaded_channel_id"
