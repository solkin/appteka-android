package com.tomclaw.appsend.screen.chat.adapter.msg

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class MsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<MsgItemView, MsgItem> {

    override fun bindView(view: MsgItemView, item: MsgItem, position: Int) {
        with(item) {
            listener.onLoadMore(this)
        }

        view.setOnClickListener { listener.onItemClick(item) }
    }

}
