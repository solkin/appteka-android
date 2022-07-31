package com.tomclaw.appsend.screen.details.adapter.controls

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.util.NOT_INSTALLED

class ControlsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ControlsItemView, ControlsItem> {

    override fun bindView(view: ControlsItemView, item: ControlsItem, position: Int) {
        view.hideButtons()
        when {
            item.installedVersionCode == NOT_INSTALLED -> {
                view.showInstallButton()
            }
            item.installedVersionCode < item.versionCode -> {
                view.showRemoveButton()
                view.showUpdateButton()
            }
            else -> {
                view.showRemoveButton()
                view.showOpenButton()
            }
        }
        view.setOnInstallClickListener { listener.onInstallClick(item.appId) }
    }

}
