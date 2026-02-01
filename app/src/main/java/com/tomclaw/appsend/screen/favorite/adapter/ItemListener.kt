package com.tomclaw.appsend.screen.favorite.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onRetryClick(item: Item)

    fun onLoadMore(item: Item)

}
