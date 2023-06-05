package com.tomclaw.appsend.screen.upload.adapter.submit

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class SubmitItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<SubmitItemView, SubmitItem> {

    override fun bindView(view: SubmitItemView, item: SubmitItem, position: Int) {
        with(view) {
            when (item.editMode) {
                true -> setEditMode()
                false -> setUploadMode()
            }
            if (item.enabled) setEnabled() else setDisabled()
            setOnClickListener { listener.onSubmitClick() }
        }
    }

}
