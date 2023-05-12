package com.tomclaw.appsend.upload

import android.app.Service
import android.content.Context
import android.content.Intent
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
        val pkg = intent.getParcelableExtraCompat(EXTRA_PACKAGE_INFO, UploadPackage::class.java) ?: return false
        val apk = intent.getParcelableExtraCompat(EXTRA_APK_INFO, UploadApk::class.java)
        val info = intent.getParcelableExtraCompat(EXTRA_INFO, UploadInfo::class.java) ?: return false

        println("[upload service] onStartCommand(pkg = $pkg, apk = $apk, info = $info)")

        val id = pkg.uniqueId

        val relay = uploadManager.status(id)

        if (info.checkExist.file == null && apk != null) {
            notifications.subscribe(
                id = id,
                pkg = pkg,
                apk = apk,
                info = info,
                start = { notificationId, notification ->
                    startForeground(notificationId, notification)
                },
                stop = {
                    stopForeground()
                },
                observable = relay,
            )
        }

        uploadManager.upload(id, pkg, apk, info)
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
    pkg: UploadPackage,
    apk: UploadApk,
    info: UploadInfo,
): Intent = Intent(context, UploadService::class.java)
    .putExtra(EXTRA_PACKAGE_INFO, pkg)
    .putExtra(EXTRA_APK_INFO, apk)
    .putExtra(EXTRA_INFO, info)

private const val EXTRA_PACKAGE_INFO = "pkg"
private const val EXTRA_APK_INFO = "apk"
private const val EXTRA_INFO = "info"
