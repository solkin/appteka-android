package com.tomclaw.appsend.screen.ratings.adapter

import com.avito.konveyor.blueprint.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onDeleteClick(item: Item)

    fun onRetryClick(item: Item)

    fun onLoadMore(item: Item)

}
