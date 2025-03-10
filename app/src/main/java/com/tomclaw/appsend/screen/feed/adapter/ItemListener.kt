package com.tomclaw.appsend.screen.feed.adapter

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.Screenshot

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(item: Item)

    fun onImageClick(items: List<Screenshot>, clicked: Int)

    fun onAppClick(appId: String, title: String?)

}
