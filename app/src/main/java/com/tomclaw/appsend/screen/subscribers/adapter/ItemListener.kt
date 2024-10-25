package com.tomclaw.appsend.screen.subscribers.adapter

import com.avito.konveyor.blueprint.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onRetryClick(item: Item)

    fun onLoadMore(item: Item)

}
