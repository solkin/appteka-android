package com.tomclaw.appsend.screen.store.adapter.app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.main.item.StoreItem
import com.tomclaw.appsend.screen.store.AppsResourceProvider
import com.tomclaw.appsend.screen.store.adapter.ItemListener

class AppItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: AppsResourceProvider
) : ItemPresenter<AppItemView, AppItem> {

    override fun bindView(view: AppItemView, item: AppItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                listener.onLoadMore(this)
            }
        }

        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setVersion(item.version)
        view.setSize(item.size)
        view.setRating(item.rating.takeIf { it > 0 })
        view.setDownloads(item.downloads)
        var statusText = ""
        if (item.isInstalled && item.isUpdatable) {
            statusText = resourceProvider.getStatusUpdatableString()
        } else if (item.isInstalled) {
            statusText = resourceProvider.getStatusInstalledString()
        }
        var clickable = true
        when (item.status) {
            StoreItem.FILE_STATUS_UNLINKED -> {
                statusText = resourceProvider.getStatusBlockedString()
                clickable = false
            }

            StoreItem.FILE_STATUS_PRIVATE -> statusText = resourceProvider.getStatusPrivateString()
            StoreItem.FILE_STATUS_MODERATION -> statusText =
                resourceProvider.getStatusModerationString()
        }
        view.setStatus(statusText)
        if (item.isNew) view.showBadge() else view.hideBadge()
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setCategory(item.category)
        if (item.openSource) view.showOpenSourceBadge() else view.hideOpenSourceBadge()
        if (clickable) {
            view.setOnClickListener { listener.onItemClick(item) }
        } else {
            view.setOnClickListener(null)
            view.setClickable(false)
        }
    }

}
