package com.tomclaw.appsend.install

import android.content.Intent
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

    /**
     * Shows the system's question about [key] if it hasn't reached the user yet. A
     * notification passes the [confirmation] it holds: the question outlives our process.
     */
    fun confirm(key: String, confirmation: Intent? = null)

    fun cancel(key: String)

}
