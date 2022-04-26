package com.tomclaw.appsend.screen.store.adapter.app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.store.adapter.ItemListener

class AppItemPresenter(
    private val listener: ItemListener
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
        if (item.isNew) view.showBadge() else view.hideBadge()
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnClickListener { listener.onItemClick(item) }
    }

}
