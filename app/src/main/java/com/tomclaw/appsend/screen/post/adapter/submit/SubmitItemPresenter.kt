package com.tomclaw.appsend.screen.post.adapter.submit

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener

class SubmitItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<SubmitItemView, SubmitItem> {

    override fun bindView(view: SubmitItemView, item: SubmitItem, position: Int) {
        with(view) {
            setOnClickListener { listener.onSubmitClick() }
        }
    }

}
