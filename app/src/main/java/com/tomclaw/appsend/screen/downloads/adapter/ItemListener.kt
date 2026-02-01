package com.tomclaw.appsend.screen.downloads.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onDeleteClick(item: Item)

    fun onRetryClick(item: Item)

    fun onLoadMore(item: Item)

}
