package com.tomclaw.appsend.screen.details.adapter.header

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.download.AWAIT
import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.download.ERROR
import com.tomclaw.appsend.download.IDLE
import com.tomclaw.appsend.download.STARTED
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import java.util.Locale

class HeaderItemPresenter(
    private val locale: Locale,
    private val listener: ItemListener,
) : ItemPresenter<HeaderItemView, HeaderItem> {

    override fun bindView(view: HeaderItemView, item: HeaderItem, position: Int) {
        when (item.downloadState) {
            IDLE -> view.hideProgress()
            AWAIT -> view.setIndeterminate()
            STARTED -> view.setIndeterminate()
            COMPLETED -> view.hideProgress()
            ERROR -> view.hideProgress()
            else -> view.setProgress(item.downloadState)
        }
        view.setAppIcon(item.icon)
        view.setAppLabel(item.label)
        view.setAppPackage(item.packageName)
        val author = item.author
        val icon = author?.icon
        if (author != null && icon != null) {
            view.showUploader()
            view.setUploaderIcon(icon)
            view.setUploaderBadge(author.primaryBadge)

            val name = author.name.takeIf { !it.isNullOrBlank() }
                ?: icon.label?.get(locale.language)
                ?: icon.label?.get(DEFAULT_LOCALE).orEmpty()
            view.setUploaderName(name)

            view.setOnUploaderClickListener { listener.onProfileClick(author.id) }
        } else {
            view.hideUploader()
        }
    }

}
