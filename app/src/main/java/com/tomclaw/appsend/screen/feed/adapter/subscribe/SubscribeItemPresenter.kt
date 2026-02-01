package com.tomclaw.appsend.screen.feed.adapter.subscribe

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.feed.FeedResourceProvider
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.user.api.UserBrief
import java.util.Locale

class SubscribeItemPresenter(
    private val locale: Locale,
    private val resourceProvider: FeedResourceProvider,
    private val listener: ItemListener,
) : ItemPresenter<SubscribeItemView, SubscribeItem> {

    override fun bindView(view: SubscribeItemView, item: SubscribeItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                listener.onLoadMore(this)
            }
        }

        view.setUserName(item.user.name())
        view.setUserIcon(item.user.userIcon)
        view.setPublisherName(item.publisher.name())
        view.setPublisherIcon(item.publisher.userIcon)
        view.setTime(resourceProvider.formatTime(item.time))
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (!item.actions.isNullOrEmpty()) view.showMenu() else view.hideMenu()
        item.reacts.takeIf { !it.isNullOrEmpty() }
            ?.let { view.setReactions(it) }
            ?: view.hideReactions()
        view.setOnPostClickListener { listener.onItemClick(item) }
        view.setOnPublisherClickListener { listener.onUserClick(item.publisher) }
        view.setOnMenuClickListener { listener.onMenuClick(item) }
        view.setOnReactionClickListener { reaction -> listener.onReactionClick(item, reaction) }
    }

    private fun UserBrief.name(): String {
        return name.takeIf { !it.isNullOrBlank() }
            ?: userIcon.label[locale.language]
            ?: userIcon.label[DEFAULT_LOCALE].orEmpty()
    }

}
