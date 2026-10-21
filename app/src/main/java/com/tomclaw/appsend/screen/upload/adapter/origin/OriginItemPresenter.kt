package com.tomclaw.appsend.screen.upload.adapter.origin

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class OriginItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<OriginItemView, OriginItem> {

    override fun bindView(view: OriginItemView, item: OriginItem, position: Int) {
        with(view) {
            setOrigin(item.value)
            setOwnEnabled(item.ownDenied == null)
            when {
                item.errorRequiredField -> showRequiredFieldError()
                // Until something is picked, say why one option is off.
                item.value == null && item.ownDenied != null -> showOwnDenied(item.ownDenied)
                else -> showDescription(item.value)
            }
            setOnOriginClickListener { listener.onOriginChanged(it) }
        }
    }

}
