package com.tomclaw.appsend.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.tomclaw.appsend.util.versionCodeCompat
import java.util.Locale

interface UserAgentProvider {

    fun getUserAgent(): String

}

class UserAgentProviderImpl(
    private val context: Context,
    private val packageManager: PackageManager,
    private val locale: Locale
): UserAgentProvider {

    override fun getUserAgent(): String {
        val info = packageManager.getPackageInfo(context.packageName, 0)
        return String.format("Appteka/%s.%d (%s; %s; sdk:%d; %s; %s-%s)", info.versionName, info.versionCodeCompat(), Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT, info.packageName, locale.language, locale.country)
    }

}
