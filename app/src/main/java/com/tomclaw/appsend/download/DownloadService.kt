package com.tomclaw.appsend.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.download.di.DownloadModule
import javax.inject.Inject

class DownloadService : Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var notifications: DownloadNotifications

    override fun onCreate() {
        super.onCreate()
        println("[service] onCreate")
        Appteka.getComponent()
            .downloadComponent(DownloadModule(this))
            .inject(service = this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val label = intent.getStringExtra(EXTRA_LABEL)
            ?: throw IllegalArgumentException("label must be provided")
        val version = intent.getStringExtra(EXTRA_VERSION)
            ?: throw IllegalArgumentException("version must be provided")
        val icon = intent.getStringExtra(EXTRA_ICON)
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("appId must be provided")
        val url = intent.getStringExtra(EXTRA_URL)
            ?: throw IllegalArgumentException("url must be provided")
        println("[service] onStartCommand(label = $label, version = $version, appId = $appId, url = $url)")

        val relay = downloadManager.status(appId)

        val file = downloadManager.download(label, version, appId, url)

        notifications.subscribe(
            appId = appId,
            label = label,
            icon = icon,
            file = file,
            start = { notificationId, notification ->
                startForeground(notificationId, notification)
            },
            stop = { stopForeground(true) },
            observable = relay,
        )

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        println("[service] onDestroy")
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder {
        println("[service] onBind")
        return Binder()
    }
}

fun createDownloadIntent(
    context: Context,
    label: String,
    version: String,
    icon: String?,
    appId: String,
    url: String
): Intent = Intent(context, DownloadService::class.java)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_VERSION, version)
    .putExtra(EXTRA_ICON, icon)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_URL, url)

private const val EXTRA_LABEL = "label"
private const val EXTRA_VERSION = "version"
private const val EXTRA_ICON = "icon"
private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_URL = "url"
