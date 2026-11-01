package com.tomclaw.appsend.install

import android.content.Context
import android.content.Intent

/** Leaves the whole install to Android's package installer screen. */
class SystemInstallMethod(
    private val context: Context,
) : InstallMethod {

    override fun install(request: InstallRequest) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(request.uri, APK_MIME_TYPE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    // The system's screen is the user's to cancel
    override fun cancel(key: String) = Unit

}

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
