package com.tomclaw.appsend.screen.profile.adapter.app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.uploads.AppItemListener

class AppItemPresenter(
    private val listener: AppItemListener,
) : ItemPresenter<AppItemView, AppItem> {

    override fun bindView(view: AppItemView, item: AppItem, position: Int) {
        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setRating(item.rating.takeIf { it > 0 })
        view.setOnClickListener { listener.onAppClick(item) }
    }

}
