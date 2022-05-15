package com.tomclaw.appsend.screen.details.adapter.header

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class HeaderItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<HeaderItemView, HeaderItem> {

    override fun bindView(view: HeaderItemView, item: HeaderItem, position: Int) {
        view.setAppIcon(item.icon)
        view.setAppLabel(item.label)
        view.setAppPackage(item.packageName)
        if (item.userId != null && item.userIcon != null) {
            view.showUploader()
            view.setUploaderIcon(item.userIcon)
            view.setUploaderName(item.userIcon.label["en"].orEmpty()) // TODO: locale + real name
        } else {
            view.hideUploader()
        }
    }

}
