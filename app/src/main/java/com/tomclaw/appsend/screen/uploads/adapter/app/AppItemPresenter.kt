package com.tomclaw.appsend.screen.uploads.adapter.app

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.core.FileStatus
import com.tomclaw.appsend.screen.uploads.AppsResourceProvider
import com.tomclaw.appsend.screen.uploads.adapter.ItemListener

class AppItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: AppsResourceProvider
) : ItemPresenter<AppItemView, AppItem> {

    override fun bindView(view: AppItemView, item: AppItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                hasError = false
                listener.onLoadMore(this)
            }
        }

        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setVersion(item.version)
        view.setSize(item.size)
        view.setRating(item.rating.takeIf { it > 0 })
        view.setDownloads(item.downloads)
        view.setCategory(item.category)
        var statusText = ""
        var isPublished = false
        if (item.isInstalled && item.isUpdatable) {
            statusText = resourceProvider.getStatusUpdatableString()
            isPublished = true
        } else if (item.isInstalled) {
            statusText = resourceProvider.getStatusInstalledString()
            isPublished = true
        }
        var clickable = true
        when (item.status) {
            FileStatus.UNLINKED -> {
                statusText = resourceProvider.getStatusBlockedString()
                isPublished = false
                clickable = false
            }

            FileStatus.PRIVATE -> {
                statusText = resourceProvider.getStatusPrivateString()
                isPublished = false
            }
            FileStatus.MODERATION -> {
                statusText = resourceProvider.getStatusModerationString()
                isPublished = false
            }
        }
        view.setStatus(statusText, isPublished)
        if (item.isNew) view.showBadge() else view.hideBadge()
        if (item.openSource) view.showOpenSourceBadge() else view.hideOpenSourceBadge()
        if (!item.isAbiCompatible) view.showAbiIncompatibleBadge() else view.hideAbiIncompatibleBadge()
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.hasError) view.showError() else view.hideError()
        if (clickable) {
            view.setOnClickListener { listener.onItemClick(item) }
        } else {
            view.setOnClickListener(null)
            view.setClickable(false)
        }
        view.setOnRetryListener { listener.onRetryClick(item) }
    }

}
