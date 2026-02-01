package com.tomclaw.appsend.screen.installed.adapter.app

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
        view.setUpdatable(item.updateAppId != null)
        if (item.isNew) view.showBadge() else view.hideBadge()
        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnUpdateClickListener {
            listener.onUpdateClick(
                item.title,
                item.updateAppId.orEmpty()
            )
        }
    }

}
