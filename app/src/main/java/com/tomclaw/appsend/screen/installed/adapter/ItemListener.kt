package com.tomclaw.appsend.screen.installed.adapter

import com.tomclaw.appsend.util.adapter.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onUpdateClick(item: Item)

    fun onUpdateCancelClick(item: Item)

}
