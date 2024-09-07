package com.tomclaw.appsend.screen.distro

import android.content.pm.PackageManager

interface DistroInfoProvider {

    fun getPackagePermissions(packageName: String): List<String>

}

class DistroInfoProviderImpl(
    private val packageManager: PackageManager,
) : DistroInfoProvider {

    override fun getPackagePermissions(packageName: String): List<String> {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        return listOf(*packageInfo.requestedPermissions)
    }

}
