package com.tomclaw.appsend.screen.installed.adapter.app

import com.tomclaw.appsend.download.AWAIT
import com.tomclaw.appsend.download.STARTED
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.installed.AppsResourceProvider
import com.tomclaw.appsend.screen.installed.adapter.ItemListener

class AppItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: AppsResourceProvider
) : ItemPresenter<AppItemView, AppItem> {

    override fun bindView(view: AppItemView, item: AppItem, position: Int) {
        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setVersion(item.version)
        view.setSize(resourceProvider.formatFileSize(item.size))
        view.setUpdateTime(
            if (item.updateTime == item.installTime) {
                resourceProvider.installDate(item.updateTime)
            } else {
                resourceProvider.updateDate(item.updateTime)
            }
        )
        val update = item.update
        view.setUpdateInfo(update?.let { resourceProvider.updateInfo(it.verName, it.size) })
        view.setOtherEditionHint(item.otherEdition != null)
        when {
            update == null -> {
                view.hideUpdateProgress()
                view.hideUpdateButton()
            }

            item.updateState.isUpdateBusy() -> {
                if (item.updateState in 0..100) {
                    view.setUpdateProgress(item.updateState)
                } else {
                    view.showUpdateProgressIndeterminate()
                }
                view.showCancelButton()
            }

            else -> {
                view.hideUpdateProgress()
                view.showUpdateButton()
            }
        }
        if (item.isNew) view.showBadge() else view.hideBadge()
        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnUpdateClickListener {
            if (item.updateState.isUpdateBusy()) {
                listener.onUpdateCancelClick(item)
            } else {
                listener.onUpdateClick(item)
            }
        }
    }

}

/** Waiting for, downloading or installing the update: COMPLETED and the idle states are not. */
fun Int.isUpdateBusy(): Boolean = this == AWAIT || this == STARTED || this in 0..100
