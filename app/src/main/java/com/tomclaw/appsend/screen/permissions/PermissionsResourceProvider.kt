package com.tomclaw.appsend.screen.permissions

import android.content.res.Resources
import com.tomclaw.appsend.R

interface PermissionsResourceProvider {

    fun getUnknownPermissionString(): String
}

class PermissionsResourceProviderImpl(
    private val resources: Resources
) : PermissionsResourceProvider {

    override fun getUnknownPermissionString(): String {
        return resources.getString(R.string.unknown_permission_description)
    }

}
