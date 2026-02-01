package com.tomclaw.appsend.screen.store.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(item: Item)

}
