package com.tomclaw.appsend.screen.feed.adapter.upload

import android.net.Uri
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
        view.setUserName(name)
        view.setUserIcon(item.user.userIcon)
        view.setTime(resourceProvider.formatTime(item.time))
        view.setText(item.description.orEmpty())
        item.screenshots
            .takeIf { it.isNotEmpty() }
            ?.first()
            ?.let {
                view.setImage(Uri.parse(it.preview))
//                view.setOnImageClickListener { listener.onImageClick(it) }
            }
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnPostClickListener { listener.onItemClick(item) }
    }

}
