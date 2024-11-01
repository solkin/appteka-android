package com.tomclaw.appsend.screen.feed.adapter

import com.avito.konveyor.blueprint.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(item: Item)

}
