package com.tomclaw.appsend.screen.details.adapter.permissions

import android.content.res.Resources
import com.tomclaw.appsend.R

interface PermissionsResourceProvider {

    fun formatOtherAccessText(accessCount: Int): String

}

class PermissionsResourceProviderImpl(
    val resources: Resources,
) : PermissionsResourceProvider {

    override fun formatOtherAccessText(accessCount: Int): String {
        return resources.getQuantityString(R.plurals.other_access, accessCount, accessCount)
    }

}