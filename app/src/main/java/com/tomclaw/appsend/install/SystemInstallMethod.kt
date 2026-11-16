package com.tomclaw.appsend.install

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.tomclaw.appsend.R
import com.tomclaw.appsend.download.DownloadNotifications
import com.tomclaw.appsend.util.ActivityStarter

/** Leaves the whole install to Android's package installer screen. */
class SystemInstallMethod(
    private val context: Context,
    private val activities: ActivityStarter,
    private val notifications: DownloadNotifications,
) : InstallMethod {

    override fun install(request: InstallRequest) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(request.uri, APK_MIME_TYPE)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            if (!activities.start(intent)) {
                notifications.showInstallReady(request)
            }
        } catch (_: ActivityNotFoundException) {
            // Some TVs and stripped firmwares ship without a package installer
            Toast.makeText(context, R.string.no_package_installer, Toast.LENGTH_LONG).show()
        }
    }

    // The system's screen owns confirmation and cancellation.
    override fun confirm(key: String, confirmation: Intent?) = Unit

    override fun cancel(key: String) = Unit

}

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
