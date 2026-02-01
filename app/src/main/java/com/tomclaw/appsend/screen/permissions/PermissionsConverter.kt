package com.tomclaw.appsend.screen.permissions

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.permissions.adapter.safe.SafePermissionItem
import com.tomclaw.appsend.screen.permissions.adapter.unsafe.UnsafePermissionItem

interface PermissionsConverter {

    fun convert(permission: String): Item

}

class PermissionsConverterImpl(
    private val permissionInfoProvider: PermissionInfoProvider
) : PermissionsConverter {

    private var id: Long = 1

    override fun convert(permission: String): Item {
        val permissionBrief = permissionInfoProvider.getPermissionBrief(permission)
        return when (permissionBrief.dangerous) {
            true -> UnsafePermissionItem(
                id = id++,
                permission = permissionBrief.permission,
                description = permissionBrief.description
            )

            false -> SafePermissionItem(
                id = id++,
                permission = permissionBrief.permission,
                description = permissionBrief.description
            )
        }
    }

}
