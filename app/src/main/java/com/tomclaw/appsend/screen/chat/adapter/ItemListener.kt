package com.tomclaw.appsend.screen.chat.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(msgId: Int)

}
