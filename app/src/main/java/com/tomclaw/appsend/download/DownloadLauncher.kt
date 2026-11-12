package com.tomclaw.appsend.download

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Starts [DownloadService] from an activity, asking for the notification
 * permission first on Android 13+. The download starts whatever the answer:
 * without the permission only its notification is missing.
 *
 * Create it while the activity is being constructed: it registers a result
 * launcher, which is only allowed before the activity starts.
 */
class DownloadLauncher(
    private val activity: ComponentActivity,
    private val onStarted: () -> Unit = {},
) {

    // Downloads asked for while the permission question is open all start
    // once it is answered.
    private val pending = ArrayList<DownloadParams>()

    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // The system needs a moment to apply the permission before a
            // foreground service may start.
            Handler(Looper.getMainLooper()).postDelayed({
                val queued = pending.toList()
                pending.clear()
                queued.forEach { start(it) }
            }, PERMISSION_APPLY_DELAY)
        }

    fun launch(params: DownloadParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                pending += params
                if (pending.size == 1) permissionLauncher.launch(permission)
                return
            }
        }
        start(params)
    }

    private fun start(params: DownloadParams) {
        val intent = createDownloadIntent(
            context = activity,
            label = params.label,
            version = params.version,
            icon = params.icon,
            appId = params.appId,
            url = params.url,
            sha1 = params.sha1,
        )
        ContextCompat.startForegroundService(activity, intent)
        onStarted()
    }

}

data class DownloadParams(
    val label: String,
    val version: String,
    val icon: String?,
    val appId: String,
    val url: String,
    val sha1: String?,
)

private const val PERMISSION_APPLY_DELAY = 100L
