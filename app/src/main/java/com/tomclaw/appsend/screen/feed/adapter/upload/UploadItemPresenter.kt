package com.tomclaw.appsend.screen.feed.adapter.upload

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.feed.FeedResourceProvider
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import java.util.Locale

class UploadItemPresenter(
    private val locale: Locale,
    private val resourceProvider: FeedResourceProvider,
    private val listener: ItemListener,
) : ItemPresenter<UploadItemView, UploadItem> {

    override fun bindView(view: UploadItemView, item: UploadItem, position: Int) {
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
        with(view) {
            setUserName(name)
            setUserIcon(item.user.userIcon)
            setTime(resourceProvider.formatTime(item.time))
            setIcon(item.icon)
            setLabel(item.title)
            setPackage(item.packageName)
            setText(item.description.orEmpty())
            item.screenshots.takeIf { it.isNotEmpty() }
                ?.let { setImages(item.screenshots) }
                ?: view.hideImage()
        }
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (!item.actions.isNullOrEmpty()) view.showMenu() else view.hideMenu()
        item.reacts.takeIf { !it.isNullOrEmpty() }
            ?.let { view.setReactions(it) }
            ?: view.hideReactions()
        view.setOnPostClickListener { listener.onItemClick(item) }
        view.setOnAppClickListener { listener.onAppClick(item.appId, item.title) }
        view.setOnMenuClickListener { listener.onMenuClick(item) }
        view.setOnReactionClickListener { reaction -> listener.onReactionClick(item, reaction) }
    }

}
