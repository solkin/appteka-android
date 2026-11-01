package com.tomclaw.appsend.install

import android.net.Uri

/**
 * [key] identifies the install for status and cancellation, and doubles as the
 * identity of its notification.
 */
data class InstallRequest(
    val key: String,
    val label: String,
    val uri: Uri,
)

/** One way of getting an APK onto the device. */
interface InstallMethod {

    fun install(request: InstallRequest)

    fun cancel(key: String)

}
