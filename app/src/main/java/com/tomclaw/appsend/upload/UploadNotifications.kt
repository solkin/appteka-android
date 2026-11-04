package com.tomclaw.appsend.upload

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.upload.createUploadActivityIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.GROUP_NOTIFICATIONS
import com.tomclaw.appsend.util.NotificationIconHolder
import com.tomclaw.appsend.util.crc32
import com.tomclaw.appsend.util.createApkIconURI
import com.tomclaw.appsend.util.getColor
import com.tomclaw.appsend.util.getLabel
import com.tomclaw.appsend.util.logDebug
import com.tomclaw.appsend.util.openDetailsPendingIntent
import com.tomclaw.imageloader.SimpleImageLoader.imageLoader
import com.tomclaw.imageloader.core.Handlers
import io.reactivex.rxjava3.core.Observable

interface UploadNotifications {

    fun createInitialNotification(label: String): Notification

    fun subscribe(
        id: String,
        pkg: UploadPackage,
        apk: UploadApk,
        info: UploadInfo,
        stop: () -> Unit,
        observable: Observable<UploadState>
    )

}

class UploadNotificationsImpl(
    private val context: Context,
    private val analytics: Analytics,
) : UploadNotifications {

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

    override fun createInitialNotification(label: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_UPLOADING)
            .setContentTitle(label)
            .setContentText(context.getString(R.string.waiting_for_upload))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setSilent(true)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .setColor(getColor(R.color.primary_color, context))
            .setGroup(GROUP_NOTIFICATIONS)
            .build()
    }

    override fun subscribe(
        id: String,
        pkg: UploadPackage,
        apk: UploadApk,
        info: UploadInfo,
        stop: () -> Unit,
        observable: Observable<UploadState>
    ) {
        val notificationId = id.crc32()

        val label = apk.packageInfo.getLabel(context.packageManager)

        val uploadingIntent = getOpenUploadIntent(notificationId, pkg, apk, info)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_UPLOADED)
            .setContentTitle(label)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setSilent(true)
            .setOngoing(true)
            .setColor(getColor(R.color.primary_color, context))
            .setContentIntent(uploadingIntent)
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

        val uri = createApkIconURI(apk.path)

        // The holder keeps updating the same builder, so one load is enough —
        // doing it per emission meant up to a hundred loads for one upload
        context.imageLoader().load(iconHolder, uri, handlers)

        // takeUntil completes the stream on a terminal status, so the subscription drops
        // itself even when the relay already cached that status before we subscribed
        observable.takeUntil { state ->
            state.status in TERMINAL_UPLOAD_STATUSES
        }.subscribe({ state ->
            when (state.status) {
                UploadStatus.AWAIT -> {
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.waiting_for_upload))
                        .setSmallIcon(android.R.drawable.stat_sys_upload)
                        .setProgress(100, 0, true)
                        .setOngoing(true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                }

                UploadStatus.ERROR -> {
                    notificationManager.cancel(UPLOAD_NOTIFICATION_ID)
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.upload_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(notificationId, notification)
                    stop()
                }

                UploadStatus.COMPLETED -> {
                    notificationManager.cancel(notificationId)
                    notificationManager.cancel(UPLOAD_NOTIFICATION_ID)
                    val uploadedIntent = state.result?.let {
                        context.openDetailsPendingIntent(notificationId, it.appId, label)
                    }
                    val uploadedNotificationBuilder =
                        NotificationCompat.Builder(context, CHANNEL_UPLOADED)
                            .setContentTitle(label)
                            .setContentText(context.getString(R.string.upload_done))
                            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                            .setGroup(GROUP_NOTIFICATIONS)
                            .setOngoing(false)
                            .setAutoCancel(true)
                            .setColor(getColor(R.color.primary_color, context))
                            .setContentIntent(uploadedIntent)
                    val uploadedIconHolder = NotificationIconHolder(
                        resources = context.resources,
                        notificationBuilder = uploadedNotificationBuilder
                    )
                    uri.run { context.imageLoader().load(uploadedIconHolder, uri, handlers) }
                    val notification = uploadedNotificationBuilder.build()
                    notificationManager.notify(notificationId, notification)
                    stop()
                }

                UploadStatus.IDLE -> {
                    notificationManager.cancel(notificationId)
                    notificationManager.cancel(UPLOAD_NOTIFICATION_ID)
                    stop()
                }

                UploadStatus.STARTED -> {
                    // This upload left the queue for the foreground notification,
                    // so its own "waiting" one has nothing left to say
                    notificationManager.cancel(notificationId)
                    val notification = notificationBuilder
                        .setContentText(context.getString(R.string.waiting_for_upload))
                        .setSmallIcon(android.R.drawable.stat_sys_upload)
                        .setProgress(100, 0, true)
                        .setOngoing(true)
                        .build()
                    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
                }

                else -> {
                    // Progress belongs to the foreground notification alone; the
                    // cancel that used to sit here fired on every single percent
                    val notification = notificationBuilder
                        .setContentText(
                            context.getString(
                                R.string.uploading_progress,
                                state.percent
                            )
                        )
                        .setProgress(100, state.percent, false)
                        .build()
                    notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
                }
            }
        }, { error ->
            // Without this the stream would tear down the subscriber and leave the
            // foreground service with nothing left to stop it
            logDebug("[upload notification] Error: $error")
            analytics.trackException(error, mapOf("reason" to "Upload status subscription error"))
            notificationManager.cancel(notificationId)
            notificationManager.cancel(UPLOAD_NOTIFICATION_ID)
            stop()
        })
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

    private fun getOpenUploadIntent(
        requestCode: Int,
        pkg: UploadPackage,
        apk: UploadApk,
        info: UploadInfo
    ): PendingIntent {
        return PendingIntent.getActivity(
            context, requestCode,
            createUploadActivityIntent(
                context = context,
                pkg = pkg,
                apk = apk,
                info = info
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
    }

}

const val UPLOAD_NOTIFICATION_ID = 2
const val CHANNEL_UPLOADING = "uploading_channel_id"
const val CHANNEL_UPLOADED = "uploaded_channel_id"
