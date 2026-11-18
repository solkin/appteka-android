package com.tomclaw.appsend.screen.details.adapter.controls

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.delivery.DeliveryState
import com.tomclaw.appsend.delivery.isActive
import com.tomclaw.appsend.screen.details.InstallState
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class ControlsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ControlsItemView, ControlsItem> {

    override fun bindView(view: ControlsItemView, item: ControlsItem, position: Int) {
        view.hideButtons()
        view.enableButtons()
        val isProgress = item.delivery.isActive
        when {
            item.delivery == DeliveryState.AwaitingConfirmation -> {
                view.showContinueButton()
                view.showCancelButton()
            }

            item.installState == InstallState.NOT_INSTALLED -> {
                view.showInstallButton()
                if (isProgress) {
                    view.showCancelButton()
                    view.disableInstallButton()
                }
            }

            item.installState == InstallState.OLDER -> {
                if (isProgress) {
                    view.showCancelButton()
                    view.disableUpdateButton()
                } else {
                    view.showRemoveButton()
                    view.showUpdateButton()
                }
            }

            else -> {
                if (isProgress) {
                    view.showCancelButton()
                    view.disableLaunchButton()
                } else {
                    view.showRemoveButton()
                    view.showLaunchButton()
                }
            }
        }
        view.setOnInstallClickListener { listener.onInstallClick() }
        view.setOnUpdateClickListener { listener.onInstallClick() }
        view.setOnLaunchClickListener { listener.onLaunchClick(item.packageName) }
        view.setOnRemoveClickListener { listener.onRemoveClick(item.packageName) }
        view.setOnCancelClickListener { listener.onCancelClick(item.appId) }
    }

}
