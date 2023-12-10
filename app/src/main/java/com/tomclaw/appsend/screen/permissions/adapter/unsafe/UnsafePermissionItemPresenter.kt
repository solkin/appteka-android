package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.permissions.PermissionsResourceProvider

class UnsafePermissionItemPresenter(
    private val resourceProvider: PermissionsResourceProvider
) : ItemPresenter<UnsafePermissionItemView, UnsafePermissionItem> {

    override fun bindView(view: UnsafePermissionItemView, item: UnsafePermissionItem, position: Int) {
        val description = item.description ?: resourceProvider.getUnknownPermissionString()
        view.setDescription(description)
        view.setPermission(item.permission)
    }

}
