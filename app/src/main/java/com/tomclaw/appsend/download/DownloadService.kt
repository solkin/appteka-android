package com.tomclaw.appsend.download

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.download.di.DownloadServiceModule
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject

class DownloadService : Service() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var notifications: DownloadNotifications

    private val activeDownloads = CopyOnWriteArraySet<String>()

    // Download callbacks arrive on the transfer thread; hopping to the main thread keeps
    // them ordered against onStartCommand, so a starting download can't race a stopSelf
    private val handler = Handler(Looper.getMainLooper())

    private var lastStartId: Int = 0

    override fun onCreate() {
        super.onCreate()
        println("[download service] onCreate")
        appComponent
            .downloadServiceComponent(DownloadServiceModule(this))
            .inject(service = this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId

        // We are launched with startForegroundService(), which gives 5 seconds to enter
        // foreground whatever the intent turns out to hold. Validating extras first would
        // let a malformed intent fall through into ForegroundServiceDidNotStartInTime.
        val label = intent?.getStringExtra(EXTRA_LABEL) ?: getString(R.string.app_name)
        startForegroundCompat(DOWNLOAD_NOTIFICATION_ID, notifications.createInitialNotification(label))

        val accepted = intent?.let { onIntentReceived(it) } == true
        if (!accepted && activeDownloads.isEmpty()) {
            stopForegroundCompat()
            stopSelf(startId)
        }

        // Nothing useful to redeliver: a restart carries no extras, and re-entering
        // foreground from the background would crash on Android 12+ anyway
        return START_NOT_STICKY
    }

    private fun onIntentReceived(intent: Intent): Boolean {
        val label = intent.getStringExtra(EXTRA_LABEL) ?: return false
        val version = intent.getStringExtra(EXTRA_VERSION) ?: return false
        val icon = intent.getStringExtra(EXTRA_ICON)
        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: return false
        val url = intent.getStringExtra(EXTRA_URL) ?: return false

        println("[download service] onStartCommand(label = $label, version = $version, appId = $appId, url = $url)")

        activeDownloads.add(appId)

        val relay = downloadManager.status(appId)

        downloadManager.download(label, version, appId, url)

        notifications.subscribe(
            appId = appId,
            label = label,
            icon = icon,
            installUri = {
                downloadManager.getInstallUri(label, version, appId)
            },
            stop = {
                handler.post { onDownloadFinished(appId) }
            },
            observable = relay,
        )
        return true
    }

    // Other downloads may still be running, so don't tear the service down for them.
    private fun onDownloadFinished(appId: String) {
        activeDownloads.remove(appId)
        if (activeDownloads.isEmpty()) {
            stopForegroundCompat()
            // Keeps a download queued right at this moment from being dropped
            stopSelf(lastStartId)
        }
    }

    /**
     * Android 15+ gives a dataSync service 6 hours per day and kills the app with
     * ForegroundServiceDidNotStopInTimeException unless it stops itself within
     * seconds of this callback. Partial files stay on disk and resume later.
     */
    override fun onTimeout(startId: Int) = onTimeoutReached()

    override fun onTimeout(startId: Int, fgsType: Int) = onTimeoutReached()

    private fun onTimeoutReached() {
        println("[download service] onTimeout")
        activeDownloads.clear()
        stopForegroundCompat()
        stopSelf()
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
        handler.removeCallbacksAndMessages(null)
        stopForegroundCompat()
        super.onDestroy()
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
