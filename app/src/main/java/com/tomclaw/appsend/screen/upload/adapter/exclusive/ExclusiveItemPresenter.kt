package com.tomclaw.appsend.screen.upload.adapter.exclusive

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class ExclusiveItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ExclusiveItemView, ExclusiveItem> {

    override fun bindView(view: ExclusiveItemView, item: ExclusiveItem, position: Int) {
        with(view) {
            setExclusive(item.value)
            setOnExclusiveChangedListener { listener.onExclusiveChanged(it) }
        }
    }

}
