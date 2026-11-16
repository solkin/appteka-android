package com.tomclaw.appsend.download

import android.app.Notification
import android.content.Context
import android.content.Intent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.core.TransferService
import com.tomclaw.appsend.delivery.AppDelivery
import com.tomclaw.appsend.delivery.isRunningHere
import com.tomclaw.appsend.download.di.DownloadServiceModule
import com.tomclaw.appsend.util.logDebug
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

/**
 * Keeps the process alive while a delivery is on our side, from the download through
 * the copy into the install, and tells about the download in a notification. The work
 * itself runs in [AppDelivery]; once the system or the user has it, we are done.
 */
class DownloadService : TransferService() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var appDelivery: AppDelivery

    @Inject
    lateinit var notifications: DownloadNotifications

    private val subscriptions = CompositeDisposable()

    override val logTag = "download service"

    override val notificationId = DOWNLOAD_NOTIFICATION_ID

    override fun onCreate() {
        super.onCreate()
        logDebug("[download service] onCreate")
        appComponent
            .downloadServiceComponent(DownloadServiceModule(this))
            .inject(service = this)
    }

    override fun createInitialNotification(intent: Intent?): Notification {
        val label = intent?.getStringExtra(EXTRA_LABEL) ?: getString(R.string.app_name)
        return notifications.createInitialNotification(label)
    }

    override fun onIntentReceived(intent: Intent): Boolean {
        val label = intent.getStringExtra(EXTRA_LABEL) ?: return false
        val icon = intent.getStringExtra(EXTRA_ICON)
        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: return false

        logDebug("[download service] onStartCommand(label = $label, appId = $appId)")

        trackTransfer(appId)

        notifications.subscribe(
            appId = appId,
            label = label,
            icon = icon,
            observable = downloadManager.status(appId),
        )
        // Hopping to the main thread keeps the stop ordered against onStartCommand
        subscriptions += appDelivery.state(appId)
            .observeOn(AndroidSchedulers.mainThread())
            .takeUntil { !it.isRunningHere }
            .subscribe { if (!it.isRunningHere) onTransferFinished(appId) }
        return true
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

}

/** Keeps the process alive while the download of [params] runs; the delivery starts it. */
fun createDownloadIntent(
    context: Context,
    params: DownloadParams,
): Intent = Intent(context, DownloadService::class.java)
    .putExtra(EXTRA_LABEL, params.label)
    .putExtra(EXTRA_ICON, params.icon)
    .putExtra(EXTRA_APP_ID, params.appId)

private const val EXTRA_LABEL = "label"
private const val EXTRA_ICON = "icon"
private const val EXTRA_APP_ID = "app_id"
