package com.tomclaw.appsend.screen.upload.adapter.origin

import com.tomclaw.appsend.core.Origin
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class OriginItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<OriginItemView, OriginItem> {

    override fun bindView(view: OriginItemView, item: OriginItem, position: Int) {
        with(view) {
            setOrigin(item.value)
            // A package bound to the uploader leaves "own" as the only answer.
            setOptionEnabled(Origin.OWN, enabled = item.ownDenied == null)
            setOptionEnabled(Origin.MIRROR, enabled = !item.fixed)
            setOptionEnabled(Origin.MOD, enabled = !item.fixed)
            item.ownDenied?.let { showOwnDenied(it) } ?: hideOwnDenied()
            // Claiming the app files a developer application — say so before
            // the upload, not after. Nothing to apply for once it is bound.
            if (item.value == Origin.OWN && !item.fixed) showOwnNote() else hideOwnNote()
            if (item.errorRequiredField) showRequiredFieldError() else hideRequiredFieldError()
            setOnOriginChangedListener { listener.onOriginChanged(it) }
        }
    }

}
