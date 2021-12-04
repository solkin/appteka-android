package com.tomclaw.appsend.screen.chat.adapter.msg

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.moderation.adapter.ItemListener

class MsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<MsgItemView, MsgItem> {

    override fun bindView(view: MsgItemView, item: MsgItem, position: Int) {
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
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.hasError) view.showError() else view.hideError()
        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnRetryListener { listener.onRetryClick(item) }
    }

}
