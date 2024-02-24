package com.tomclaw.appsend.screen.profile.adapter.uploads

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener

class UploadsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<UploadsItemView, UploadsItem> {

    override fun bindView(view: UploadsItemView, item: UploadsItem, position: Int) {
        view.setUploadsCount(item.count.toString())
    }

}
