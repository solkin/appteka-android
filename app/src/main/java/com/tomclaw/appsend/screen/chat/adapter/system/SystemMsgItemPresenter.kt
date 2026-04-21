package com.tomclaw.appsend.screen.chat.adapter.system

import com.tomclaw.appsend.util.adapter.ItemPresenter

class SystemMsgItemPresenter : ItemPresenter<SystemMsgItemView, SystemMsgItem> {

    override fun bindView(view: SystemMsgItemView, item: SystemMsgItem, position: Int) {
        view.setText(item.text)
        view.setDate(item.date)
    }

}
