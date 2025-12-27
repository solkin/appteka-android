package com.tomclaw.appsend.screen.upload.adapter.prefill_version

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class PrefillVersionItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<PrefillVersionItemView, PrefillVersionItem> {

    override fun bindView(view: PrefillVersionItemView, item: PrefillVersionItem, position: Int) {
        with(view) {
            setVersions(item.versions, item.selectedVersion)
            setOnVersionSelectedListener { version ->
                listener.onPrefillVersionSelected(version)
            }
        }
    }

}
