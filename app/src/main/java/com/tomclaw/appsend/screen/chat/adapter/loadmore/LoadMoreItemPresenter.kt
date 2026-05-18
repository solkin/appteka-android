package com.tomclaw.appsend.screen.chat.adapter.loadmore

import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.util.adapter.ItemPresenter

class LoadMoreItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<LoadMoreItemView, LoadMoreItem> {

    override fun bindView(view: LoadMoreItemView, item: LoadMoreItem, position: Int) {
        listener.onLoadMore(item.msgId)
    }

}
