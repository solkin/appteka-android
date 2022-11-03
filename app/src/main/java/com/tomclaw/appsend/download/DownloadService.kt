package com.tomclaw.appsend.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { onIntentReceived(it) }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onIntentReceived(intent: Intent): Boolean {
        val label = intent.getStringExtra(EXTRA_LABEL) ?: return false
        val version = intent.getStringExtra(EXTRA_VERSION) ?: return false
        val icon = intent.getStringExtra(EXTRA_ICON)
        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: return false
        val url = intent.getStringExtra(EXTRA_URL) ?: return false

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
        println("[service] onDestroy")
        stopForeground()
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
