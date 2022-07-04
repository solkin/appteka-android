package com.tomclaw.appsend.screen.details.adapter.permissions

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class PermissionsItemPresenter(
    private val resourceProvider: PermissionsResourceProvider,
    private val listener: ItemListener
) : ItemPresenter<PermissionsItemView, PermissionsItem> {

    override fun bindView(view: PermissionsItemView, item: PermissionsItem, position: Int) {
        view.showAccess(
            network = true,
            calls = true,
            sms = true,
            storage = true,
            location = true,
            otherText = "14 more permissions"
        )
    }

}
