package com.tomclaw.appsend.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by ivsolkin on 27.01.17.
 */

fun Context.openFileIntent(filePath: String, type: String?): Intent {
    val context = this
    val file = File(filePath)
    val uri = if (isFileProviderUri) {
        FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
    } else {
        Uri.fromFile(file)
    }
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, type)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    grantUriPermission(context, uri, intent)

    return intent
}

private fun grantUriPermission(context: Context, uri: Uri, intent: Intent) {
    if (isFileProviderUri) {
        val resInfoList = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

private val isFileProviderUri: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

