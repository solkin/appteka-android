package com.tomclaw.appsend.screen.feed.adapter.text

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.feed.FeedResourceProvider
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import java.util.Locale

class TextItemPresenter(
    private val locale: Locale,
    private val resourceProvider: FeedResourceProvider,
    private val listener: ItemListener,
) : ItemPresenter<TextItemView, TextItem> {

    override fun bindView(view: TextItemView, item: TextItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                listener.onLoadMore(this)
            }
        }

        val name = item.user.name.takeIf { !it.isNullOrBlank() }
            ?: item.user.userIcon.label[locale.language]
            ?: item.user.userIcon.label[DEFAULT_LOCALE].orEmpty()
        view.setUserName(name)
        view.setUserIcon(item.user.userIcon)
        view.setTime(resourceProvider.formatTime(item.time))
        view.setText(item.text)
        item.screenshots.takeIf { it.isNotEmpty() }
            ?.let { view.setImages(item.screenshots) }
            ?: view.hideImage()
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (!item.actions.isNullOrEmpty()) view.showMenu() else view.hideMenu()
        item.reacts.takeIf { !it.isNullOrEmpty() }
            ?.let { view.setReactions(it) }
            ?: view.hideReactions()
        view.setOnPostClickListener { listener.onItemClick(item) }
        view.setOnMenuClickListener { listener.onMenuClick(item) }
        view.setOnReactionClickListener { reaction -> listener.onReactionClick(item, reaction) }
    }

}
