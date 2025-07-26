package com.tomclaw.appsend.core

import android.content.Context
import android.content.pm.PackageManager
import com.tomclaw.appsend.util.versionCodeCompat

interface AppInfoProvider {

    fun getPackageName(): String

    fun getVersionName(): String

    fun getVersionCode(): Long

}

class AppInfoProviderImpl(
    context: Context,
    packageManager: PackageManager,
) : AppInfoProvider {

    private val packageInfo = packageManager.getPackageInfo(context.packageName, 0)

    override fun getPackageName(): String = packageInfo.packageName

    override fun getVersionName(): String = packageInfo.versionName.orEmpty()

    override fun getVersionCode(): Long = packageInfo.versionCodeCompat()

}
