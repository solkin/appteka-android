package com.tomclaw.appsend.screen.details.adapter.header

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.util.Status
import java.util.Locale

class HeaderItemPresenter(
    private val locale: Locale,
    private val listener: ItemListener,
) : ItemPresenter<HeaderItemView, HeaderItem> {

    override fun bindView(view: HeaderItemView, item: HeaderItem, position: Int) {
        item.downloadState?.let {
            when(it.status) {
                Status.AWAIT -> { view.setIndeterminate() }
                Status.PROGRESS -> { view.setProgress(it.percent) }
                Status.COMPLETED ->  { view.hideProgress() }
                Status.IDLE -> { view.hideProgress() }
                else -> { view.hideProgress() }
            }
        }
        view.setAppIcon(item.icon)
        view.setAppLabel(item.label)
        view.setAppPackage(item.packageName)
        if (item.userId != null && item.userIcon != null) {
            view.showUploader()
            view.setUploaderIcon(item.userIcon)

            val name = item.userName.takeIf { !it.isNullOrBlank() }
                ?: item.userIcon.label[locale.language]
                ?: item.userIcon.label[DEFAULT_LOCALE].orEmpty()
            view.setUploaderName(name)

            view.setOnUploaderClickListener { listener.onProfileClick(item.userId) }
        } else {
            view.hideUploader()
        }
    }

}
