package com.tomclaw.appsend.screen.users.adapter

import com.avito.konveyor.blueprint.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(item: Item)

}
