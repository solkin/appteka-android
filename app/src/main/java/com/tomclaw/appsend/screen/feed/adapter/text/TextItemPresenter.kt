package com.tomclaw.appsend.screen.feed.adapter.text

import androidx.core.net.toUri
import com.avito.konveyor.blueprint.ItemPresenter
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
        item.screenshots
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.let {
                view.setImage(it.preview.toUri())
                view.setOnImageClickListener { listener.onImageClick(listOf(it), 0) }
            }
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnPostClickListener { listener.onItemClick(item) }
    }

}
