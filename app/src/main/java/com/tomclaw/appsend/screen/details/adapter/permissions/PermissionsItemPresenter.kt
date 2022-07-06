package com.tomclaw.appsend.screen.details.adapter.permissions

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class PermissionsItemPresenter(
    private val resourceProvider: PermissionsResourceProvider,
    private val listener: ItemListener
) : ItemPresenter<PermissionsItemView, PermissionsItem> {

    override fun bindView(view: PermissionsItemView, item: PermissionsItem, position: Int) {
        var hasNetworkAccess = false
        var hasCallsAccess = false
        var hasSmsAccess = false
        var hasStorageAccess = false
        var hasLocationAccess = false
        var otherAccessCount = 0
        var otherAccessText: String? = null

        item.permissions.forEach { permission ->
            val permissionUpper = permission.uppercase()
            when {
                permissionUpper.contains("NETWORK") -> hasNetworkAccess = true
                permissionUpper.contains("CALL") -> hasCallsAccess = true
                permissionUpper.contains("SMS") -> hasSmsAccess = true
                permissionUpper.contains("STORAGE") -> hasStorageAccess = true
                permissionUpper.contains("LOCATION") -> hasLocationAccess = true
                else -> otherAccessCount++
            }
        }

        if (otherAccessCount > 0) {
            otherAccessText = resourceProvider.formatOtherAccessText(otherAccessCount)
        }

        view.showAccess(
            network = hasNetworkAccess,
            calls = hasCallsAccess,
            sms = hasSmsAccess,
            storage = hasStorageAccess,
            location = hasLocationAccess,
            otherText = otherAccessText
        )

        view.setOnClickListener { listener.onPermissionsClick(permissions = item.permissions) }
    }

}
