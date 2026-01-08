package com.tomclaw.appsend.download

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.download.di.DownloadServiceModule
import javax.inject.Inject

class DownloadService : Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var notifications: DownloadNotifications

    override fun onCreate() {
        super.onCreate()
        println("[download service] onCreate")
        Appteka.getComponent()
            .downloadServiceComponent(DownloadServiceModule(this))
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

        println("[download service] onStartCommand(label = $label, version = $version, appId = $appId, url = $url)")

        // Start foreground immediately to avoid ForegroundServiceStartNotAllowedException on Android 12+
        val initialNotification = notifications.createInitialNotification(label)
        startForegroundCompat(DOWNLOAD_NOTIFICATION_ID, initialNotification)

        val relay = downloadManager.status(appId)

        val file = downloadManager.download(label, version, appId, url)

        notifications.subscribe(
            appId = appId,
            label = label,
            icon = icon,
            file = file,
            stop = {
                stopForegroundCompat()
            },
            observable = relay,
        )
        return true
    }

    private fun startForegroundCompat(notificationId: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(notificationId, notification)
        }
    }

    @Suppress("DEPRECATION")
    private fun Service.stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onDestroy() {
        println("[download service] onDestroy")
        stopForegroundCompat()
    }

    override fun onBind(intent: Intent): IBinder {
        println("[download service] onBind")
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
