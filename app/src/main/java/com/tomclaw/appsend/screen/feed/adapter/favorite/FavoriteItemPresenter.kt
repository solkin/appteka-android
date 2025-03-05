package com.tomclaw.appsend.screen.feed.adapter.favorite

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.feed.FeedResourceProvider
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import java.util.Locale

class FavoriteItemPresenter(
    private val locale: Locale,
    private val resourceProvider: FeedResourceProvider,
    private val listener: ItemListener,
) : ItemPresenter<FavoriteItemView, FavoriteItem> {

    override fun bindView(view: FavoriteItemView, item: FavoriteItem, position: Int) {
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
        view.setIcon(item.icon)
        view.setLabel(item.title)
        view.setPackage(item.packageName)
        view.setText(item.description.orEmpty())
        item.screenshots
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.let {
                view.setImage(it.preview)
                view.setOnImageClickListener { listener.onImageClick(it) }
            }
            ?: view.hideImage()
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnPostClickListener { listener.onItemClick(item) }
    }

}
