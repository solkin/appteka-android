package com.tomclaw.appsend.screen.distro.adapter.apk

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.distro.DistroResourceProvider
import com.tomclaw.appsend.screen.distro.adapter.ItemListener

class ApkItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: DistroResourceProvider
) : ItemPresenter<ApkItemView, ApkItem> {

    override fun bindView(view: ApkItemView, item: ApkItem, position: Int) {
        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setVersion(item.version)
        view.setSize(resourceProvider.formatFileSize(item.size))
        view.setLocation(item.path)
        if (item.isNew) view.showBadge() else view.hideBadge()
        view.setOnClickListener { listener.onItemClick(item) }
    }

}
