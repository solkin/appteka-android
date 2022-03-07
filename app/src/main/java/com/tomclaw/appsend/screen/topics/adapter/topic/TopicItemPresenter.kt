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
        if (item.hasUnread) {
            view.showUnread()
        } else {
            view.hideUnread()
        }
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnLongClickListener { listener.onItemLongClick(item) }
    }

}
