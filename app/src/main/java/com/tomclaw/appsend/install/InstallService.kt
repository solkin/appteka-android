package com.tomclaw.appsend.install

import android.app.Notification
import android.content.Context
import android.content.Intent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.core.TransferService
import com.tomclaw.appsend.download.DownloadNotifications
import com.tomclaw.appsend.download.INSTALL_NOTIFICATION_ID
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import javax.inject.Inject

/**
 * Keeps the process alive while an APK is copied into its session, which for a large
 * one outlasts the user's patience. The copying itself stays in [SessionInstallMethod];
 * once the session is committed the system carries on without us.
 */
class InstallService : TransferService() {

    @Inject
    lateinit var tracker: InstallTracker

    @Inject
    lateinit var notifications: DownloadNotifications

    private val subscriptions = CompositeDisposable()

    override val logTag = "install service"

    override val notificationId = INSTALL_NOTIFICATION_ID

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(service = this)
    }

    override fun createInitialNotification(intent: Intent?): Notification {
        val label = intent?.getStringExtra(EXTRA_LABEL) ?: getString(R.string.app_name)
        return notifications.createInstallProgress(label, percent = 0)
    }

    override fun onIntentReceived(intent: Intent): Boolean {
        val key = intent.getStringExtra(EXTRA_KEY) ?: return false
        val label = intent.getStringExtra(EXTRA_LABEL) ?: return false

        trackTransfer(key)

        subscriptions += tracker.status(key)
            .observeOn(AndroidSchedulers.mainThread())
            .takeUntil { it !is InstallStatus.Writing }
            .subscribe { status ->
                if (status is InstallStatus.Writing) {
                    notifications.showInstallProgress(label, status.percent)
                } else {
                    onTransferFinished(key)
                }
            }
        return true
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

}

fun createInstallServiceIntent(
    context: Context,
    request: InstallRequest,
): Intent = Intent(context, InstallService::class.java)
    .putExtra(EXTRA_KEY, request.key)
    .putExtra(EXTRA_LABEL, request.label)

private const val EXTRA_KEY = "key"
private const val EXTRA_LABEL = "label"
