package com.tomclaw.appsend.screen.topics.adapter.topic

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.topics.adapter.ItemListener

class TopicItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<TopicItemView, TopicItem> {

    override fun bindView(view: TopicItemView, item: TopicItem, position: Int) {
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
        view.setMessageText(item.lastMsgText)
        view.setMessageAvatar(item.lastMsgUserIcon)
        if (item.isPinned) {
            view.showPin()
        } else {
            view.hidePin()
        }
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.hasError) view.showError() else view.hideError()
        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnRetryListener { listener.onRetryClick(item) }
    }

}
