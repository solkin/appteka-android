package com.tomclaw.appsend.upload

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.main.item.CommonItem
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
        val item = intent.getParcelableExtraCompat(EXTRA_ITEM, CommonItem::class.java) ?: return false

        println("[upload service] onStartCommand(item = $item)")

        val id = item.path

        val relay = uploadManager.status(id)

        notifications.subscribe(
            id = id,
            item = item,
            start = { notificationId, notification ->
                startForeground(notificationId, notification)
            },
            stop = {
                stopForeground()
            },
            observable = relay,
        )

        uploadManager.upload(id, item)
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
    item: CommonItem
): Intent = Intent(context, UploadService::class.java)
    .putExtra(EXTRA_ITEM, item)

private const val EXTRA_ITEM = "item"
