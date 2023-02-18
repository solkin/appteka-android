package com.tomclaw.appsend.screen.upload.adapter.selected_app

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class SelectedAppItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<SelectedAppItemView, SelectedAppItem> {

    override fun bindView(view: SelectedAppItemView, item: SelectedAppItem, position: Int) {
        view.setOnClickListener { listener.onSelectAppClick() }
    }

}
