package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import com.avito.konveyor.blueprint.ItemPresenter

class UnsafePermissionItemPresenter : ItemPresenter<UnsafePermissionItemView, UnsafePermissionItem> {

    override fun bindView(view: UnsafePermissionItemView, item: UnsafePermissionItem, position: Int) {
        view.setDescription(item.description)
        view.setPermission(item.permission)
    }

}
