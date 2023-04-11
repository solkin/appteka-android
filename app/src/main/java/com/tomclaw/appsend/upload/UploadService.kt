package com.tomclaw.appsend.upload

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.download.DownloadService
import com.tomclaw.appsend.upload.di.UploadServiceModule
import com.tomclaw.appsend.util.getParcelableExtraCompat
import javax.inject.Inject

class UploadService : Service() {

    @Inject
    lateinit var uploadManager: UploadManager

    @Inject
    lateinit var notifications: UploadNotifications

    override fun onCreate() {
        super.onCreate()
        println("[upload service] onCreate")
        Appteka.getComponent()
            .uploadServiceComponent(UploadServiceModule(this))
            .inject(service = this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { onIntentReceived(it) }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onIntentReceived(intent: Intent): Boolean {
        val meta = intent.getParcelableExtraCompat(EXTRA_META, MetaInfo::class.java) ?: return false

        println("[upload service] onStartCommand(meta = $meta)")

        val relay = uploadManager.status(meta.file)

        uploadManager.upload(meta.file, meta)

        notifications.subscribe(
            id = meta.file,
            meta = meta,
            start = { notificationId, notification ->
                startForeground(notificationId, notification)
            },
            stop = {
                stopForeground()
            },
            observable = relay,
        )
        return true
    }

    @Suppress("DEPRECATION")
    private fun Service.stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onDestroy() {
        println("[upload service] onDestroy")
        stopForeground()
    }

    override fun onBind(intent: Intent): IBinder {
        println("[upload service] onBind")
        return Binder()
    }

}

fun createUploadIntent(
    context: Context,
    meta: MetaInfo
): Intent = Intent(context, UploadService::class.java)
    .putExtra(EXTRA_META, meta)

private const val EXTRA_META = "meta"
