package com.tomclaw.appsend.screen.upload.adapter.selected_app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener
import com.tomclaw.appsend.util.PackageIconLoader

class SelectedAppItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: SelectedAppResourceProvider,
) : ItemPresenter<SelectedAppItemView, SelectedAppItem> {

    override fun bindView(view: SelectedAppItemView, item: SelectedAppItem, position: Int) {
        with(view) {
            val uri = PackageIconLoader.getUri(item.packageInfo.packageInfo)
            setAppIcon(uri)
            setAppLabel(item.packageInfo.label)
            setAppPackage(item.packageInfo.packageName)
            setAppVersion(item.packageInfo.version)
            setAppSize(resourceProvider.formatFileSize(item.packageInfo.size))
        }
        view.setOnClickListener { listener.onSelectAppClick() }
        view.setOnDiscardListener { listener.onDiscardClick() }
    }

}
