package com.tomclaw.appsend.screen.moderation.adapter.app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.moderation.adapter.ItemListener

class AppItemPresenter(
    private val listener: ItemListener
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
        view.setRating(item.rating)
        view.setDownloads(item.downloads)
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.hasError) view.showError() else view.hideError()
        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnRetryListener { listener.onRetryClick(item) }
    }

}
