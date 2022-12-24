package com.tomclaw.appsend.screen.details.adapter.description

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class DescriptionItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: DescriptionResourceProvider
) : ItemPresenter<DescriptionItemView, DescriptionItem> {

    override fun bindView(view: DescriptionItemView, item: DescriptionItem, position: Int) {
        view.setText(item.text)
        view.setAppVersion(resourceProvider.formatFileVersion(item.versionName, item.versionCode))
        view.setVersionsCount(item.versionsCount)
        view.setUploadDate(resourceProvider.formatDate(item.uploadDate))
        view.setChecksum(item.checksum)

        view.setOnGooglePlayClickListener { listener.onGooglePlayClick() }
        view.setOnVersionsClickListener { listener.onVersionsClick() }
    }

}
