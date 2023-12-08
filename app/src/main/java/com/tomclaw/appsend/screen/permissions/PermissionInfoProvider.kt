package com.tomclaw.appsend.screen.permissions

import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface PermissionInfoProvider {

    fun getPermissionBrief(permission: String): PermissionInfoProviderImpl.PermissionBrief

}

class PermissionInfoProviderImpl(
    private val packageManager: PackageManager
) : PermissionInfoProvider {

    override fun getPermissionBrief(permission: String): PermissionBrief {
        var description: String?
        var dangerous: Boolean
        try {
            val permissionInfo = packageManager.getPermissionInfo(permission, GET_META_DATA)
            description = permissionInfo.loadLabel(packageManager).toString()
            dangerous = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissionInfo.protection == PermissionInfo.PROTECTION_DANGEROUS
            } else {
                @Suppress("DEPRECATION")
                permissionInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS
            }
        } catch (ignored: Throwable) {
            description = null
            dangerous = false
        }
        return PermissionBrief(permission, description, dangerous)
    }

    @Parcelize
    data class PermissionBrief(
        val permission: String,
        val description: String?,
        val dangerous: Boolean
    ) : Parcelable

}
