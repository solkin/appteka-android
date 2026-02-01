package com.tomclaw.appsend.screen.topics.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onItemLongClick(item: Item)

    fun onLoadMore(item: Item)

}
