package com.tomclaw.appsend.upload

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.tomclaw.appsend.Appteka
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
        val pkg = intent.getParcelableExtraCompat(EXTRA_PACKAGE_INFO, UploadPackage::class.java)
            ?: return false
        val apk = intent.getParcelableExtraCompat(EXTRA_APK_INFO, UploadApk::class.java)
        val info = intent.getParcelableExtraCompat(EXTRA_INFO, UploadInfo::class.java)
            ?: return false

        println("[upload service] onStartCommand(pkg = $pkg, apk = $apk, info = $info)")

        val id = pkg.uniqueId

        // Start foreground immediately to avoid ForegroundServiceStartNotAllowedException on Android 12+
        val label = apk?.packageInfo?.applicationInfo?.loadLabel(packageManager)?.toString()
            ?: pkg.uniqueId
        val initialNotification = notifications.createInitialNotification(label)
        startForegroundCompat(UPLOAD_NOTIFICATION_ID, initialNotification)

        val relay = uploadManager.status(id)

        if (apk != null) {
            notifications.subscribe(
                id = id,
                pkg = pkg,
                apk = apk,
                info = info,
                stop = {
                    stopForegroundCompat()
                },
                observable = relay,
            )
        } else {
            // Subscribe to status changes even when apk is null to handle foreground notification
            relay.subscribe { state ->
                when (state.status) {
                    UploadStatus.COMPLETED,
                    UploadStatus.ERROR,
                    UploadStatus.IDLE -> {
                        stopForegroundCompat()
                    }
                    else -> {
                        // Keep foreground notification for other states
                    }
                }
            }
        }

        uploadManager.upload(id, pkg, apk, info)
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
        println("[upload service] onDestroy")
        stopForegroundCompat()
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
