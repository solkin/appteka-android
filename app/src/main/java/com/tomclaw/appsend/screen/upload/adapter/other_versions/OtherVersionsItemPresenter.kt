package com.tomclaw.appsend.screen.upload.adapter.other_versions

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class OtherVersionsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<OtherVersionsItemView, OtherVersionsItem> {

    override fun bindView(view: OtherVersionsItemView, item: OtherVersionsItem, position: Int) {
        with(view) {
            setVersionsCount(item.versions.size)
            setOnClickListener { listener.onOtherVersionsClick(item.versions) }
        }
    }

}
