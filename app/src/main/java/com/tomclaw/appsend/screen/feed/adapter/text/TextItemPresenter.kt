package com.tomclaw.appsend.screen.feed.adapter.text

import android.net.Uri
import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.feed.FeedResourceProvider
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import java.util.Locale

class TextItemPresenter(
    private val locale: Locale,
    private val resourceProvider: FeedResourceProvider,
    private val listener: ItemListener,
) : ItemPresenter<PostItemView, TextItem> {

    override fun bindView(view: PostItemView, item: TextItem, position: Int) {
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
            ?.let { view.setImage(Uri.parse(it.preview)) }
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnPostClickListener { listener.onItemClick(item) }
    }

}
