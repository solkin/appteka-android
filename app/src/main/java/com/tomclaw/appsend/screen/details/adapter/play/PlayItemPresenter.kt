package com.tomclaw.appsend.screen.details.adapter.play

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import java.util.Locale

class PlayItemPresenter(
    private val locale: Locale,
    private val resourceProvider: PlayResourceProvider
) : ItemPresenter<PlayItemView, PlayItem> {

    override fun bindView(view: PlayItemView, item: PlayItem, position: Int) {
        item.rating?.takeIf { it > 0 }?.let { view.showRating(it.toString()) } ?: view.hideRating()

        view.setDownloads(item.downloads)

        view.setSize(resourceProvider.formatFileSize(item.size))

        if (item.exclusive) view.showExclusive() else view.hideExclusive()

        item.category?.let {
            val title = it.name[locale.language] ?: it.name[DEFAULT_LOCALE].orEmpty()
            view.showCategory(it.icon, title)
        } ?: view.hideCategory()

        item.osVersion?.let { view.showOsVersion(it) } ?: view.hideOsVersion()
    }

}
