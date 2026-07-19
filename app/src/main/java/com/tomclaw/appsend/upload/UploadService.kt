package com.tomclaw.appsend.upload

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
import com.tomclaw.appsend.upload.di.UploadServiceModule
import com.tomclaw.appsend.util.getParcelableExtraCompat
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject

class UploadService : Service() {

    @Inject
    lateinit var uploadManager: UploadManager

    @Inject
    lateinit var notifications: UploadNotifications

    private val activeUploads = CopyOnWriteArraySet<String>()

    // Upload callbacks arrive on the transfer thread; hopping to the main thread keeps
    // them ordered against onStartCommand, so a starting upload can't race a stopSelf
    private val handler = Handler(Looper.getMainLooper())

    private var lastStartId: Int = 0

    override fun onCreate() {
        super.onCreate()
        println("[upload service] onCreate")
        appComponent
            .uploadServiceComponent(UploadServiceModule(this))
            .inject(service = this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId

        // We are launched with startForegroundService(), which gives 5 seconds to enter
        // foreground whatever the intent turns out to hold. Unmarshalling extras first
        // would let a malformed intent fall through into ForegroundServiceDidNotStartInTime.
        startForegroundCompat(
            UPLOAD_NOTIFICATION_ID,
            notifications.createInitialNotification(getString(R.string.app_name))
        )

        val accepted = intent?.let { onIntentReceived(it) } == true
        if (!accepted && activeUploads.isEmpty()) {
            stopForegroundCompat()
            stopSelf(startId)
        }

        // Nothing useful to redeliver: a restart carries no extras, and re-entering
        // foreground from the background would crash on Android 12+ anyway
        return START_NOT_STICKY
    }

    private fun onIntentReceived(intent: Intent): Boolean {
        val pkg = intent.getParcelableExtraCompat(EXTRA_PACKAGE_INFO, UploadPackage::class.java)
            ?: return false
        val apk = intent.getParcelableExtraCompat(EXTRA_APK_INFO, UploadApk::class.java)
        val info = intent.getParcelableExtraCompat(EXTRA_INFO, UploadInfo::class.java)
            ?: return false

        println("[upload service] onStartCommand(pkg = $pkg, apk = $apk, info = $info)")

        val id = pkg.uniqueId

        activeUploads.add(id)

        // Now that extras parsed, swap the placeholder title for the real app label
        val label = apk?.packageInfo?.applicationInfo?.loadLabel(packageManager)?.toString()
            ?: pkg.uniqueId
        startForegroundCompat(UPLOAD_NOTIFICATION_ID, notifications.createInitialNotification(label))

        // Start the upload first so the relay's cached state is fresh (AWAIT)
        // before any subscriber attaches. Otherwise BehaviorRelay would replay
        // the previous terminal state (e.g. ERROR after a retry), and the
        // notification subscriber would immediately stop the foreground service.
        uploadManager.upload(id, pkg, apk, info)

        val relay = uploadManager.status(id)

        if (apk != null) {
            notifications.subscribe(
                id = id,
                pkg = pkg,
                apk = apk,
                info = info,
                stop = {
                    handler.post { onUploadFinished(id) }
                },
                observable = relay,
            )
        } else {
            // take(1) disposes itself, so a state already cached in the relay can't
            // leave a live subscription behind the way a captured Disposable would
            relay.filter { it.status in TERMINAL_UPLOAD_STATUSES }
                .take(1)
                .subscribe { handler.post { onUploadFinished(id) } }
        }

        return true
    }

    // Other uploads may still be running, so don't tear the service down for them.
    private fun onUploadFinished(id: String) {
        activeUploads.remove(id)
        if (activeUploads.isEmpty()) {
            stopForegroundCompat()
            // Keeps an upload queued right at this moment from being dropped
            stopSelf(lastStartId)
        }
    }

    /**
     * Android 15+ gives a dataSync service 6 hours per day and kills the app with
     * ForegroundServiceDidNotStopInTimeException unless it stops itself within
     * seconds of this callback.
     */
    override fun onTimeout(startId: Int) = onTimeoutReached()

    override fun onTimeout(startId: Int, fgsType: Int) = onTimeoutReached()

    private fun onTimeoutReached() {
        println("[upload service] onTimeout")
        activeUploads.clear()
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
        println("[upload service] onDestroy")
        handler.removeCallbacksAndMessages(null)
        stopForegroundCompat()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        println("[upload service] onBind")
        return Binder()
    }

}

fun createUploadIntent(
    context: Context,
    pkg: UploadPackage,
    apk: UploadApk?,
    info: UploadInfo,
): Intent = Intent(context, UploadService::class.java)
    .putExtra(EXTRA_PACKAGE_INFO, pkg)
    .putExtra(EXTRA_APK_INFO, apk)
    .putExtra(EXTRA_INFO, info)

private const val EXTRA_PACKAGE_INFO = "pkg"
private const val EXTRA_APK_INFO = "apk"
private const val EXTRA_INFO = "info"
