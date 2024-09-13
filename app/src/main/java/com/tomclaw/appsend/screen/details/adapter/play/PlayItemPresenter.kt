package com.tomclaw.appsend.screen.details.adapter.play

import android.os.Build
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

        if (item.favorites > 0) {
            view.showFavorites(item.favorites)
        } else {
            view.hideFavorites()
        }

        view.setSize(resourceProvider.formatFileSize(item.size))

        if (item.exclusive) view.showExclusive() else view.hideExclusive()

        if (item.openSource) view.showOpenSource() else view.hideOpenSource()

        item.category?.let {
            val title = it.name[locale.language] ?: it.name[DEFAULT_LOCALE].orEmpty()
            view.showCategory(it.icon, title)
        } ?: view.hideCategory()

        item.osVersion?.let { osVersion ->
            if ((item.minSdk ?: 0) <= Build.VERSION.SDK_INT) {
                view.showOsVersionCompatible(osVersion)
            } else {
                view.showOsVersionIncompatible(osVersion)
            }
        } ?: view.hideOsVersion()
    }

}
