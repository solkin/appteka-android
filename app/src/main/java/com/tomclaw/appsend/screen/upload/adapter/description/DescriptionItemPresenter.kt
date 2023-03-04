package com.tomclaw.appsend.screen.upload.adapter.description

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class DescriptionItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<DescriptionItemView, DescriptionItem> {

    override fun bindView(view: DescriptionItemView, item: DescriptionItem, position: Int) {
        with(view) {
            setText(item.text)
            setOnTextChangedListener { listener.onDescriptionChanged(it) }
        }
    }

}
