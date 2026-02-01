package com.tomclaw.appsend.screen.users.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(item: Item)

}
