package com.tomclaw.appsend.screen.installed.adapter

import com.avito.konveyor.blueprint.Item

interface ItemListener {

    fun onItemClick(item: Item)

    fun onUpdateClick(title: String, appId: String)

}
