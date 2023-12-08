package com.tomclaw.appsend.screen.permissions.adapter.safe

import com.avito.konveyor.blueprint.ItemPresenter

class SafePermissionItemPresenter : ItemPresenter<SafePermissionItemView, SafePermissionItem> {

    override fun bindView(view: SafePermissionItemView, item: SafePermissionItem, position: Int) {
        view.setDescription(item.description)
        view.setPermission(item.permission)
    }

}
