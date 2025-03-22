package com.tomclaw.appsend.screen.post.adapter.ribbon

import com.avito.konveyor.blueprint.Item

data class RibbonItem(
    override val id: Long,
    val items: List<Item>,
) : Item
