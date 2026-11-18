package com.tomclaw.appsend.screen.details

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.tomclaw.appsend.util.NOT_INSTALLED
import com.tomclaw.appsend.util.SIGNING_INFO_FLAG
import com.tomclaw.appsend.util.editionKey
import com.tomclaw.appsend.util.getPackageInfoCompat

/** What the details screen knows about the installed package. */
interface InstalledAppProvider {

    fun installedApp(packageName: String, versionCode: Int): InstalledApp

}

class InstalledAppProviderImpl(
    private val packageManager: PackageManager,
) : InstalledAppProvider {

    override fun installedApp(packageName: String, versionCode: Int): InstalledApp {
        if (versionCode == NOT_INSTALLED) return InstalledApp.NONE
        return try {
            val info = packageManager.getPackageInfoCompat(packageName, SIGNING_INFO_FLAG)
            val flags = info.applicationInfo?.flags ?: ApplicationInfo.FLAG_SYSTEM
            InstalledApp(
                versionCode = versionCode,
                editionKey = info.editionKey(),
                // Removing a system app only rolls it back to its factory build.
                removable = flags and ApplicationInfo.FLAG_SYSTEM == 0,
            )
        } catch (_: Exception) {
            // Gone meanwhile, or the package manager failed: a key we cannot
            // read is unknown, and versions decide as before editions.
            InstalledApp(versionCode, editionKey = null, removable = false)
        }
    }

}
