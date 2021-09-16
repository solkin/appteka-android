package com.tomclaw.appsend.screen.moderation.adapter.app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.vika.screen.home.adapter.ItemClickListener

class AppItemPresenter(
    private val listener: ItemClickListener
) : ItemPresenter<AppItemView, AppItem> {

    override fun bindView(view: AppItemView, item: AppItem, position: Int) {
        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setSubtitle(item.subtitle)
        view.setOnClickListener { listener.onItemClick(item) }
    }

}
