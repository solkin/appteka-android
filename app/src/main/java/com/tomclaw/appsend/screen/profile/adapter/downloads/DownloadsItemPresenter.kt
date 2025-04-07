package com.tomclaw.appsend.screen.profile.adapter.downloads

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener

class DownloadsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<DownloadsItemView, DownloadsItem> {

    override fun bindView(view: DownloadsItemView, item: DownloadsItem, position: Int) {
        view.setCount(item.count)
        view.setOnClickListener { listener.onDownloadsClick() }
    }

}
